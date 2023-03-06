package io.harmny.service.user.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.harmny.service.user.filter.UserIdAuthenticationToken
import io.harmny.service.user.model.ApplicationTokenDto
import io.harmny.service.user.model.Fail
import io.harmny.service.user.model.TokenAccessType
import io.harmny.service.user.model.TokenPermission
import io.harmny.service.user.model.TokenResourceType
import io.harmny.service.user.model.User
import io.harmny.service.user.properties.JwtProperties
import io.harmny.service.user.utils.ifLeft
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.net.URI
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@Service
class AuthorizationService(
    private val objectMapper: ObjectMapper,
    private val userService: UserService,
    private val tokenService: TokenService,
    jwtProperties: JwtProperties,
) {

    private val key = Keys.hmacShaKeyFor(jwtProperties.key.toByteArray())
    private val parser = Jwts.parserBuilder().setSigningKey(key).build()

    fun generateMasterToken(userId: String): Either<Fail, String> {
        return userService.updateMasterTokenId(userId).flatMap { user ->
            val masterTokenId = user.masterTokenId
            if (masterTokenId == null) {
                Fail.internal("master.token.creation")
            } else {
                val token = TokenCompact(id = masterTokenId, userId = user.id)
                token.toJwtString().right()
            }
        }
    }

    fun getCurrentUserId(): Either<Fail, String> {
        val authToken = SecurityContextHolder.getContext().authentication as? UserIdAuthenticationToken?
        return authToken?.userId?.right() ?: Fail.authentication("token.invalid")
    }

    fun findActiveUserId(tokenString: String): Either<Fail, String> {
        return findActiveUser(tokenString).map { user -> user.id }
    }

    private fun findActiveUser(tokenString: String): Either<Fail, User> {
        return parseToken(tokenString)
            .flatMap { token ->
                token.takeIf { it.applicationId == null }?.userId?.right() ?: Fail.authorization("resource.not.available")
            }.flatMap { userId ->
                userService.findById(userId)?.right() ?: Fail.authorization("user.unknown")
            }.flatMap { user ->
                user.takeIf { it.active }?.right() ?: Fail.authorization("user.inactive")
            }
    }

    fun signIn(email: String, password: String): Either<Fail, String> {
        val user = userService.findByEmailAndPassword(email, password)
            ?: return Fail.authorization(key = "user.not.found.by.credentials")
        val userId = user.takeIf { it.active }?.id
            ?: return Fail.authorization(key = "user.inactive")

        val expirationTime = Instant.now().plus(10, ChronoUnit.MINUTES).toEpochMilli()
        return TokenCompact(userId, expirationTime = expirationTime).toJwtString().right()
    }

    fun toJwtToken(tokenDto: ApplicationTokenDto): String {
        return tokenDto.compact().toJwtString()
    }

    fun validate(
        tokenString: String,
        method: String,
        requestUri: String,
    ): Either<Fail, Boolean> {
        val token = parseToken(tokenString).ifLeft { return it.left() }
        val expirationTime = token.expirationTime?.let { Instant.ofEpochMilli(it) }
        if (expirationTime != null && expirationTime.isBefore(Instant.now())) {
            return Fail.authorization(key = "token.expired")
        }

        val user = userService.findById(token.userId) ?: return Fail.authorization("user.not.found")
        if (!user.active) return Fail.authorization("user.inactive")

        val tokenId = token.id
        val applicationId = token.applicationId
        return if (applicationId == null) {
            if (tokenId != null) {
                // handle master tokens - valid if the same ID matches db value
                if (user.masterTokenId == tokenId) {
                    Either.Right(true)
                } else {
                    Fail.authentication("token.master.invalid")
                }
            } else {
                // handle user's 'UI' token - valid until expiration time
                Either.Right(true)
            }
        } else {
            // handle application tokens
            token.permissions.toTokenPermissions()
                .flatMap { permissions -> checkTokenPermissions(requestUri, method, permissions) }
                .flatMap { checkApplicationTokenExists(user.id, applicationId, token) }
        }
    }

    private fun checkTokenPermissions(
        requestUri: String,
        method: String,
        permissions: List<TokenPermission>,
    ): Either<Fail, Boolean> {
        val requestPath = URI.create(requestUri).path
        val (_, access, _) = permissions.firstOrNull { (resource) ->
            requestPath.contains(resource.path)
        } ?: return Fail.authorization("resource.not.available")

        val allowedMethods = access.flatMap { it.allowedMethods.toList() }.map { it.name }.distinct()
        return if (method.uppercase() !in allowedMethods) {
            Fail.authorization("operation.not.allowed")
        } else {
            true.right()
        }
    }

    private fun checkApplicationTokenExists(
        userId: String,
        applicationId: String,
        token: TokenCompact,
    ): Either<Fail, Boolean> {
        val tokenId = token.id ?: return Fail.authorization(key = "token.invalid")
        tokenService.get(userId, applicationId, tokenId).ifLeft { return it.left() }
        return Either.Right(true)
    }

    private fun List<String>?.toTokenPermissions(): Either<Fail, List<TokenPermission>> {
        val permissions = this?.map {
            val parts = it.split(":").takeIf { parts -> parts.size == 2 || parts.size == 3 } ?: return Fail.authorization("token.invalid")
            val resource = TokenResourceType.byCode(parts[0]) ?: return Fail.authorization("token.invalid")
            val accessList = parts[1].toCharArray().map { access ->
                TokenAccessType.byCode(access.toString()) ?: return Fail.authorization("token.invalid")
            }
            val own = parts.takeIf { parts.size == 3 }?.get(2) != "n"
            TokenPermission(resource = resource, access = accessList, own = own)
        } ?: emptyList()
        return permissions.right()
    }

    private fun TokenCompact.toJwtString(): String = Jwts.builder()
        .setClaims(hashMapOf<String, Any>(Pair("token", objectMapper.writeValueAsString(this))))
        .setIssuedAt(Date())
        .setExpiration(Date(System.currentTimeMillis() + ChronoUnit.YEARS.duration.toMillis()))
        .signWith(key)
        .compact()

    private fun parseToken(token: String): Either<Fail, TokenCompact> {
        return try {
            val claims = parser.parseClaimsJws(token).body
            objectMapper.readValue<TokenCompact>(claims.get("token", String::class.java)).right()
        } catch (e: Exception) {
            Fail.authorization(key = "token.invalid")
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
private data class TokenCompact(
    @JsonProperty("u")
    val userId: String,
    @JsonProperty("i")
    val id: String? = null,
    @JsonProperty("a")
    val applicationId: String? = null,
    @JsonProperty("p")
    val permissions: List<String>? = null,
    @JsonProperty("e")
    val expirationTime: Long? = null,
)

private fun ApplicationTokenDto.compact(): TokenCompact {
    return TokenCompact(
        id = this.id,
        userId = this.userId,
        applicationId = this.applicationId,
        expirationTime = this.expirationTime?.toEpochMilli(),
        permissions = this.permissions.map {
            val ownPart = if (it.own) "" else ":n"
            "${it.resource.code}:${it.access.joinToString(separator = "") { access -> access.code }}${ownPart}"
        },
    )
}

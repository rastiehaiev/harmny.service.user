package io.harmny.service.user.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.harmny.service.user.model.ApplicationTokenDto
import io.harmny.service.user.model.Fail
import io.harmny.service.user.model.FailReason
import io.harmny.service.user.model.TokenAccessType
import io.harmny.service.user.model.TokenPermission
import io.harmny.service.user.model.TokenResourceType
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
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
) {

    private val rawKey: String = "DAVLtjoTHQ3uhsGm2VstWj5M2JsdhhxQPy71BL11XQ4E5OpRgfCYjNPELkP1M6g"
    private val key = Keys.hmacShaKeyFor(rawKey.toByteArray())
    private val parser = Jwts.parserBuilder().setSigningKey(key).build()

    fun findActiveUserId(tokenString: String): Either<Fail, String> {
        return parseToken(tokenString)
            .flatMap { token ->
                token.takeIf { it.applicationId == null }?.userId?.right() ?: Fail.resourceUnavailable.left()
            }.flatMap { userId ->
                userService.findById(userId)?.right() ?: Fail.userNotFound.left()
            }.flatMap { user ->
                user.takeIf { it.active }?.id?.right() ?: Fail.userNotActive.left()
            }
    }

    fun signIn(email: String, password: String): Either<Fail, String> {
        val user = userService.findByEmailAndPassword(email, password)
            ?: return Fail.unauthenticated(FailReason.USER_NOT_FOUND_BY_EMAIL_AND_PASSWORD).left()
        val userId = user.takeIf { it.active }?.id ?: return Fail.userNotActive.left()

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
        return parseToken(tokenString)
            .flatMap { token ->
                val expirationTime = token.expirationTime?.let { Instant.ofEpochMilli(it) }
                if (expirationTime != null && expirationTime.isBefore(Instant.now())) {
                    Fail.unauthenticated(FailReason.TOKEN_EXPIRED).left()
                }

                val user = userService.findById(token.userId) ?: return@flatMap Fail.userNotFound.left()
                if (!user.active) return@flatMap Fail.userNotActive.left()

                val applicationId = token.applicationId
                if (applicationId == null) {
                    Either.Right(true)
                } else {
                    token.permissions.toTokenPermissions().flatMap { permissions ->
                        val checkFailed = checkTokenPermissions(requestUri, method, permissions)
                        checkFailed?.left() ?: checkApplicationTokenExists(user.id, applicationId, token)
                    }
                }
            }
    }

    private fun checkTokenPermissions(
        requestUri: String,
        method: String,
        permissions: List<TokenPermission>,
    ): Fail? {
        val requestPath = URI.create(requestUri).path
        val (_, access, _) = permissions.firstOrNull { (resource) ->
            requestPath.contains(resource.path)
        } ?: return Fail.unauthorized(FailReason.RESOURCE_NOT_ALLOWED)

        val allowedMethods = access.flatMap { it.allowedMethods.toList() }.map { it.name }.distinct()
        return if (method.uppercase() !in allowedMethods) {
            Fail.unauthorized(FailReason.WRITE_OPERATIONS_NOT_ALLOWED)
        } else {
            null
        }
    }

    private fun checkApplicationTokenExists(
        userId: String,
        applicationId: String,
        token: TokenCompact,
    ): Either<Fail, Boolean> {
        val tokenId = token.id
        return if (tokenId == null) {
            Fail.invalidToken.left()
        } else {
            val applicationToken = tokenService.get(userId, applicationId, tokenId)
            if (applicationToken == null) {
                Fail.tokenDoesNotExist.left()
            } else {
                Either.Right(true)
            }
        }
    }

    private fun List<String>?.toTokenPermissions(): Either<Fail, List<TokenPermission>> {
        val permissions = this?.map {
            val parts = it.split(":").takeIf { parts -> parts.size == 2 || parts.size == 3 } ?: return Fail.invalidToken.left()
            val resource = TokenResourceType.byCode(parts[0]) ?: return Fail.invalidToken.left()
            val accessList = parts[1].toCharArray().map { access ->
                TokenAccessType.byCode(access.toString()) ?: return Fail.invalidToken.left()
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
            Fail.invalidToken.left()
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

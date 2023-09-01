package io.harmny.service.user.web.instruments

import arrow.core.Either
import arrow.core.right
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.harmny.service.user.model.Fail
import io.harmny.service.user.properties.HarmnyUserServiceProperties
import io.harmny.service.user.web.model.TokenAccessType
import io.harmny.service.user.web.model.TokenPermission
import io.harmny.service.user.web.model.TokenPrincipal
import io.harmny.service.user.web.model.TokenResourceType
import io.harmny.service.user.web.model.UserPrincipal
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.SignatureException
import java.util.Date

@Component
class TokenProvider(
    private val objectMapper: ObjectMapper,
    private val properties: HarmnyUserServiceProperties,
) {

    private val key = Keys.hmacShaKeyFor(properties.auth.tokenSecret.toByteArray())
    private val parser = Jwts.parserBuilder().setSigningKey(key).build()

    companion object {
        private val log = LoggerFactory.getLogger(TokenProvider::class.java)
    }

    fun createToken(principal: Any): TokenAndExpiration {
        val tokenCompact = when (principal) {
            is UserPrincipal -> principal.compact()
            is TokenPrincipal -> principal.compact()
            else -> error("Unknown principal")
        }

        val tokenExpiration = getTokenExpiration(principal)
        val token = Jwts.builder()
            .setSubject(objectMapper.writeValueAsString(tokenCompact))
            .setIssuedAt(Date())
            .setExpiration(tokenExpiration)
            .signWith(key)
            .compact()
        return TokenAndExpiration(token, tokenExpiration)
    }

    fun parseToken(tokenString: String): Either<Fail, TokenPrincipal> {
        return try {
            val claimsJws = parser.parseClaimsJws(tokenString)
            claimsJws.body?.subject
                ?.let { objectMapper.readValue<TokenCompact>(it) }
                ?.toPrincipal(claimsJws.body.expiration)
                ?: Fail.authorization(key = "token.invalid")
        } catch (e: Exception) {
            when (e) {
                is SignatureException -> log.error("Invalid JWT signature for token '$tokenString'.")
                is MalformedJwtException -> log.error("Invalid JWT token '$tokenString'.")
                is ExpiredJwtException -> log.error("Expired JWT token '$tokenString'.")
                is UnsupportedJwtException -> log.error("Unsupported JWT token '$tokenString'.")
                else -> log.error("Failed to parse token '$tokenString'. Reason: ${e.message}", e)
            }
            Fail.authorization(key = "token.invalid")
        }
    }

    private fun getTokenExpiration(principal: Any): Date {
        val specifiedExpirationTime = (principal as? TokenPrincipal)?.expirationTime?.let { Date(it) }
        return specifiedExpirationTime ?: Date(System.currentTimeMillis() + properties.auth.tokenExpirationMsDefault)
    }
}

private fun TokenCompact.toPrincipal(expiration: Date?): Either<Fail, TokenPrincipal> {
    val tokenCompact = this
    return this.permissions.toTokenPermissions().map {
        TokenPrincipal(
            id = tokenCompact.id,
            userId = tokenCompact.userId,
            applicationId = tokenCompact.applicationId,
            permissions = it,
            expirationTime = expiration?.time,
        )
    }
}

private fun List<String>?.toTokenPermissions(): Either<Fail, List<TokenPermission>> {
    val permissions = this?.map {
        val parts = it.split(":").takeIf { parts -> parts.size == 2 || parts.size == 3 }
            ?: return Fail.authorization("token.invalid")
        val resource = TokenResourceType.byCode(parts[0]) ?: return Fail.authorization("token.invalid")
        val accessList = parts[1].toCharArray().map { access ->
            TokenAccessType.byCode(access.toString()) ?: return Fail.authorization("token.invalid")
        }
        val own = parts.takeIf { parts.size == 3 }?.get(2) != "n"
        TokenPermission(resource = resource, access = accessList, own = own)
    } ?: emptyList()
    return permissions.right()
}

data class TokenAndExpiration(
    val token: String,
    val expiration: Date,
)

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
    @JsonProperty("r")
    val refresh: Boolean? = null,
)

private fun TokenPrincipal.compact(): TokenCompact {
    return TokenCompact(
        id = this.id,
        userId = this.userId,
        applicationId = this.applicationId,
        permissions = this.permissions.map {
            val ownPart = if (it.own) "" else ":n"
            "${it.resource.code}:${it.access.joinToString(separator = "") { access -> access.code }}${ownPart}"
        },
        refresh = this.refresh,
    )
}

private fun UserPrincipal.compact() = TokenCompact(userId = this.id)

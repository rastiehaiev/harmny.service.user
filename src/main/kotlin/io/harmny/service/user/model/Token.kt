package io.harmny.service.user.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.springframework.http.HttpMethod
import java.time.Instant

data class Token(
    val userId: String,
    val applicationId: String? = null,
    val permissions: List<TokenPermission> = emptyList(),
    val expirationTime: Instant? = null,
) {
    fun isMaster() = applicationId == null
}

data class TokenPermission(
    val resource: TokenResourceType,
    val access: List<TokenAccessType>,
    val own: Boolean = true,
)

fun TokenCompact.loosen(): Either<Fail, Token> {
    val permissions = this.p.map {
        val parts = it.split(":").takeIf { parts -> parts.size == 2 || parts.size == 3 } ?: return Fail.invalidToken.left()
        val resource = TokenResourceType.byCode(parts[0]) ?: return Fail.invalidToken.left()
        val accessList = parts[1].toCharArray().map { access ->
            TokenAccessType.byCode(access.toString()) ?: return Fail.invalidToken.left()
        }
        val own = parts.takeIf { parts.size == 3 }?.get(2) != "n"
        TokenPermission(resource = resource, access = accessList, own = own)
    }
    return Token(
        userId = this.u,
        applicationId = this.a,
        expirationTime = this.e?.let { Instant.ofEpochMilli(it) },
        permissions = permissions,
    ).right()
}

enum class TokenAccessType(
    val code: String,
    vararg val allowedMethods: HttpMethod,
) {
    CREATE("c", HttpMethod.POST),
    READ("r", HttpMethod.GET),
    UPDATE("u", HttpMethod.PATCH, HttpMethod.PUT),
    DELETE("d", HttpMethod.DELETE);

    companion object {
        fun byCode(code: String): TokenAccessType? = TokenAccessType.values().firstOrNull { it.code == code.lowercase() }
    }
}

enum class TokenResourceType(
    val code: String,
    val path: String,
) {
    BOOK("b", "/books"),
    TODO("t", "/todos"),
    ROUTINE("r", "/routines");

    companion object {
        fun byCode(code: String): TokenResourceType? = values().firstOrNull { it.code == code.lowercase() }
    }
}

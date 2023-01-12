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
        val resource = TokenResourceType.byCode(it.r) ?: return Fail.invalidToken().left()
        val accessList = it.a.toCharArray().map { access ->
            TokenAccessType.byCode(access.toString()) ?: return Fail.invalidToken().left()
        }
        TokenPermission(
            resource = resource,
            access = accessList,
            own = it.o != "n",
        )
    }
    return Token(
        userId = this.u,
        applicationId = this.a,
        expirationTime = this.e,
        permissions = permissions,
    ).right()
}

enum class TokenAccessType(vararg val allowedMethods: HttpMethod) {
    C(HttpMethod.POST),
    R(HttpMethod.GET),
    U(HttpMethod.PATCH, HttpMethod.PUT),
    D(HttpMethod.DELETE);

    companion object {
        fun byCode(code: String): TokenAccessType? = TokenAccessType.values().firstOrNull { it.name == code.uppercase() }
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
        fun byCode(code: String): TokenResourceType? = values().firstOrNull { it.code == code }
    }
}

package io.harmny.service.user.model

import org.springframework.http.HttpMethod

data class TokenPermission(
    val resource: TokenResourceType,
    val access: List<TokenAccessType>,
    val own: Boolean = true,
)

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
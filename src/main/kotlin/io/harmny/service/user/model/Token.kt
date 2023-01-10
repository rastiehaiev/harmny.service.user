package io.harmny.service.user.model

import java.time.Instant

data class Token(
    val userId: String,
    val applicationId: String? = null,
    val master: Boolean,
    val permissions: List<TokenPermission> = emptyList(),
    val expirationTime: Instant? = null,
)

data class TokenPermission(
    val resource: String,
    val access: String,
    val own: Boolean = true,
)

enum class TokenAccessType {
    R, W;

    companion object {
        fun byCode(code: String): TokenAccessType? = TokenAccessType.values().firstOrNull { it.name == code.uppercase() }
    }
}

enum class TokenResourceType(
    private val code: String,
    val path: String,
) {
    BOOK("b", "/books"),
    TODO("t", "/todos"),
    ROUTINE("r", "/routines");

    companion object {
        fun byCode(code: String): TokenResourceType? = values().firstOrNull { it.code == code }
    }
}

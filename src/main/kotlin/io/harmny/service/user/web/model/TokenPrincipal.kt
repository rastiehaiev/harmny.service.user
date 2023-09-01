package io.harmny.service.user.web.model

import org.springframework.http.HttpMethod
import org.springframework.security.core.authority.SimpleGrantedAuthority

data class TokenPrincipal(
    val id: String?,
    val userId: String,
    val applicationId: String? = null,
    val permissions: List<TokenPermission> = emptyList(),
    val expirationTime: Long? = null,
    val refresh: Boolean? = null,
)

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
        fun byCode(code: String): TokenAccessType? = TokenAccessType.values().find { it.code == code.lowercase() }
    }
}

enum class TokenResourceType(
    val code: String,
    val path: String,
) {
    BOOK("b", "/books"),
    TODO("a", "/activities"),
    ROUTINE("r", "/routines");

    companion object {
        fun byCode(code: String): TokenResourceType? = values().find { it.code == code.lowercase() }
    }
}

fun TokenPrincipal.getAuthorities(): List<SimpleGrantedAuthority> {
    return if (this.applicationId == null) {
        listOf(SimpleGrantedAuthority("ROLE_USER"))
    } else emptyList()
}

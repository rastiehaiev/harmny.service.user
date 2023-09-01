package io.harmny.service.user.web.model

import io.harmny.service.user.model.dto.User
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

class UserPrincipal(
    val id: String,
    val email: String,
    private val password: String,
    private val authorities: List<SimpleGrantedAuthority>,
    private val attributes: Map<String, Any>,
) : OAuth2User, UserDetails {

    companion object {
        fun create(user: User, attributes: Map<String, Any> = emptyMap()): UserPrincipal {
            return UserPrincipal(
                user.id,
                user.email,
                password = "",
                authorities = listOf(SimpleGrantedAuthority("ROLE_USER")),
                attributes,
            )
        }
    }

    override fun getName() = id
    override fun getUsername() = email
    override fun getPassword() = password
    override fun getAttributes() = attributes
    override fun getAuthorities() = authorities
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
}

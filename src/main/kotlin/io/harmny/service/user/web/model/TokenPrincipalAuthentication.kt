package io.harmny.service.user.web.model

import org.springframework.security.authentication.AbstractAuthenticationToken

class TokenPrincipalAuthentication(
    private val tokenPrincipal: TokenPrincipal,
) : AbstractAuthenticationToken(tokenPrincipal.getAuthorities()) {

    override fun getPrincipal() = tokenPrincipal
    override fun getCredentials() = tokenPrincipal.userId
    override fun isAuthenticated() = true
}

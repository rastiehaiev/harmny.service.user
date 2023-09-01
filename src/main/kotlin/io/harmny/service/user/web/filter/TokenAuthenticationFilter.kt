package io.harmny.service.user.web.filter

import io.harmny.service.user.web.instruments.TokenProvider
import io.harmny.service.user.web.model.TokenPrincipalAuthentication
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TokenAuthenticationFilter(
    private val tokenProvider: TokenProvider,
) : OncePerRequestFilter() {

    companion object {
        private val log = LoggerFactory.getLogger(TokenAuthenticationFilter::class.java)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val tokenString = getTokenFromRequest(request)
            if (!tokenString.isNullOrBlank()) {
                tokenProvider.parseToken(tokenString).orNull()?.takeIf { it.refresh != true }?.also { tokenPrincipal ->
                    val authentication = TokenPrincipalAuthentication(tokenPrincipal)
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (ex: Exception) {
            log.error("Could not set authentication in security context.", ex)
        }
        filterChain.doFilter(request, response)
    }

    private fun getTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (!bearerToken.isNullOrBlank() && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7, bearerToken.length).trim()
        } else null
    }
}

package io.harmny.service.user.web.handler

import arrow.core.Either
import arrow.core.right
import io.harmny.service.user.model.Fail
import io.harmny.service.user.properties.HarmnyUserServiceProperties
import io.harmny.service.user.service.UserService
import io.harmny.service.user.utils.ifLeft
import io.harmny.service.user.web.exception.AuthenticationFailedException
import io.harmny.service.user.web.instruments.TokenProvider
import io.harmny.service.user.web.model.TokenPrincipal
import io.harmny.service.user.web.model.UserPrincipal
import io.harmny.service.user.web.repository.HttpCookieOAuth2AuthorizationRequestRepository
import io.harmny.service.user.web.repository.HttpCookieOAuth2AuthorizationRequestRepository.Companion.REDIRECT_URI_PARAM_COOKIE_NAME
import io.harmny.service.user.web.utils.CookieUtils.getCookie
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.Date
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class OAuth2AuthenticationSuccessHandler(
    private val properties: HarmnyUserServiceProperties,
    private val userService: UserService,
    private val tokenProvider: TokenProvider,
    private val httpCookieOAuth2AuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
) : SimpleUrlAuthenticationSuccessHandler() {

    companion object {
        private val log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler::class.java)
    }

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val targetUrl = determineTargetUrl(request, response, authentication)
        if (response.isCommitted) {
            log.debug("Response has already been committed. Unable to redirect to $targetUrl.")
        } else {
            clearAuthenticationAttributes(request, response)
            redirectStrategy.sendRedirect(request, response, targetUrl)
        }
    }

    override fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ): String {
        val targetUrl = getTargetUrl(request).ifLeft { throw AuthenticationFailedException(it) }
        val principal = authentication.principal as UserPrincipal
        val (token, tokenExpiration) = tokenProvider.createToken(principal)
        val refreshToken = generateRefreshToken(principal, tokenExpiration)
        return UriComponentsBuilder.fromUriString(targetUrl)
            .queryParam("token", token)
            .queryParam("refresh-token", refreshToken)
            .build().toUriString()
    }

    private fun getTargetUrl(request: HttpServletRequest): Either<Fail, String> {
        val redirectUri = getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)?.value
        return if (redirectUri != null && !isAuthorizedRedirectUri(redirectUri)) {
            Fail.authentication("unauthorized.redirect.uri")
        } else {
            (redirectUri ?: defaultTargetUrl).right()
        }
    }

    private fun isAuthorizedRedirectUri(uri: String): Boolean {
        val clientRedirectUri = URI.create(uri)
        return properties.oAuth2.authorizedRedirectUris
            .any { authorizedRedirectUri ->
                // Only validate host and port. Let the clients use different paths if they want to
                val authorizedURI = URI.create(authorizedRedirectUri)
                authorizedURI.host.equals(clientRedirectUri.host, ignoreCase = true)
                        && authorizedURI.port == clientRedirectUri.port
            }
    }

    private fun clearAuthenticationAttributes(request: HttpServletRequest, response: HttpServletResponse) {
        super.clearAuthenticationAttributes(request)
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response)
    }

    private fun generateRefreshToken(principal: UserPrincipal, tokenExpiration: Date): String {
        val refreshTokenId = userService.rotateRefreshTokenId(principal.id).ifLeft {
            throw AuthenticationFailedException(it)
        }
        val refreshTokenPrincipal = TokenPrincipal(
            id = refreshTokenId,
            userId = principal.id,
            expirationTime = tokenExpiration.time + properties.auth.refreshTokenExpirationDeltaMs,
            refresh = true,
        )
        return tokenProvider.createToken(refreshTokenPrincipal).token
    }
}

package io.harmny.service.user.web.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.harmny.service.user.model.toErrorResponse
import io.harmny.service.user.web.exception.AuthenticationFailedException
import io.harmny.service.user.web.repository.HttpCookieOAuth2AuthorizationRequestRepository
import io.harmny.service.user.web.repository.HttpCookieOAuth2AuthorizationRequestRepository.Companion.REDIRECT_URI_PARAM_COOKIE_NAME
import io.harmny.service.user.web.utils.CookieUtils.getCookie
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class OAuth2AuthenticationFailureHandler(
    private val objectMapper: ObjectMapper,
    private val httpCookieOAuth2AuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
) : SimpleUrlAuthenticationFailureHandler() {

    companion object {
        private val log = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler::class.java)
    }

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        val redirectUrlFromCookie = getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)?.value ?: "/"
        val targetUrl = UriComponentsBuilder.fromUriString(redirectUrlFromCookie)
            .queryParam("error", getErrorMessage(exception))
            .build()
            .toUriString()

        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun getErrorMessage(exception: AuthenticationException): String {
        return if (exception is AuthenticationFailedException) {
            objectMapper.writeValueAsString(exception.fail.toErrorResponse())
        } else {
            log.error("Authentication unexpectedly failed. Reason: ${exception.message}", exception)
            exception.localizedMessage ?: "Unknown error."
        }
    }
}

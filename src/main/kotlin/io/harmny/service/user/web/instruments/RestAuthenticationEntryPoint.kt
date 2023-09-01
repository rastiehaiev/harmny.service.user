package io.harmny.service.user.web.instruments

import com.fasterxml.jackson.databind.ObjectMapper
import io.harmny.service.user.model.Fail
import io.harmny.service.user.model.Fails
import io.harmny.service.user.model.toErrorResponseEntity
import io.harmny.service.user.web.exception.AuthenticationFailedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RestAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {

    companion object {
        private val log = LoggerFactory.getLogger(RestAuthenticationEntryPoint::class.java)
    }

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        log.warn("Failed to authenticate request: ${request.method}:${request.requestURL}.")
        when (exception) {
            is AuthenticationFailedException -> response.sendError(exception.fail)
            is InsufficientAuthenticationException -> response.sendError(Fails.authentication("insufficient"))
            else -> {
                log.error("Responding with unauthorized error. Reason: {}", exception.message, exception)
                response.sendError(Fails.authentication("failed"))
            }
        }
    }

    private fun HttpServletResponse.sendError(fail: Fail) {
        val responseEntity = fail.toErrorResponseEntity()
        val body = responseEntity.body.let { objectMapper.writeValueAsString(it) }

        addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        addHeader("X-Auth-Failed-Error-Code", fail.type)
        status = responseEntity.statusCodeValue
        writer.write(body)
        writer.flush()
    }
}
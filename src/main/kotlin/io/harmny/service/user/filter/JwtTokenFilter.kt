package io.harmny.service.user.filter

import com.fasterxml.jackson.databind.ObjectMapper
import io.harmny.service.user.model.toErrorResponseEntity
import io.harmny.service.user.service.AuthorizationService
import io.harmny.service.user.utils.ifLeft
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtTokenFilter(
    private val objectMapper: ObjectMapper,
    private val authorizationService: AuthorizationService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authorizationToken = request.getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith("Bearer ") }
            ?.split(" ")
            ?.takeIf { it.size == 2 }
            ?.takeIf { it[0].equals("Bearer", ignoreCase = true) }
            ?.get(1)
            ?.trim()

        if (request.pathInfo == "/validation") {
            val originalUrl = request.getHeader("X-Original-URI")
            val originalMethod = request.getHeader("X-Original-Method")
            if (authorizationToken == null || originalMethod == null || originalUrl == null) {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
            } else {
                authorizationService.validate(authorizationToken, originalMethod, originalUrl).map {
                    filterChain.doFilter(request, response)
                }.ifLeft {
                    val errorResponseEntity = it.toErrorResponseEntity()
                    response.contentType = "application/json"
                    response.characterEncoding = "UTF-8"
                    response.status = errorResponseEntity.statusCode.value()
                    val out = response.writer
                    out.print(objectMapper.writeValueAsString(errorResponseEntity.body))
                    out.flush()
                }
            }
        } else {
            authorizationToken?.let { token ->
                authorizationService.findActiveUserId(token).map { userId ->
                    SecurityContextHolder.getContext().authentication = UserIdAuthenticationToken(userId)
                }
            }
            filterChain.doFilter(request, response)
        }
    }
}

class UserIdAuthenticationToken(val userId: String) : AbstractAuthenticationToken(emptyList()) {
    override fun getCredentials(): Any = userId
    override fun getPrincipal(): Any = userId
}

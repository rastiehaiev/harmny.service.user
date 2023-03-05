package io.harmny.service.user.endpoints

import io.harmny.service.user.model.toErrorResponseEntity
import io.harmny.service.user.response.ValidityResponse
import io.harmny.service.user.service.AuthorizationService
import io.harmny.service.user.utils.ifLeft
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/validation"])
class ValidationEndpoint(private val authorizationService: AuthorizationService) {

    @PostMapping
    fun validate(
        @RequestHeader("X-Token") token: String,
        @RequestHeader("X-Request-URI") requestUri: String,
        @RequestHeader("X-Request-Method") requestMethod: String,
    ): ResponseEntity<out Any> {
        return authorizationService.validate(token, requestMethod, requestUri)
            .ifLeft { return it.toErrorResponseEntity() }
            .let { ResponseEntity.ok(ValidityResponse(it)) }
    }
}

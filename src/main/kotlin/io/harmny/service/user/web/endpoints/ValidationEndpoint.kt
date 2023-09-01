package io.harmny.service.user.web.endpoints

import io.harmny.service.user.utils.ifLeft
import io.harmny.service.user.web.annotation.CurrentToken
import io.harmny.service.user.web.exception.AuthenticationFailedException
import io.harmny.service.user.web.model.TokenPrincipal
import io.harmny.service.user.web.model.response.ValidityResponse
import io.harmny.service.user.web.service.AuthorizationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/validation"])
class ValidationEndpoint(
    private val authorizationService: AuthorizationService,
) {

    @RequestMapping
    fun validate(
        @CurrentToken tokenPrincipal: TokenPrincipal?,
        @RequestHeader("X-Original-URI") originalUri: String,
        @RequestHeader("X-Original-Method") originalMethod: String,
    ): ResponseEntity<ValidityResponse> {
        return authorizationService.validate(tokenPrincipal, originalMethod, originalUri)
            .map { ResponseEntity.ok(ValidityResponse(it)) }
            .ifLeft { throw AuthenticationFailedException(it) }
    }
}

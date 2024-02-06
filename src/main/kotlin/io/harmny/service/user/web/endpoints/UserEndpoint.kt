package io.harmny.service.user.web.endpoints

import io.harmny.service.user.model.Fails
import io.harmny.service.user.model.toDto
import io.harmny.service.user.model.toErrorResponseEntity
import io.harmny.service.user.service.UserService
import io.harmny.service.user.utils.ifLeft
import io.harmny.service.user.web.annotation.CurrentToken
import io.harmny.service.user.web.model.TokenPrincipal
import io.harmny.service.user.web.model.request.UserUpdateRequest
import io.harmny.service.user.web.model.response.TokenResponse
import io.harmny.service.user.web.service.AuthorizationService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/users"])
class UserEndpoint(
    private val userService: UserService,
    private val authorizationService: AuthorizationService,
) {

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    fun getUser(@CurrentToken principal: TokenPrincipal): ResponseEntity<out Any> {
        return userService.findById(principal.userId)?.let { ResponseEntity.ok(it.toDto()) }
            ?: return Fails.resource("user.not.found").toErrorResponseEntity()
    }

    @PutMapping
    @PreAuthorize("hasRole('USER')")
    fun updateUser(
        @CurrentToken principal: TokenPrincipal,
        @RequestBody request: UserUpdateRequest,
    ): ResponseEntity<out Any> {
        return userService.update(principal.userId, request)
            .ifLeft { return it.toErrorResponseEntity() }
            .let { ResponseEntity.ok(it.toDto()) }
    }

    @PostMapping("/master-token")
    @PreAuthorize("hasRole('USER')")
    fun requestMasterToken(
        @RequestHeader("user-agent") userAgent: String?,
        @CurrentToken principal: TokenPrincipal,
    ): ResponseEntity<out Any> {
        return authorizationService.requestMasterToken(principal.userId, userAgent)
            .ifLeft { return it.toErrorResponseEntity() }
            .let { ResponseEntity.ok(TokenResponse(it)) }
    }
}

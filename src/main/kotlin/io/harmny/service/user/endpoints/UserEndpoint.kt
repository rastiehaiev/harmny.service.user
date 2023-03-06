package io.harmny.service.user.endpoints

import arrow.core.flatMap
import arrow.core.right
import io.harmny.service.user.model.toErrorResponseEntity
import io.harmny.service.user.request.UserCreateRequest
import io.harmny.service.user.request.UserSignInRequest
import io.harmny.service.user.request.UserUpdateRequest
import io.harmny.service.user.response.TokenResponse
import io.harmny.service.user.service.AuthorizationService
import io.harmny.service.user.service.UserService
import io.harmny.service.user.utils.ifLeft
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/users"])
class UserEndpoint(
    private val userService: UserService,
    private val authorizationService: AuthorizationService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(
        @RequestBody request: UserCreateRequest,
    ): ResponseEntity<out Any> {
        return userService.create(request)
            .ifLeft { return it.toErrorResponseEntity() }
            .let { ResponseEntity.ok(it) }
    }

    @PutMapping
    fun updateUser(
        @RequestBody request: UserUpdateRequest,
    ): ResponseEntity<out Any> {
        return authorizationService.getCurrentUserId()
            .flatMap { userId -> userService.update(userId, request).right() }
            .ifLeft { return it.toErrorResponseEntity() }
            .let { ResponseEntity.ok(it) }
    }

    @PostMapping("/master-token")
    fun requestMasterToken(): ResponseEntity<out Any> {
        return authorizationService.getCurrentUserId()
            .flatMap {userId -> authorizationService.generateMasterToken(userId) }
            .ifLeft { return it.toErrorResponseEntity() }
            .let { ResponseEntity.ok(TokenResponse(it)) }
    }

    @PostMapping("/signin")
    fun signIn(
        @RequestBody request: UserSignInRequest,
    ): ResponseEntity<out Any> {
        return authorizationService.signIn(request.email, request.password)
            .ifLeft { return it.toErrorResponseEntity() }
            .let { ResponseEntity.ok(TokenResponse(it)) }
    }
}

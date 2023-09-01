package io.harmny.service.user.web.endpoints

import io.harmny.service.user.model.AuthProvider
import io.harmny.service.user.model.toErrorResponseEntity
import io.harmny.service.user.service.CreateUserRequest
import io.harmny.service.user.service.UserService
import io.harmny.service.user.utils.ifLeft
import io.harmny.service.user.web.model.request.RefreshTokenRequest
import io.harmny.service.user.web.model.request.UserCreateRequest
import io.harmny.service.user.web.model.request.UserSignInRequest
import io.harmny.service.user.web.model.response.TokenResponse
import io.harmny.service.user.web.service.AuthorizationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/auth"])
class AuthEndpoint(
    private val userService: UserService,
    private val authorizationService: AuthorizationService,
) {

    @PostMapping("/sign-in")
    fun signIn(
        @RequestBody request: UserSignInRequest,
    ): ResponseEntity<out Any> {
        return authorizationService.signIn(request.email, request.password)
            .ifLeft { return it.toErrorResponseEntity() }
            .let { (token, refreshToken) -> ResponseEntity.ok(TokenResponse(token, refreshToken)) }
    }

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(
        @RequestBody request: UserCreateRequest,
    ): ResponseEntity<out Any> {
        val createUserRequest = CreateUserRequest(
            request.firstName,
            request.lastName,
            request.email,
            authProvider = AuthProvider.local,
            request.password,
        )
        return userService.create(createUserRequest)
            .ifLeft { return it.toErrorResponseEntity() }
            .let { ResponseEntity.ok(it) }
    }

    @PostMapping("/refresh-token")
    fun refreshToken(request: RefreshTokenRequest): ResponseEntity<out Any> {
        return authorizationService.refreshToken(request)
            .ifLeft { return it.toErrorResponseEntity() }
            .let { (token, refreshToken) -> ResponseEntity.ok(TokenResponse(token, refreshToken)) }
    }
}

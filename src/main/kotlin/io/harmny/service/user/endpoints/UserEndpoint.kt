package io.harmny.service.user.endpoints

import arrow.core.flatMap
import arrow.core.right
import io.harmny.service.user.model.User
import io.harmny.service.user.model.toErrorResponse
import io.harmny.service.user.request.UserCreateRequest
import io.harmny.service.user.request.UserSignInRequest
import io.harmny.service.user.request.UserUpdateRequest
import io.harmny.service.user.response.TokenResponse
import io.harmny.service.user.response.ValidityResponse
import io.harmny.service.user.service.TokenService
import io.harmny.service.user.service.UserService
import org.springframework.http.ResponseEntity
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
    private val tokenService: TokenService,
) {

    @PostMapping
    fun createUser(request: UserCreateRequest): User {
        return userService.create(request)
    }

    @PutMapping
    fun updateUser(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: UserUpdateRequest,
    ): ResponseEntity<Any> {
        return tokenService.findActiveUserIdByMasterToken(token)
            .flatMap { userId -> userService.update(userId, request).right() }
            .fold(
                { fail -> ResponseEntity.status(fail.statusCode).body(fail.toErrorResponse()) },
                { user -> ResponseEntity.ok(user) },
            )
    }

    @PostMapping("/signin")
    fun signIn(@RequestBody request: UserSignInRequest): ResponseEntity<Any> {
        return tokenService.signIn(request.email, request.password)
            .fold(
                { fail -> ResponseEntity.status(fail.statusCode).body(fail.toErrorResponse()) },
                { token -> ResponseEntity.ok(TokenResponse(token)) },
            )
    }

    @PostMapping("/validation")
    fun validate(
        @RequestHeader("Authorization") token: String,
        @RequestHeader("Request-URI") requestUri: String,
        @RequestHeader("Request-Method") requestMethod: String,
    ): ResponseEntity<Any> {
        return tokenService.validate(token, requestMethod, requestUri)
            .fold(
                { fail -> ResponseEntity.status(fail.statusCode).body(fail.toErrorResponse()) },
                { validity -> ResponseEntity.ok(ValidityResponse(validity)) },
            )
    }
}

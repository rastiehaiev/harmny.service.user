package io.harmny.service.user.endpoints

import arrow.core.flatMap
import io.harmny.service.user.model.toErrorResponseEntity
import io.harmny.service.user.request.ApplicationCreateRequest
import io.harmny.service.user.request.ApplicationTokenRequest
import io.harmny.service.user.request.ApplicationUpdateRequest
import io.harmny.service.user.response.UnitResponse
import io.harmny.service.user.service.ApplicationService
import io.harmny.service.user.service.AuthorizationService
import io.harmny.service.user.service.TokenService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/applications"])
class ApplicationsEndpoint(
    private val tokenService: TokenService,
    private val applicationService: ApplicationService,
    private val authorizationService: AuthorizationService,
) {

    @GetMapping
    fun listApplications(
        @RequestHeader("X-Token") token: String,
    ): ResponseEntity<out Any> {
        return authorizationService.findActiveUserId(token)
            .map { userId -> applicationService.findAllByUserId(userId) }
            .fold(
                { it.toErrorResponseEntity() },
                { ResponseEntity.ok(it) },
            )
    }

    @PostMapping
    fun createApplication(
        @RequestHeader("X-Token") token: String,
        @RequestBody request: ApplicationCreateRequest,
    ): ResponseEntity<out Any> {
        return authorizationService.findActiveUserId(token)
            .map { userId -> applicationService.create(userId, request) }
            .fold(
                { it.toErrorResponseEntity() },
                { ResponseEntity.ok(it) },
            )
    }

    @PutMapping("/{application_id}")
    fun updateApplication(
        @RequestHeader("X-Token") token: String,
        @PathVariable("application_id") applicationId: String,
        @RequestBody request: ApplicationUpdateRequest,
    ): ResponseEntity<out Any> {
        return authorizationService.findActiveUserId(token)
            .map { userId -> applicationService.update(userId, applicationId, request) }
            .fold(
                { it.toErrorResponseEntity() },
                { ResponseEntity.ok(it) },
            )
    }

    @DeleteMapping("/{application_id}")
    fun deleteApplication(
        @RequestHeader("X-Token") token: String,
        @PathVariable("application_id") applicationId: String,
    ): ResponseEntity<out Any> {
        return authorizationService.findActiveUserId(token)
            .map { userId -> applicationService.delete(userId, applicationId) }
            .fold(
                { it.toErrorResponseEntity() },
                { ResponseEntity.ok(it) },
            )
    }

    @PostMapping("/{application_id}/tokens")
    fun createApplicationToken(
        @RequestHeader("X-Token") token: String,
        @PathVariable("application_id") applicationId: String,
        @RequestBody request: ApplicationTokenRequest,
    ): ResponseEntity<out Any> {
        return authorizationService.findActiveUserId(token)
            .flatMap { userId -> tokenService.create(userId, applicationId, request) }
            .fold(
                { it.toErrorResponseEntity() },
                { tokenDto ->
                    val applicationToken = authorizationService.toJwtToken(tokenDto)
                    ResponseEntity.ok(tokenDto.copy(token = applicationToken))
                },
            )
    }

    @GetMapping("/{application_id}/tokens")
    fun listApplicationTokens(
        @RequestHeader("X-Token") token: String,
        @PathVariable("application_id") applicationId: String,
    ): ResponseEntity<out Any> {
        return authorizationService.findActiveUserId(token)
            .map { userId -> tokenService.list(userId, applicationId) }
            .fold(
                { it.toErrorResponseEntity() },
                { ResponseEntity.ok(it) },
            )
    }

    @DeleteMapping("/{application_id}/tokens/{token_id}")
    fun deleteApplicationToken(
        @RequestHeader("X-Token") token: String,
        @PathVariable("application_id") applicationId: String,
        @PathVariable("token_id") tokenId: String,
    ): ResponseEntity<out Any> {
        return authorizationService.findActiveUserId(token)
            .flatMap { userId -> tokenService.delete(userId, applicationId, tokenId) }
            .fold(
                { it.toErrorResponseEntity() },
                { ResponseEntity.ok(UnitResponse(it)) },
            )
    }
}

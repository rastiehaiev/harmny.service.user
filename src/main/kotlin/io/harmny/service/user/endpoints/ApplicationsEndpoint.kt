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
    fun listApplications(): ResponseEntity<out Any> {
        return authorizationService.getCurrentUserId()
            .map { userId -> applicationService.findAllByUserId(userId) }
            .fold(
                { it.toErrorResponseEntity() },
                { ResponseEntity.ok(it) },
            )
    }

    @PostMapping
    fun createApplication(
        @RequestBody request: ApplicationCreateRequest,
    ): ResponseEntity<out Any> {
        return authorizationService.getCurrentUserId()
            .flatMap { userId -> applicationService.create(userId, request) }
            .fold(
                { it.toErrorResponseEntity() },
                { ResponseEntity.ok(it) },
            )
    }

    @PutMapping("/{application_id}")
    fun updateApplication(
        @PathVariable("application_id") applicationId: String,
        @RequestBody request: ApplicationUpdateRequest,
    ): ResponseEntity<out Any> {
        return authorizationService.getCurrentUserId()
            .flatMap { userId -> applicationService.update(userId, applicationId, request) }
            .fold(
                { it.toErrorResponseEntity() },
                { ResponseEntity.ok(it) },
            )
    }

    @DeleteMapping("/{application_id}")
    fun deleteApplication(
        @PathVariable("application_id") applicationId: String,
    ): ResponseEntity<out Any> {
        return authorizationService.getCurrentUserId()
            .flatMap { userId -> applicationService.delete(userId, applicationId) }
            .fold(
                { it.toErrorResponseEntity() },
                { ResponseEntity.ok(it) },
            )
    }

    @PostMapping("/{application_id}/tokens")
    fun createApplicationToken(
        @PathVariable("application_id") applicationId: String,
        @RequestBody request: ApplicationTokenRequest,
    ): ResponseEntity<out Any> {
        return authorizationService.getCurrentUserId()
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
        @PathVariable("application_id") applicationId: String,
    ): ResponseEntity<out Any> {
        return authorizationService.getCurrentUserId()
            .map { userId -> tokenService.list(userId, applicationId) }
            .fold(
                { it.toErrorResponseEntity() },
                { ResponseEntity.ok(it) },
            )
    }

    @DeleteMapping("/{application_id}/tokens/{token_id}")
    fun deleteApplicationToken(
        @PathVariable("application_id") applicationId: String,
        @PathVariable("token_id") tokenId: String,
    ): ResponseEntity<out Any> {
        return authorizationService.getCurrentUserId()
            .flatMap { userId -> tokenService.delete(userId, applicationId, tokenId) }
            .fold(
                { it.toErrorResponseEntity() },
                { ResponseEntity.ok(UnitResponse(it)) },
            )
    }
}

package io.harmny.service.user.web.endpoints

import arrow.core.flatMap
import io.harmny.service.user.model.toErrorResponseEntity
import io.harmny.service.user.web.model.request.ApplicationCreateRequest
import io.harmny.service.user.web.model.request.ApplicationTokenRequest
import io.harmny.service.user.web.model.request.ApplicationUpdateRequest
import io.harmny.service.user.web.model.response.UnitResponse
import io.harmny.service.user.service.ApplicationService
import io.harmny.service.user.web.service.AuthorizationService
import io.harmny.service.user.service.ApplicationTokenService
import io.harmny.service.user.web.annotation.CurrentToken
import io.harmny.service.user.web.model.TokenPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
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
    private val applicationTokenService: ApplicationTokenService,
    private val applicationService: ApplicationService,
    private val authorizationService: AuthorizationService,
) {

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    fun listApplications(
        @CurrentToken principal: TokenPrincipal,
    ): ResponseEntity<out Any> {
        return ResponseEntity.ok(applicationService.findAllByUserId(principal.userId))
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    fun createApplication(
        @CurrentToken principal: TokenPrincipal,
        @RequestBody request: ApplicationCreateRequest,
    ): ResponseEntity<out Any> {
        return applicationService.create(principal.userId, request).fold(
            { it.toErrorResponseEntity() },
            { ResponseEntity.ok(it) },
        )
    }

    @PutMapping("/{application_id}")
    @PreAuthorize("hasRole('USER')")
    fun updateApplication(
        @CurrentToken principal: TokenPrincipal,
        @PathVariable("application_id") applicationId: String,
        @RequestBody request: ApplicationUpdateRequest,
    ): ResponseEntity<out Any> {
        return applicationService.update(principal.userId, applicationId, request).fold(
            { it.toErrorResponseEntity() },
            { ResponseEntity.ok(it) },
        )
    }

    @DeleteMapping("/{application_id}")
    @PreAuthorize("hasRole('USER')")
    fun deleteApplication(
        @CurrentToken principal: TokenPrincipal,
        @PathVariable("application_id") applicationId: String,
    ): ResponseEntity<out Any> {
        return applicationService.delete(principal.userId, applicationId).fold(
            { it.toErrorResponseEntity() },
            { ResponseEntity.ok(it) },
        )
    }

    @PostMapping("/{application_id}/tokens")
    @PreAuthorize("hasRole('USER')")
    fun createApplicationToken(
        @CurrentToken principal: TokenPrincipal,
        @PathVariable("application_id") applicationId: String,
        @RequestBody request: ApplicationTokenRequest,
    ): ResponseEntity<out Any> {
        return authorizationService.requestApplicationToken(principal.userId, applicationId, request)
            .fold(
                { it.toErrorResponseEntity() },
                { ResponseEntity.ok(it) },
            )
    }

    @GetMapping("/{application_id}/tokens")
    @PreAuthorize("hasRole('USER')")
    fun listApplicationTokens(
        @CurrentToken principal: TokenPrincipal,
        @PathVariable("application_id") applicationId: String,
    ): ResponseEntity<out Any> {
        return ResponseEntity.ok(applicationTokenService.list(principal.userId, applicationId))
    }

    @DeleteMapping("/{application_id}/tokens/{token_id}")
    @PreAuthorize("hasRole('USER')")
    fun deleteApplicationToken(
        @CurrentToken principal: TokenPrincipal,
        @PathVariable("application_id") applicationId: String,
        @PathVariable("token_id") tokenId: String,
    ): ResponseEntity<out Any> {
        return applicationTokenService.delete(principal.userId, applicationId, tokenId)
            .fold(
                { it.toErrorResponseEntity() },
                { ResponseEntity.ok(UnitResponse(it)) },
            )
    }
}

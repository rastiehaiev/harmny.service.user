package io.harmny.service.user.endpoints

import io.harmny.service.user.model.toErrorResponse
import io.harmny.service.user.request.ApplicationCreateRequest
import io.harmny.service.user.request.ApplicationUpdateRequest
import io.harmny.service.user.service.ApplicationService
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
) {

    @GetMapping
    fun listUserApplications(
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<Any> {
        return tokenService.findActiveUserIdByMasterToken(token)
            .map { userId -> applicationService.findAllByUserId(userId) }
            .fold(
                { fail -> ResponseEntity.status(fail.statusCode).body(fail.toErrorResponse()) },
                { user -> ResponseEntity.ok(user) },
            )
    }

    @PostMapping
    fun createUserApplication(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: ApplicationCreateRequest,
    ): ResponseEntity<Any> {
        return tokenService.findActiveUserIdByMasterToken(token)
            .map { userId -> applicationService.create(userId, request) }
            .fold(
                { fail -> ResponseEntity.status(fail.statusCode).body(fail.toErrorResponse()) },
                { user -> ResponseEntity.ok(user) },
            )
    }

    @PutMapping("/{application_id}")
    fun updateUserApplication(
        @RequestHeader("Authorization") token: String,
        @PathVariable("application_id") applicationId: String,
        @RequestBody request: ApplicationUpdateRequest,
    ): ResponseEntity<Any> {
        return tokenService.findActiveUserIdByMasterToken(token)
            .map { userId -> applicationService.update(userId, applicationId, request) }
            .fold(
                { fail -> ResponseEntity.status(fail.statusCode).body(fail.toErrorResponse()) },
                { user -> ResponseEntity.ok(user) },
            )
    }

    @DeleteMapping("/{application_id}")
    fun deleteUserApplication(
        @RequestHeader("Authorization") token: String,
        @PathVariable("application_id") applicationId: String,
    ): ResponseEntity<Any> {
        return tokenService.findActiveUserIdByMasterToken(token)
            .map { userId -> applicationService.delete(userId, applicationId) }
            .fold(
                { fail -> ResponseEntity.status(fail.statusCode).body(fail.toErrorResponse()) },
                { user -> ResponseEntity.ok(user) },
            )
    }
}

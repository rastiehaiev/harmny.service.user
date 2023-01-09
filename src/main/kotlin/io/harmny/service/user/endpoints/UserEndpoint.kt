package io.harmny.service.user.endpoints

import io.harmny.service.user.model.Application
import io.harmny.service.user.model.User
import io.harmny.service.user.request.ApplicationCreateRequest
import io.harmny.service.user.request.ApplicationUpdateRequest
import io.harmny.service.user.request.UserCreateRequest
import io.harmny.service.user.request.UserUpdateRequest
import io.harmny.service.user.service.ApplicationService
import io.harmny.service.user.service.UserService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/users"])
class UserEndpoint(
    private val userService: UserService,
    private val applicationService: ApplicationService,
) {

    @PostMapping
    fun createUser(request: UserCreateRequest): User {
        return userService.create(request)
    }

    @GetMapping("/{user_id}")
    fun findUserById(@PathVariable("user_id") userId: String): User? {
        return userService.findById(userId)
    }

    @PutMapping("/{user_id}")
    fun updateUser(
        @PathVariable("user_id") userId: String,
        request: UserUpdateRequest,
    ): User? {
        return userService.update(userId, request)
    }

    @GetMapping("/{user_id}/applications")
    fun listUserApplications(@PathVariable("user_id") userId: String): List<Application> {
        return applicationService.findAllByUserId(userId)
    }

    @PostMapping("/{user_id}/applications")
    fun createUserApplication(
        @PathVariable("user_id") userId: String,
        request: ApplicationCreateRequest,
    ): Application {
        return applicationService.create(userId, request)
    }

    @PutMapping("/{user_id}/applications/{application_id}")
    fun updateUserApplication(
        @PathVariable("user_id") userId: String,
        @PathVariable("application_id") applicationId: String,
        request: ApplicationUpdateRequest,
    ): Application {
        return applicationService.update(userId, applicationId, request)
    }

    @PutMapping("/{user_id}/applications/{application_id}/token")
    fun updateUserApplication(
        @PathVariable("user_id") userId: String,
        @PathVariable("application_id") applicationId: String,
    ): Application {
        return applicationService.rotateToken(userId, applicationId)
    }

    @DeleteMapping("/{user_id}/applications/{application_id}")
    fun deleteUserApplication(
        @PathVariable("user_id") userId: String,
        @PathVariable("application_id") applicationId: String,
    ): Application {
        return applicationService.delete(userId, applicationId)
    }
}

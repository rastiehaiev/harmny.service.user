package io.harmny.service.user.service

import io.harmny.service.user.model.Application
import io.harmny.service.user.request.ApplicationCreateRequest
import io.harmny.service.user.request.ApplicationUpdateRequest
import org.springframework.stereotype.Service

@Service
class ApplicationService {

    fun findAllByUserId(userId: String): List<Application> {
        TODO()
    }

    fun create(userId: String, request: ApplicationCreateRequest): Application {
        TODO()
    }

    fun update(
        userId: String,
        applicationId: String,
        request: ApplicationUpdateRequest,
    ): Application {
        TODO()
    }

    fun delete(userId: String, applicationId: String): Application {
        TODO()
    }

    fun rotateToken(userId: String, applicationId: String): Application {
        TODO()
    }
}

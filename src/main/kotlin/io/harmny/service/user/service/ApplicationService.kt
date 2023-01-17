package io.harmny.service.user.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.harmny.service.user.model.Application
import io.harmny.service.user.model.Fail
import io.harmny.service.user.request.ApplicationCreateRequest
import io.harmny.service.user.request.ApplicationUpdateRequest
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ApplicationService {

    private val localStorage = hashMapOf<String, Application>()

    fun findAllByUserId(userId: String): List<Application> {
        return localStorage.values.filter { it.userId == userId }
    }

    fun create(userId: String, request: ApplicationCreateRequest): Application {
        val applicationId = UUID.randomUUID().toString()
        val application = Application(id = applicationId, userId = userId, name = request.name)
        localStorage[applicationId] = application
        return application
    }

    fun findById(userId: String, applicationId: String): Application? {
        return localStorage[applicationId]?.takeIf { it.userId == userId }
    }

    fun update(
        userId: String,
        applicationId: String,
        request: ApplicationUpdateRequest,
    ): Application {
        TODO()
    }

    fun delete(userId: String, applicationId: String): Either<Fail, Application> {
        localStorage[applicationId]?.takeIf { it.userId == userId }?.also {
            localStorage.remove(applicationId)
            return it.right()
        }
        return Fail.applicationNotFound.left()
    }
}

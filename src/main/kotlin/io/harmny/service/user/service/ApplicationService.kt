package io.harmny.service.user.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.harmny.service.user.entity.ApplicationEntity
import io.harmny.service.user.model.Application
import io.harmny.service.user.model.Fail
import io.harmny.service.user.repository.ApplicationRepository
import io.harmny.service.user.repository.TokenRepository
import io.harmny.service.user.request.ApplicationCreateRequest
import io.harmny.service.user.request.ApplicationUpdateRequest
import io.harmny.service.user.utils.ifLeft
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ApplicationService(
    private val tokenRepository: TokenRepository,
    private val applicationRepository: ApplicationRepository,
) {

    companion object {
        private const val MAX_APPLICATION_NAME_LENGTH = 100
    }

    fun findAllByUserId(userId: String): List<Application> {
        return applicationRepository.findAllByUserId(userId).map { it.toModel() }
    }

    fun create(userId: String, request: ApplicationCreateRequest): Either<Fail, Application> {
        val name = validateApplicationName(request.name).ifLeft { return it.left() }
        val applicationEntity = ApplicationEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = name,
        )
        return applicationRepository.save(applicationEntity).toModel().right()
    }

    fun findById(userId: String, applicationId: String): Either<Fail, Application> {
        val application = findByUserIdAndApplicationId(userId, applicationId).ifLeft { return it.left() }
        return application.toModel().right()
    }

    fun update(
        userId: String,
        applicationId: String,
        request: ApplicationUpdateRequest,
    ): Either<Fail, Application> {
        val applicationName = validateApplicationName(request.name).ifLeft { return it.left() }
        val application = findByUserIdAndApplicationId(userId, applicationId).ifLeft { return it.left() }

        application.name = applicationName
        return applicationRepository.save(application).toModel().right()
    }

    fun delete(userId: String, applicationId: String): Either<Fail, Application> {
        val application = findByUserIdAndApplicationId(userId, applicationId).ifLeft { return it.left() }

        tokenRepository.deleteAllByUserIdAndApplicationId(userId, applicationId)
        applicationRepository.delete(application)
        return application.toModel().right()
    }

    private fun findByUserIdAndApplicationId(userId: String, applicationId: String): Either<Fail, ApplicationEntity> {
        return applicationRepository.findByIdOrNull(applicationId)
            ?.takeIf { it.userId == userId }
            ?.right()
            ?: return Fail.resource(key = "application.not.found")
    }

    private fun validateApplicationName(name: String): Either<Fail, String> {
        return name.trim().let {
            if (it.isEmpty()) {
                Fail.input(key = "application.name.empty")
            } else if (it.length > MAX_APPLICATION_NAME_LENGTH) {
                Fail.input(
                    key = "application.name.too.long",
                    properties = mapOf("MAX_APPLICATION_NAME_LENGTH" to MAX_APPLICATION_NAME_LENGTH.toString()),
                )
            } else {
                it.right()
            }
        }
    }
}

private fun ApplicationEntity.toModel(): Application {
    return Application(id, userId, name)
}

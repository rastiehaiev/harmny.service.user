package io.harmny.service.user.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.harmny.service.user.entity.ApplicationTokenEntity
import io.harmny.service.user.model.Fail
import io.harmny.service.user.model.dto.ApplicationToken
import io.harmny.service.user.repository.ApplicationTokenRepository
import io.harmny.service.user.utils.ifLeft
import io.harmny.service.user.web.model.request.ApplicationTokenRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class ApplicationTokenService(
    private val applicationService: ApplicationService,
    private val applicationTokenRepository: ApplicationTokenRepository,
) {

    fun create(
        userId: String,
        applicationId: String,
        request: ApplicationTokenRequest,
    ): Either<Fail, ApplicationToken> {
        applicationService.findById(userId, applicationId).ifLeft { return it.left() }

        val expirationTime = request.expirationTime?.let { Instant.ofEpochMilli(it) }?.also {
            if (it.isBefore(Instant.now().plusSeconds(5))) {
                return Fail.input("expiration.time.invalid")
            }
        }
        val tokenEntity = ApplicationTokenEntity(
            id = UUID.randomUUID().toString(),
            userId,
            applicationId,
            request.permissions,
            expirationTime,
        )
        return applicationTokenRepository.save(tokenEntity).toApplicationToken().right()
    }

    fun list(userId: String, applicationId: String): List<ApplicationToken> {
        return applicationTokenRepository.findAllByUserIdAndApplicationId(userId, applicationId)
            .map { it.toApplicationToken() }
    }

    fun get(userId: String, applicationId: String, tokenId: String): Either<Fail, ApplicationToken> {
        return find(userId, applicationId, tokenId)
            ?.toApplicationToken()
            ?.right()
            ?: Fail.resource(key = "token.not.found")
    }

    fun delete(userId: String, applicationId: String, tokenId: String): Either<Fail, Boolean> {
        val tokenEntity = find(userId, applicationId, tokenId) ?: return Fail.resource(key = "token.not.found")
        applicationTokenRepository.delete(tokenEntity)
        return true.right()
    }

    private fun find(userId: String, applicationId: String, tokenId: String): ApplicationTokenEntity? {
        return applicationTokenRepository.findByIdOrNull(tokenId)
            ?.takeIf { it.userId == userId && it.applicationId == applicationId }
    }

    private fun ApplicationTokenEntity.toApplicationToken(): ApplicationToken {
        return ApplicationToken(id, userId, applicationId, permissions, expirationTime)
    }
}

package io.harmny.service.user.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.harmny.service.user.entity.TokenEntity
import io.harmny.service.user.model.ApplicationTokenDto
import io.harmny.service.user.model.Fail
import io.harmny.service.user.repository.TokenRepository
import io.harmny.service.user.request.ApplicationTokenRequest
import io.harmny.service.user.utils.ifLeft
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class TokenService(
    private val tokenRepository: TokenRepository,
    private val applicationService: ApplicationService,
) {

    fun create(userId: String, applicationId: String, request: ApplicationTokenRequest): Either<Fail, ApplicationTokenDto> {
        applicationService.findById(userId, applicationId).ifLeft { return it.left() }

        val expirationTime = request.expirationTime?.let { Instant.ofEpochMilli(it) }?.also {
            if (it.isBefore(Instant.now().plusSeconds(5))) {
                return Fail.input("expiration.time.invalid")
            }
        }
        val tokenEntity = TokenEntity(
            id = UUID.randomUUID().toString(),
            userId,
            applicationId,
            request.permissions,
            expirationTime,
        )
        return tokenRepository.save(tokenEntity).toApplicationToken().right()
    }

    fun list(userId: String, applicationId: String): List<ApplicationTokenDto> {
        return tokenRepository.findAllByUserIdAndApplicationId(userId, applicationId)
            .map { it.toApplicationToken() }
    }

    fun get(userId: String, applicationId: String, tokenId: String): Either<Fail, ApplicationTokenDto> {
        return find(userId, applicationId, tokenId)
            ?.toApplicationToken()
            ?.right()
            ?: Fail.resource(key = "token.not.found")
    }

    fun delete(userId: String, applicationId: String, tokenId: String): Either<Fail, Boolean> {
        val tokenEntity = find(userId, applicationId, tokenId) ?: return Fail.resource(key = "token.not.found")
        tokenRepository.delete(tokenEntity)
        return true.right()
    }

    private fun find(userId: String, applicationId: String, tokenId: String): TokenEntity? {
        return tokenRepository.findByIdOrNull(tokenId)
            ?.takeIf { it.userId == userId && it.applicationId == applicationId }
    }

    private fun TokenEntity.toApplicationToken(): ApplicationTokenDto {
        return ApplicationTokenDto(id, userId, applicationId, permissions, expirationTime)
    }
}

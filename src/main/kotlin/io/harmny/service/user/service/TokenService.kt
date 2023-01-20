package io.harmny.service.user.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.harmny.service.user.model.ApplicationTokenDto
import io.harmny.service.user.model.Fail
import io.harmny.service.user.request.ApplicationTokenRequest
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class TokenService(
    private val applicationService: ApplicationService,
) {

    private val tokens = HashMap<Pair<String, String>, MutableList<ApplicationTokenDto>>()

    fun create(userId: String, applicationId: String, request: ApplicationTokenRequest): Either<Fail, ApplicationTokenDto> {
        applicationService.findById(userId, applicationId)
            ?: return Fail.applicationNotFound.left()

        val expirationTime = request.expirationTime?.let { Instant.ofEpochMilli(it) }?.also {
            if (it.isBefore(Instant.now())) {
                return Fail.invalidExpirationTime.left()
            }
        }
        val tokenDto = ApplicationTokenDto(
            id = UUID.randomUUID().toString(),
            userId,
            applicationId,
            request.permissions,
            expirationTime,
        )

        tokens.computeIfAbsent(userId to applicationId) { ArrayList() }.add(tokenDto)
        return tokenDto.right()
    }

    fun list(userId: String, applicationId: String): List<ApplicationTokenDto> {
        return tokens[userId to applicationId] ?: emptyList()
    }

    fun get(userId: String, applicationId: String, tokenId: String): ApplicationTokenDto? {
        return list(userId, applicationId).firstOrNull { it.id == tokenId }
    }

    fun delete(userId: String, applicationId: String, tokenId: String): Either<Fail, Boolean> {
        tokens[userId to applicationId]?.firstOrNull { it.id == tokenId } ?: return Fail.tokenNotFound.left()
        tokens[userId to applicationId]?.removeIf { it.id == tokenId }
        return true.right()
    }
}

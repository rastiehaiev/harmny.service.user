package io.harmny.service.user.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.harmny.service.user.instruments.TokenHandler
import io.harmny.service.user.model.Fail
import io.harmny.service.user.model.FailReason
import io.harmny.service.user.model.Token
import org.springframework.stereotype.Service
import java.net.URI
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class TokenService(
    private val tokenHandler: TokenHandler,
    private val userService: UserService,
    private val applicationService: ApplicationService,
) {

    fun findActiveUserIdByMasterToken(tokenString: String): Either<Fail, String> {
        return tokenHandler.parse(tokenString)
            .flatMap { token ->
                token.takeIf { it.isMaster() }?.userId?.right() ?: Fail.resourceUnavailable.left()
            }.flatMap { userId ->
                userService.findById(userId)?.right() ?: Fail.userNotFound.left()
            }.flatMap { user ->
                user.takeIf { it.active }?.id?.right() ?: Fail.userNotActive.left()
            }
    }

    fun signIn(email: String, password: String): Either<Fail, String> {
        val user = userService.findByEmailAndPassword(email, password)
            ?: return Fail.unauthenticated(FailReason.USER_NOT_FOUND_BY_EMAIL_AND_PASSWORD).left()
        val userId = user.takeIf { it.active }?.id
            ?: return Fail.unauthorized(FailReason.USER_NOT_ACTIVE).left()

        return Token(
            userId = userId,
            expirationTime = Instant.now().plus(10, ChronoUnit.MINUTES),
        ).toJwtString().right()
    }

    fun validate(
        tokenString: String,
        method: String,
        requestUri: String,
    ): Either<Fail, Boolean> {
        return tokenHandler.parse(tokenString)
            .flatMap { token ->
                val user = userService.findById(token.userId) ?: return@flatMap Fail.userNotFound.left()
                if (!user.active) return@flatMap Fail.userNotActive.left()

                val applicationId = token.applicationId
                if (applicationId != null) {
                    val application = applicationService.findById(token.userId, applicationId)
                    if (application != null) {
                        return@flatMap Fail.unauthorized(FailReason.APPLICATION_DOES_NOT_EXIST).left()
                    }
                }
                val expirationTime = token.expirationTime
                if (expirationTime != null && expirationTime.isBefore(Instant.now())) {
                    Fail.unauthenticated(FailReason.TOKEN_EXPIRED).left()
                } else if (token.isMaster()) {
                    Either.Right(true)
                } else if (token.permissions.isNotEmpty()) {

                    val requestPath = URI.create(requestUri).path
                    val (_, access, _) = token.permissions.firstOrNull { (resource) ->
                        requestPath.contains(resource.path)
                    } ?: return@flatMap Fail.unauthorized(FailReason.RESOURCE_NOT_ALLOWED).left()

                    val allowedMethods = access.flatMap { it.allowedMethods.toList() }.map { it.name }.distinct()
                    if (method.uppercase() !in allowedMethods) {
                        return@flatMap Fail.unauthorized(FailReason.WRITE_OPERATIONS_NOT_ALLOWED).left()
                    } else {
                        Either.Right(true)
                    }
                } else {
                    Either.Right(true)
                }
            }
    }

    private fun Token.toJwtString(): String {
        return tokenHandler.generate(this)
    }
}

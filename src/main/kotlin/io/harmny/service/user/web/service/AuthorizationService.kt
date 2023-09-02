package io.harmny.service.user.web.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.harmny.service.user.model.Fail
import io.harmny.service.user.properties.HarmnyUserServiceProperties
import io.harmny.service.user.service.ApplicationTokenService
import io.harmny.service.user.service.UserService
import io.harmny.service.user.utils.ifLeft
import io.harmny.service.user.web.endpoints.ValidationEndpoint
import io.harmny.service.user.web.instruments.TokenProvider
import io.harmny.service.user.web.model.TokenPermission
import io.harmny.service.user.web.model.TokenPrincipal
import io.harmny.service.user.web.model.request.ApplicationTokenRequest
import io.harmny.service.user.web.model.request.RefreshTokenRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URI
import java.time.Instant

@Service
class AuthorizationService(
    private val userService: UserService,
    private val tokenProvider: TokenProvider,
    private val properties: HarmnyUserServiceProperties,
    private val applicationTokenService: ApplicationTokenService,
) {

    companion object {
        private val log = LoggerFactory.getLogger(ValidationEndpoint::class.java)
    }

    fun requestMasterToken(userId: String): Either<Fail, String> {
        return userService.updateMasterTokenId(userId).flatMap { masterTokenId ->
            tokenProvider.createToken(TokenPrincipal(id = masterTokenId, userId = userId))
                .token
                .right()
        }
    }

    fun requestApplicationToken(
        userId: String,
        applicationId: String,
        request: ApplicationTokenRequest,
    ): Either<Fail, String> {
        return applicationTokenService.create(userId, applicationId, request).map { token ->
            val tokenPrincipal = TokenPrincipal(
                token.id,
                token.userId,
                token.id,
                token.permissions,
                token.expirationTime?.toEpochMilli(),
            )
            tokenProvider.createToken(tokenPrincipal).token
        }
    }

    fun signIn(email: String, password: String): Either<Fail, Pair<String, String>> {
        val user = userService.findByEmailAndPassword(email, password)
            ?: return Fail.authentication(key = "user.not.found.by.credentials")
        val userId = user.takeIf { it.active }?.id
            ?: return Fail.authorization(key = "user.inactive")

        return signInInternally(userId)
    }

    fun refreshToken(request: RefreshTokenRequest): Either<Fail, Pair<String, String>> {
        val tokenPrincipal = tokenProvider.parseToken(request.token).ifLeft { return it.left() }
        val user = userService.findById(tokenPrincipal.userId)
            ?: return Fail.authentication(key = "user.not.found.by.credentials")
        val userId = user.takeIf { it.active }?.id
            ?: return Fail.authorization(key = "user.inactive")

        if (tokenPrincipal.id == null || user.refreshTokenId != tokenPrincipal.id) {
            return Fail.authorization(key = "refresh.token.expired")
        }
        return signInInternally(userId)
    }

    private fun signInInternally(userId: String): Either<Fail, Pair<String, String>> {
        val refreshTokenId = userService.rotateRefreshTokenId(userId).ifLeft { return it.left() }
        val tokenExpirationTime = Instant.now().plusMillis(properties.auth.tokenExpirationMsUi).toEpochMilli()

        val tokenPrincipal = TokenPrincipal(
            id = null,
            userId = userId,
            expirationTime = tokenExpirationTime,
        )
        val refreshTokenPrincipal = TokenPrincipal(
            id = refreshTokenId,
            userId = userId,
            expirationTime = tokenExpirationTime + properties.auth.refreshTokenExpirationDeltaMs,
            refresh = true,
        )

        return Pair(
            tokenProvider.createToken(tokenPrincipal).token,
            tokenProvider.createToken(refreshTokenPrincipal).token,
        ).right()
    }

    fun validate(
        token: TokenPrincipal?,
        method: String,
        requestUri: String,
    ): Either<Fail, Boolean> {
        if (token == null) {
            return Fail.authentication(key = "token.missing")
        }

        val expirationTime = token.expirationTime?.let { Instant.ofEpochMilli(it) }
        if (expirationTime != null && expirationTime.isBefore(Instant.now())) {
            return Fail.authorization(key = "token.expired")
        }

        val user = userService.findById(token.userId) ?: return Fail.authorization("user.not.found")
        if (!user.active) return Fail.authorization("user.inactive")

        val tokenId = token.id
        val applicationId = token.applicationId
        return if (applicationId == null) {
            if (tokenId != null) {
                // handle master tokens
                if (tokenId == user.masterTokenId) {
                    Either.Right(true)
                } else {
                    Fail.authentication("token.master.invalid")
                }
            } else {
                // handle user's 'UI' token - valid until expiration time
                Either.Right(true)
            }
        } else {
            // handle application tokens
            checkTokenPermissions(requestUri, method, token.permissions).flatMap {
                checkApplicationTokenExists(user.id, applicationId, token)
            }
        }
    }

    private fun checkTokenPermissions(
        requestUri: String,
        method: String,
        permissions: List<TokenPermission>,
    ): Either<Fail, Boolean> {
        val requestPath = URI.create(requestUri).path
        val (_, access, _) = permissions.firstOrNull { (resource) ->
            requestPath.contains(resource.path)
        } ?: return Fail.authorization("resource.not.available")

        val allowedMethods = access.flatMap { it.allowedMethods.toList() }.map { it.name }.distinct()
        return if (method.uppercase() !in allowedMethods) {
            Fail.authorization("operation.not.allowed")
        } else {
            true.right()
        }
    }

    private fun checkApplicationTokenExists(
        userId: String,
        applicationId: String,
        token: TokenPrincipal,
    ): Either<Fail, Boolean> {
        val tokenId = token.id ?: return Fail.authorization(key = "token.invalid")
        applicationTokenService.get(userId, applicationId, tokenId).ifLeft { return it.left() }
        return Either.Right(true)
    }
}

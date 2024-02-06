package io.harmny.service.user.web.instruments

import arrow.core.Either
import arrow.core.right
import com.google.auth.oauth2.TokenVerifier
import io.harmny.service.user.model.Fail
import io.harmny.service.user.properties.HarmnyUserServiceProperties
import io.harmny.service.user.web.model.GoogleOAuth2UserInfo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class IosTokenHandler(
    properties: HarmnyUserServiceProperties,
) {

    companion object {
        private val log = LoggerFactory.getLogger(IosTokenHandler::class.java)
    }

    private val tokenVerifier = TokenVerifier.newBuilder()
        .setAudience(properties.oAuth2.iosAppClientId)
        .build()

    fun verifyAndGet(token: String): Either<Fail, GoogleOAuth2UserInfo> {
        return try {
            val jsonWebSignature = tokenVerifier.verify(token)
            GoogleOAuth2UserInfo(jsonWebSignature.payload.toMap()).right()
        } catch (e: Exception) {
            log.error("Failed to verify ID token. Reason: ${e.message}")
            Fail.authentication(key = "id.token.invalid")
        }
    }
}

package io.harmny.service.user.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "harmny")
data class HarmnyUserServiceProperties(
    val cors: Cors,
    val auth: Auth,
    val oAuth2: OAuth2,
)

data class Cors(
    val allowedOrigins: String,
)

data class Auth(
    val tokenSecret: String,
    val tokenExpirationMsDefault: Long,
    val tokenExpirationMsUi: Long,
    val refreshTokenExpirationDeltaMs: Long,
)

data class OAuth2(
    val authorizedRedirectUris: List<String> = emptyList(),
)

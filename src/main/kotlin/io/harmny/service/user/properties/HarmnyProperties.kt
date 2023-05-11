package io.harmny.service.user.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "harmny")
data class HarmnyProperties(
    val jwtKey: String,
)

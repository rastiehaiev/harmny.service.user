package io.harmny.service.user.entity

import io.harmny.service.user.model.TokenPermission
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("tokens")
data class TokenEntity(
    val id: String,
    val userId: String,
    val applicationId: String?,
    val permissions: List<TokenPermission>,
    val expirationTime: Instant?,
)

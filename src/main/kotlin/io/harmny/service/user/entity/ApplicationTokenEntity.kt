package io.harmny.service.user.entity

import io.harmny.service.user.web.model.TokenPermission
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("application-tokens")
data class ApplicationTokenEntity(
    val id: String,
    val userId: String,
    val applicationId: String,
    val permissions: List<TokenPermission>,
    val expirationTime: Instant?,
)

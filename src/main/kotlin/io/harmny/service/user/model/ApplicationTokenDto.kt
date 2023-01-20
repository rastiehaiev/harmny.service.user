package io.harmny.service.user.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ApplicationTokenDto(
    val id: String,
    val userId: String,
    val applicationId: String? = null,
    val permissions: List<TokenPermission> = emptyList(),
    val expirationTime: Instant? = null,
    val token: String? = null,
)

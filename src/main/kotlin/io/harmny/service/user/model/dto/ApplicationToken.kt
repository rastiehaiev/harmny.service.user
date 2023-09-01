package io.harmny.service.user.model.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.harmny.service.user.web.model.TokenPermission
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ApplicationToken(
    val id: String,
    val userId: String,
    val applicationId: String,
    val permissions: List<TokenPermission> = emptyList(),
    val expirationTime: Instant? = null,
    val token: String? = null,
)

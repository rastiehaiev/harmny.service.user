package io.harmny.service.user.web.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.harmny.service.user.web.model.TokenPermission

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ApplicationTokenRequest(
    val name: String,
    val permissions: List<TokenPermission>,
    val expirationTime: Long?,
)

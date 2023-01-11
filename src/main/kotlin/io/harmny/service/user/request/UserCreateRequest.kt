package io.harmny.service.user.request

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserCreateRequest(
    val firstName: String,
    val lastName: String?,
    val email: String,
    val password: String,
)

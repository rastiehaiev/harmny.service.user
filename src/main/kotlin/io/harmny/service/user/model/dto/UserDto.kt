package io.harmny.service.user.model.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.harmny.service.user.model.AuthProvider

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserDto(
    val id: String,
    val firstName: String,
    val lastName: String?,
    val active: Boolean = false,
    val email: String,
    val authProvider: AuthProvider,
    val profilePhotoUrl: String?,
)

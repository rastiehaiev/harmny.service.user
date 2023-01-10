package io.harmny.service.user.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val active: Boolean = true,
    val email: String,
    val password: String,
)

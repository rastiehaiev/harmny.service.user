package io.harmny.service.user.model

import io.harmny.service.user.model.dto.UserDto

data class User(
    val id: String,
    val firstName: String,
    val lastName: String?,
    val active: Boolean = false,
    val email: String,
    val authProvider: AuthProvider,
    val profilePhotoUrl: String?,
    val masterTokenId: String? = null,
    val refreshTokenIds: Map<String, String>,
)

fun User.toDto(): UserDto {
    return UserDto(id, firstName, lastName, active, email, authProvider, profilePhotoUrl)
}

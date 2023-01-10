package io.harmny.service.user.request

data class UserSignInRequest(
    val email: String,
    val password: String,
)

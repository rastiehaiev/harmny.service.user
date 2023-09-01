package io.harmny.service.user.web.model.request

data class UserSignInRequest(
    val email: String,
    val password: String,
)

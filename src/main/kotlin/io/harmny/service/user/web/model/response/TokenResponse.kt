package io.harmny.service.user.web.model.response

data class TokenResponse(
    val token: String,
    val refreshToken: String? = null,
)

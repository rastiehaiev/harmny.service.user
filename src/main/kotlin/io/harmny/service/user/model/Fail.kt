package io.harmny.service.user.model

import io.harmny.service.user.response.ErrorResponse

data class Fail(
    val statusCode: Int,
    val reason: FailReason,
) {

    companion object {

        fun unauthenticated(reason: FailReason): Fail = Fail(statusCode = 401, reason)

        fun userNotFound(): Fail = unauthenticated(FailReason.USER_NOT_FOUND)

        fun userNotActive(): Fail = unauthenticated(FailReason.USER_NOT_ACTIVE)

        fun unauthorized(reason: FailReason): Fail = Fail(statusCode = 403, reason)

        fun resourceUnavailable(): Fail = unauthorized(FailReason.RESOURCE_NOT_ALLOWED)

        fun invalidToken(): Fail = unauthorized(FailReason.TOKEN_INVALID)
    }
}

enum class FailReason {
    TOKEN_EXPIRED,
    TOKEN_INVALID,
    APPLICATION_DOES_NOT_EXIST,
    USER_NOT_FOUND_BY_EMAIL_AND_PASSWORD,
    RESOURCE_NOT_ALLOWED,
    WRITE_OPERATIONS_NOT_ALLOWED,
    USER_NOT_ACTIVE,
    USER_NOT_FOUND,
}

fun Fail.toErrorResponse(): ErrorResponse {
    return ErrorResponse(type = this.reason.toString())
}

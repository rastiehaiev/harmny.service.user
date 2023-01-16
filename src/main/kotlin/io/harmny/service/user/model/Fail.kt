package io.harmny.service.user.model

import io.harmny.service.user.response.ErrorResponse

data class Fail(
    val statusCode: Int,
    val reason: FailReason,
) {

    companion object {

        fun unauthenticated(reason: FailReason): Fail = Fail(statusCode = 401, reason)

        fun unauthorized(reason: FailReason): Fail = Fail(statusCode = 403, reason)

        val userNotFound: Fail = unauthenticated(FailReason.USER_NOT_FOUND)

        val userAlreadyExists: Fail = Fail(statusCode = 409, reason = FailReason.USER_EXISTS)

        val userNotActive: Fail = unauthenticated(FailReason.USER_NOT_ACTIVE)

        val resourceUnavailable: Fail = unauthorized(FailReason.RESOURCE_NOT_ALLOWED)

        val invalidToken: Fail = unauthorized(FailReason.TOKEN_INVALID)
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
    USER_EXISTS,
}

fun Fail.toErrorResponse(): ErrorResponse {
    return ErrorResponse(type = this.reason.toString())
}

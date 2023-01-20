package io.harmny.service.user.model

import io.harmny.service.user.response.ErrorObject
import io.harmny.service.user.response.ErrorResponse

data class Fail(
    val statusCode: Int,
    val reason: FailReason,
) {

    companion object {

        fun unauthenticated(reason: FailReason): Fail = Fail(statusCode = 401, reason)
        fun unauthorized(reason: FailReason): Fail = Fail(statusCode = 403, reason)
        private fun badRequest(reason: FailReason): Fail = Fail(statusCode = 400, reason)
        private fun notFound(reason: FailReason): Fail = Fail(statusCode = 404, reason)

        val userNotFound: Fail = unauthenticated(FailReason.USER_NOT_FOUND)
        val userAlreadyExists: Fail = Fail(statusCode = 409, reason = FailReason.USER_EXISTS)
        val userNotActive: Fail = unauthenticated(FailReason.USER_NOT_ACTIVE)

        val applicationNotFound: Fail = notFound(FailReason.APPLICATION_NOT_FOUND)
        val applicationDoesNotExist: Fail = unauthorized(FailReason.APPLICATION_DOES_NOT_EXIST)
        val tokenDoesNotExist: Fail = unauthorized(FailReason.TOKEN_DOES_NOT_EXIST)

        val tokenNotFound: Fail = notFound(FailReason.TOKEN_NOT_FOUND)

        val resourceUnavailable: Fail = unauthorized(FailReason.RESOURCE_NOT_ALLOWED)

        val invalidToken: Fail = unauthorized(FailReason.TOKEN_INVALID)
        val invalidExpirationTime: Fail = badRequest(FailReason.INVALID_EXPIRATION_TIME)
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
    INVALID_EXPIRATION_TIME,
    APPLICATION_NOT_FOUND,
    TOKEN_NOT_FOUND,
    TOKEN_DOES_NOT_EXIST,
}

fun Fail.toErrorResponse(): ErrorResponse {
    return ErrorResponse(listOf(ErrorObject(this.reason.toString())))
}

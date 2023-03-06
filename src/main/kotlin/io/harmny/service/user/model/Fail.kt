package io.harmny.service.user.model

import arrow.core.Either
import arrow.core.left
import io.harmny.service.user.response.ErrorObject
import io.harmny.service.user.response.ErrorResponse
import org.springframework.http.ResponseEntity

data class Fail(
    val type: String,
    val description: String?,
    val properties: Map<String, String>?,
) {

    companion object {

        fun <S> input(
            key: String = "",
            description: String? = null,
            properties: Map<String, String>? = null,
        ): Either<Fail, S> {
            return general("fail.input", key, description, properties)
        }

        fun <S> resource(
            key: String = "",
            description: String? = null,
            properties: Map<String, String>? = null,
        ): Either<Fail, S> {
            return general("fail.resource", key, description, properties)
        }

        fun <S> authentication(
            key: String = "",
            description: String? = null,
            properties: Map<String, String>? = null,
        ): Either<Fail, S> {
            return general("fail.authentication", key, description, properties)
        }

        fun <S> authorization(
            key: String = "",
            description: String? = null,
            properties: Map<String, String>? = null,
        ): Either<Fail, S> {
            return general("fail.authorization", key, description, properties)
        }

        fun <S> conflict(
            key: String = "",
            description: String? = null,
            properties: Map<String, String>? = null,
        ): Either<Fail, S> {
            return general("fail.conflict", key, description, properties)
        }

        fun <S> internal(
            key: String = "",
            description: String? = null,
            properties: Map<String, String>? = null,
        ): Either<Fail, S> {
            return general("fail.internal", key, description, properties)
        }

        private fun <S> general(
            type: String,
            key: String,
            description: String? = null,
            properties: Map<String, String>? = null,
        ): Either<Fail, S> {
            val resultType = if (key.isNotBlank()) "$type.$key" else type
            return Fail(resultType, description, properties).left()
        }
    }
}

fun Fail.toErrorResponseEntity(): ResponseEntity<ErrorResponse> {
    val statusCode = when {
        this.type.startsWith("fail.resource") -> 404
        this.type.startsWith("fail.authentication") -> 401
        this.type.startsWith("fail.authorization") -> 403
        this.type.startsWith("fail.conflict") -> 409
        this.type.startsWith("fail.internal") -> 500
        else -> 400
    }
    return ResponseEntity.status(statusCode).body(toErrorResponse())
}

private fun Fail.toErrorResponse() = ErrorResponse(ErrorObject(type, description, properties))

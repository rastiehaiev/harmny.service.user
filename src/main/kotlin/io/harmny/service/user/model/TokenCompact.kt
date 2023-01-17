package io.harmny.service.user.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TokenCompact(
    val u: String, // user ID
    val a: String? = null, // application ID
    val p: List<String> = emptyList(),
    val e: Long? = null, // expiration
)

fun Token.compact(): TokenCompact {
    return TokenCompact(
        u = this.userId,
        a = this.applicationId,
        e = this.expirationTime?.toEpochMilli(),
        p = this.permissions.map {
            val ownPart = if (it.own) "" else ":n"
            "${it.resource.code}:${it.access.joinToString(separator = "") { access -> access.code }}${ownPart}"
        },
    )
}

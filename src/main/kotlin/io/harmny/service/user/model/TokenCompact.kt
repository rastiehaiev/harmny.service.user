package io.harmny.service.user.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TokenCompact(
    val u: String, // user ID
    val a: String? = null, // application ID
    val p: List<TokenCompactPermission> = emptyList(),
    val e: Instant? = null, // expiration
)

data class TokenCompactPermission(
    val r: String, // resource
    val a: String, // access
    val o: String, // own
)

fun Token.compacted(): TokenCompact {
    return TokenCompact(
        u = this.userId,
        a = this.applicationId,
        e = this.expirationTime,
        p = this.permissions.map {
            TokenCompactPermission(
                r = it.resource.code,
                a = it.access.joinToString { access -> access.name.lowercase() },
                o = if (it.own) "y" else "n",
            )
        },
    )
}

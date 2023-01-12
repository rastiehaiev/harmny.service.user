package io.harmny.service.user.instruments

import arrow.core.Either
import arrow.core.left
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.harmny.service.user.model.Fail
import io.harmny.service.user.model.Token
import io.harmny.service.user.model.TokenCompact
import io.harmny.service.user.model.compacted
import io.harmny.service.user.model.loosen
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.time.temporal.ChronoUnit
import java.util.Date

@Component
class TokenHandler(
    private val objectMapper: ObjectMapper,
) {

    private val rawKey: String = "DAVLtjoTHQ3uhsGm2VstWj5M2JsdhhxQPy71BL11XQ4E5OpRgfCYjNPELkP1M6g"
    private val key = Keys.hmacShaKeyFor(rawKey.toByteArray())
    private val parser = Jwts.parserBuilder().setSigningKey(key).build()

    fun generate(token: Token): String {
        return Jwts.builder()
            .setClaims(hashMapOf<String, Any>(Pair("token", objectMapper.writeValueAsString(token.compacted()))))
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + ChronoUnit.YEARS.duration.toMillis()))
            .signWith(key)
            .compact()
    }

    fun parse(token: String): Either<Fail, Token> {
        return try {
            val claims = parser.parseClaimsJws(token).body
            objectMapper.readValue<TokenCompact>(claims.get("token", String::class.java)).loosen()
        } catch (e: Exception) {
            Fail.invalidToken().left()
        }
    }
}

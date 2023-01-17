package io.harmny.service.user.model

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TokenTest {

    private val objectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())

    @Test
    fun `should create compact token for application`() {
        val userId = UUID.randomUUID().toString()
        val applicationId = UUID.randomUUID().toString()
        val token = Token(
            userId = userId,
            applicationId = applicationId,
            permissions = listOf(
                TokenPermission(
                    resource = TokenResourceType.BOOK,
                    access = listOf(TokenAccessType.CREATE, TokenAccessType.DELETE),
                ),
            ),
        ).compact()

        val result = objectMapper.writeValueAsString(token)
        assertThat(result).isEqualTo("{\"u\":\"$userId\",\"a\":\"$applicationId\",\"p\":[\"b:cd\"]}")
    }

    @Test
    fun `should create compact master token`() {
        val userId = UUID.randomUUID().toString()
        val expirationTime = Instant.now()
        val token = Token(userId = userId, expirationTime = expirationTime)
        assertThat(token.isMaster()).isTrue

        val tokenCompact = token.compact()

        val result = objectMapper.writeValueAsString(tokenCompact)
        assertThat(result).isEqualTo("{\"u\":\"$userId\",\"e\":${expirationTime.toEpochMilli()}}")
    }

    @Test
    fun `should compact and loosen back the token`() {
        val token = Token(
            userId = UUID.randomUUID().toString(),
            applicationId = UUID.randomUUID().toString(),
            permissions = listOf(
                TokenPermission(
                    resource = TokenResourceType.BOOK,
                    access = listOf(TokenAccessType.CREATE, TokenAccessType.DELETE),
                ),
                TokenPermission(
                    resource = TokenResourceType.ROUTINE,
                    access = listOf(TokenAccessType.CREATE, TokenAccessType.UPDATE, TokenAccessType.DELETE),
                ),
            ),
        )
        val tokenCompact = token.compact()
        val tokenLoosen = tokenCompact.loosen().orNull()

        assertThat(tokenLoosen).isEqualTo(token)
    }
}
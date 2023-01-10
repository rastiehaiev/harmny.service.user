package io.harmny.service.user.instruments

import arrow.core.Either
import io.harmny.service.user.model.Fail
import io.harmny.service.user.model.Token
import org.springframework.stereotype.Component

@Component
class TokenHandler {

    private val secret: String = ""

    fun parse(token: String): Either<Fail, Token> {
        TODO()
    }
}

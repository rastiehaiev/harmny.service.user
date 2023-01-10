package io.harmny.service.user.service

import arrow.core.Either
import io.harmny.service.user.model.Fail
import io.harmny.service.user.model.User
import io.harmny.service.user.request.UserCreateRequest
import io.harmny.service.user.request.UserUpdateRequest
import org.springframework.stereotype.Service

@Service
class UserService {

    fun findById(userId: String): User? {
        TODO()
    }

    fun findByEmailAndPassword(email: String, password: String): User? {
        TODO()
    }

    fun create(request: UserCreateRequest): User {
        TODO()
    }

    fun update(userId: String, request: UserUpdateRequest): Either<Fail, User> {
        TODO()
    }
}

package io.harmny.service.user.service

import arrow.core.Either
import arrow.core.left
import io.harmny.service.user.model.Fail
import io.harmny.service.user.model.User
import io.harmny.service.user.repository.UserRepository
import io.harmny.service.user.request.UserCreateRequest
import io.harmny.service.user.request.UserUpdateRequest
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    fun findById(userId: String): User? {
        return userRepository.findById(userId)
    }

    fun findByEmailAndPassword(email: String, password: String): User? {
        return userRepository.findByEmailAndPassword(email, password)
    }

    fun create(request: UserCreateRequest): Either<Fail, User> {
        val user = User(
            id = UUID.randomUUID().toString(),
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            password = request.password,
            active = true,
        )
        return userRepository.save(user)
    }

    fun update(userId: String, request: UserUpdateRequest): Either<Fail, User> {
        val user = userRepository.findById(userId) ?: return Fail.userNotFound.left()

        TODO()
    }
}

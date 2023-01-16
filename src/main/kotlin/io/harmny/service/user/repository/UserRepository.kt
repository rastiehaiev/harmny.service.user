package io.harmny.service.user.repository

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.harmny.service.user.model.Fail
import io.harmny.service.user.model.User
import org.springframework.stereotype.Component

@Component
class UserRepository {

    private val users = hashMapOf<String, User>()

    fun findByEmailAndPassword(email: String, password: String): User? {
        return users.values.firstOrNull {
            it.email.equals(email, ignoreCase = true) && it.password.equals(password, ignoreCase = true)
        }
    }

    fun findById(userId: String): User? {
        return users[userId]
    }

    fun save(user: User): Either<Fail, User> {
        if (findByEmail(user.email) != null) {
            return Fail.userAlreadyExists.left()
        }
        users[user.id] = user
        return user.right()
    }

    private fun findByEmail(email: String): User? {
        return users.values.firstOrNull {
            it.email.equals(email, ignoreCase = true)
        }
    }
}

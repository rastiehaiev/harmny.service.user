package io.harmny.service.user.repository

import io.harmny.service.user.model.User
import org.springframework.stereotype.Component

@Component
class UserRepository {

    private val users = mutableListOf<User>()

    fun findByEmailAndPassword(email: String, password: String): User? {
        return users.firstOrNull {
            it.email.equals(email, ignoreCase = true) && it.password.equals(password, ignoreCase = true)
        }
    }

    fun findById(userId: String): User? {
        return users.firstOrNull { it.id.equals(userId, ignoreCase = true) }
    }

    fun save(user: User) {
        users.add(user)
    }
}

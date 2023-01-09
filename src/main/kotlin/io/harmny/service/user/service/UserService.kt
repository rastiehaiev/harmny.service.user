package io.harmny.service.user.service

import io.harmny.service.user.model.User
import io.harmny.service.user.request.UserCreateRequest
import io.harmny.service.user.request.UserUpdateRequest
import org.springframework.stereotype.Service

@Service
class UserService {

    fun findById(userId: String): User? {
        TODO()
    }

    fun create(request: UserCreateRequest): User {
        TODO()
    }

    fun update(userId: String, request: UserUpdateRequest): User {
        TODO()
    }
}

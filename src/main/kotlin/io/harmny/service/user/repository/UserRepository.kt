package io.harmny.service.user.repository

import io.harmny.service.user.entity.UserEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<UserEntity, String> {

    fun findByEmail(email: String): UserEntity?
}

package io.harmny.service.user.repository

import io.harmny.service.user.entity.ApplicationEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationRepository : MongoRepository<ApplicationEntity, String> {

    fun findAllByUserId(userId: String): List<ApplicationEntity>
}

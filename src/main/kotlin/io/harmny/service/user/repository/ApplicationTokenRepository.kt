package io.harmny.service.user.repository

import io.harmny.service.user.entity.ApplicationTokenEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationTokenRepository : MongoRepository<ApplicationTokenEntity, String> {

    fun findAllByUserIdAndApplicationId(userId: String, applicationId: String): List<ApplicationTokenEntity>

    fun deleteAllByUserIdAndApplicationId(userId: String, applicationId: String)
}

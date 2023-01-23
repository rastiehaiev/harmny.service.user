package io.harmny.service.user.repository

import io.harmny.service.user.entity.TokenEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TokenRepository : MongoRepository<TokenEntity, String> {

    fun findAllByUserIdAndApplicationId(userId: String, applicationId: String): List<TokenEntity>

    fun deleteAllByUserIdAndApplicationId(userId: String, applicationId: String)
}

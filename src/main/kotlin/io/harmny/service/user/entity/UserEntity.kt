package io.harmny.service.user.entity

import io.harmny.service.user.model.AuthProvider
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("users")
data class UserEntity(
    @Id
    val id: String,
    var firstName: String,
    var lastName: String?,
    var active: Boolean = false,
    val email: String,
    var password: String?,
    val authProvider: AuthProvider,
    val profilePhotoUrl: String?,
    var masterTokenId: String? = null,
    var refreshTokenIds: HashMap<String, String> = HashMap(),
)

package io.harmny.service.user.entity

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
    var password: String,
)

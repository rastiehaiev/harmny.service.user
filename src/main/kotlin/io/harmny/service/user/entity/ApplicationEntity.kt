package io.harmny.service.user.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("applications")
data class ApplicationEntity(
    @Id
    val id: String,
    val userId: String,
    var name: String,
)

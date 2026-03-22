package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

@Serializable
data class RefreshToken(
    @BsonId
    val id: String = ObjectId().toString(),
    val token: String,
    val userId: String,
    val userType: String,
    val email: String,
    val createdAt: Long = Instant.now().epochSecond,
    val lastUsedAt: Long = Instant.now().epochSecond,
    val expiresAt: Long,
    val isRevoked: Boolean = false
)

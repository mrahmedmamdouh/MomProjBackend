package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

enum class UserType {
    MOM, DOCTOR, ADMIN
}

@Serializable
data class User(
    @BsonId
    val id: String = ObjectId().toString(),
    val email: String,
    val password: String,
    val userType: UserType,
    val momId: String? = null,
    val doctorId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

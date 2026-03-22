package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MomAuth(
    @BsonId
    val id: String = ObjectId().toString(),
    val uid: String,
    val momId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class DoctorAuth(
    @BsonId
    val id: String = ObjectId().toString(),
    val uid: String,
    val doctorId: String,
    val createdAt: Long = System.currentTimeMillis()
)

package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Doctor(
    @BsonId
    val id: String = ObjectId().toString(),
    val authUid: String,
    val name: String,
    val email: String,
    val phone: String,
    val specialization: String,
    val rating: Double = 0.0,
    val isAuthorized: Boolean = false,
    val photo: String = "",
    val nidId: String,
    val nidRef: String,
    val createdAt: Long = System.currentTimeMillis()
)

package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Seller(
    @BsonId
    val id: String = ObjectId().toString(),
    val name: String,
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
)

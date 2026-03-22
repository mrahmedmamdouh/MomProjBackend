package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class ProductRating(
    @BsonId
    val id: String = ObjectId().toString(),
    val productId: String,
    val uid: String,
    val rating: Int,
    val title: String = "",
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

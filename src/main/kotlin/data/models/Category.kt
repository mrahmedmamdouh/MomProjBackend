package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Category(
    @BsonId
    val id: String = ObjectId().toString(),
    val name: String,
    val slug: String,
    val createdAt: Long = System.currentTimeMillis()
)

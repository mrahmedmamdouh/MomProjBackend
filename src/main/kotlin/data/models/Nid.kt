package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Nid(
    @BsonId
    val id: String = ObjectId().toString(),
    val imageFront: String,
    val imageBack: String,
    val createdAt: Long = System.currentTimeMillis()
)

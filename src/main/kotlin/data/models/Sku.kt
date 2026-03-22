package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Sku(
    @BsonId
    val id: String = ObjectId().toString(),
    val productId: String,
    val productRef: String,
    val skuCode: String,
    val title: String,
    val taxClass: String = "STANDARD",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

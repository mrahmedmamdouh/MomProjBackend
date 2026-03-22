package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Inventory(
    @BsonId
    val id: String = ObjectId().toString(),
    val skuId: String,
    val onHand: Int = 0,
    val reserved: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

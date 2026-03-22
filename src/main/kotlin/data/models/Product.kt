package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Product(
    @BsonId
    val id: String = ObjectId().toString(),
    val name: String,
    val slug: String,
    val description: String,
    val status: String = "ACTIVE",
    val defaultSellerId: String,
    val defaultSellerRef: String,
    val categoryIds: List<String> = emptyList(),
    val minSessionsToPurchase: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Cart(
    @BsonId
    val id: String = ObjectId().toString(),
    val momId: String,
    val items: List<CartItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class CartItem(
    val skuId: String,
    val qty: Int,
    val priceSnapshot: Double,
    val offerId: String,
    val skuRef: String,
    val offerRef: String,
    val addedAt: Long = System.currentTimeMillis()
)

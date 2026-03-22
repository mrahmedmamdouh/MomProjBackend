package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Order(
    @BsonId
    val id: String = ObjectId().toString(),
    val orderNo: String,
    val momId: String,
    val momRef: String,
    val uid: String,
    val placedAt: Long = System.currentTimeMillis(),
    val status: String = "PENDING",
    val currency: String = "USD",
    val subtotal: Double,
    val discountTotal: Double = 0.0,
    val taxTotal: Double = 0.0,
    val shippingTotal: Double = 0.0,
    val grandTotal: Double,
    val items: List<OrderItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class OrderItem(
    val skuId: String,
    val skuRef: String,
    val productId: String,
    val productRef: String,
    val sellerId: String,
    val sellerRef: String,
    val qty: Int,
    val unitPrice: Double,
    val lineTotal: Double,
    val productName: String
)

package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class SkuOffer(
    @BsonId
    val id: String = ObjectId().toString(),
    val skuId: String,
    val skuRef: String,
    val sellerId: String,
    val sellerRef: String,
    val listPrice: Double,
    val salePrice: Double,
    val currency: String = "USD",
    val isActive: Boolean = true,
    val activeFrom: Long = System.currentTimeMillis(),
    val activeTo: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

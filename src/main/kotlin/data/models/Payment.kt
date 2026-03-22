package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Payment(
    @BsonId
    val id: String = ObjectId().toString(),
    val orderId: String,
    val orderRef: String,
    val uid: String,
    val provider: String,
    val method: String,
    val amount: Double,
    val currency: String = "USD",
    val status: String = "PENDING",
    val transactionRef: String? = null,
    val authorizedAt: Long? = null,
    val capturedAt: Long? = null,
    val refundedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

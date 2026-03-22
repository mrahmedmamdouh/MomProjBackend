package com.evelolvetech.data.responses

import com.evelolvetech.data.models.Payment
import kotlinx.serialization.Serializable

@Serializable
data class PaymentResponse(
    val id: String,
    val orderId: String,
    val provider: String,
    val method: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val transactionRef: String?,
    val authorizedAt: Long?,
    val capturedAt: Long?,
    val refundedAt: Long?,
    val createdAt: Long
) {
    companion object {
        fun fromPayment(payment: Payment) = PaymentResponse(
            id = payment.id,
            orderId = payment.orderId,
            provider = payment.provider,
            method = payment.method,
            amount = payment.amount,
            currency = payment.currency,
            status = payment.status,
            transactionRef = payment.transactionRef,
            authorizedAt = payment.authorizedAt,
            capturedAt = payment.capturedAt,
            refundedAt = payment.refundedAt,
            createdAt = payment.createdAt
        )
    }
}

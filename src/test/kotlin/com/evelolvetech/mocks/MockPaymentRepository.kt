package com.evelolvetech.mocks

import com.evelolvetech.data.models.Payment
import com.evelolvetech.data.repository.api.mom.ecommerce.PaymentRepository

class MockPaymentRepository : PaymentRepository {
    val payments = mutableMapOf<String, Payment>()

    override suspend fun createPayment(payment: Payment): Boolean {
        payments[payment.id] = payment
        return true
    }

    override suspend fun getPaymentById(id: String): Payment? {
        return payments[id]
    }

    override suspend fun getPaymentsByOrderId(orderId: String): List<Payment> {
        return payments.values.filter { it.orderId == orderId }
    }

    override suspend fun getPaymentsByMomUid(uid: String): List<Payment> {
        return payments.values.filter { it.uid == uid }
    }

    override suspend fun updatePaymentStatus(id: String, status: String, transactionRef: String?): Boolean {
        val payment = payments[id] ?: return false
        payments[id] = payment.copy(
            status = status,
            transactionRef = transactionRef ?: payment.transactionRef,
            authorizedAt = if (status == "AUTHORIZED") System.currentTimeMillis() else payment.authorizedAt,
            capturedAt = if (status == "CAPTURED") System.currentTimeMillis() else payment.capturedAt,
            refundedAt = if (status == "REFUNDED") System.currentTimeMillis() else payment.refundedAt
        )
        return true
    }

    override suspend fun deletePayment(id: String): Boolean {
        return payments.remove(id) != null
    }
}

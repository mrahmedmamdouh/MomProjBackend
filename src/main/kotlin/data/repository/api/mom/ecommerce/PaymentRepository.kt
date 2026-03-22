package com.evelolvetech.data.repository.api.mom.ecommerce

import com.evelolvetech.data.models.Payment

interface PaymentRepository {
    suspend fun createPayment(payment: Payment): Boolean
    suspend fun getPaymentById(id: String): Payment?
    suspend fun getPaymentsByOrderId(orderId: String): List<Payment>
    suspend fun getPaymentsByMomUid(uid: String): List<Payment>
    suspend fun updatePaymentStatus(id: String, status: String, transactionRef: String? = null): Boolean
    suspend fun deletePayment(id: String): Boolean
}

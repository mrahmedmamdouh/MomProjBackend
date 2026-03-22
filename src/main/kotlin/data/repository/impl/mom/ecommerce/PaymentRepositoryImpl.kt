package com.evelolvetech.data.repository.impl.mom.ecommerce

import com.evelolvetech.data.models.Payment
import com.evelolvetech.data.repository.api.mom.ecommerce.PaymentRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.*

class PaymentRepositoryImpl(
    db: MongoDatabase
) : PaymentRepository {

    private val payments: MongoCollection<Payment> = db.getCollection<Payment>()

    override suspend fun createPayment(payment: Payment): Boolean {
        return try {
            payments.insertOne(payment)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getPaymentById(id: String): Payment? {
        return payments.findOne(Payment::id eq id)
    }

    override suspend fun getPaymentsByOrderId(orderId: String): List<Payment> {
        return payments.find(Payment::orderId eq orderId).toList()
    }

    override suspend fun getPaymentsByMomUid(uid: String): List<Payment> {
        return payments.find(Payment::uid eq uid).toList()
    }

    override suspend fun updatePaymentStatus(id: String, status: String, transactionRef: String?): Boolean {
        return try {
            val updates = mutableListOf(setValue(Payment::status, status))
            transactionRef?.let { updates.add(setValue(Payment::transactionRef, it)) }

            val result = payments.updateOne(
                Payment::id eq id,
                combine(updates)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deletePayment(id: String): Boolean {
        return try {
            val result = payments.deleteOne(Payment::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }
}

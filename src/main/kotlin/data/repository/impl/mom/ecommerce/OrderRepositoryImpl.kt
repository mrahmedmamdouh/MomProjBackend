package com.evelolvetech.data.repository.impl.mom.ecommerce

import com.evelolvetech.data.models.Order
import com.evelolvetech.data.models.OrderItem
import com.evelolvetech.data.repository.api.mom.ecommerce.OrderRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.*

class OrderRepositoryImpl(
    db: MongoDatabase
) : OrderRepository {

    private val orders: MongoCollection<Order> = db.getCollection<Order>()

    override suspend fun createOrder(order: Order): Boolean {
        return try {
            orders.insertOne(order)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getOrderById(id: String): Order? {
        return orders.findOne(Order::id eq id)
    }

    override suspend fun getOrdersByMomId(momId: String, page: Int, size: Int): List<Order> {
        return orders.find(Order::momId eq momId)
            .skip(page * size)
            .limit(size)
            .sort(descending(Order::placedAt))
            .toList()
    }

    override suspend fun updateOrderStatus(id: String, status: String): Boolean {
        return try {
            val result = orders.updateOne(
                Order::id eq id,
                setValue(Order::status, status)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteOrder(id: String): Boolean {
        return try {
            val result = orders.deleteOne(Order::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getOrderItems(orderId: String): List<OrderItem> {
        return getOrderById(orderId)?.items ?: emptyList()
    }

    override suspend fun getAllOrders(page: Int, size: Int): List<Order> {
        return orders.find()
            .skip(page * size)
            .limit(size)
            .sort(descending(Order::placedAt))
            .toList()
    }
}

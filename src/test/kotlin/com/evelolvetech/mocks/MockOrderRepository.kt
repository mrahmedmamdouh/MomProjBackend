package com.evelolvetech.mocks

import com.evelolvetech.data.models.Order
import com.evelolvetech.data.models.OrderItem
import com.evelolvetech.data.repository.api.mom.ecommerce.OrderRepository

class MockOrderRepository : OrderRepository {
    val orders = mutableMapOf<String, Order>()

    override suspend fun createOrder(order: Order): Boolean {
        orders[order.id] = order
        return true
    }

    override suspend fun getOrderById(id: String): Order? {
        return orders[id]
    }

    override suspend fun getOrdersByMomId(momId: String, page: Int, size: Int): List<Order> {
        return orders.values
            .filter { it.momId == momId }
            .sortedByDescending { it.createdAt }
            .drop(page * size)
            .take(size)
    }

    override suspend fun updateOrderStatus(id: String, status: String): Boolean {
        val order = orders[id] ?: return false
        orders[id] = order.copy(status = status)
        return true
    }

    override suspend fun deleteOrder(id: String): Boolean {
        return orders.remove(id) != null
    }

    override suspend fun getOrderItems(orderId: String): List<OrderItem> {
        return orders[orderId]?.items ?: emptyList()
    }

    override suspend fun getAllOrders(page: Int, size: Int): List<Order> {
        return orders.values
            .sortedByDescending { it.createdAt }
            .drop(page * size)
            .take(size)
    }
}

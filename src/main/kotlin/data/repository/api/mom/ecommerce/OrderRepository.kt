package com.evelolvetech.data.repository.api.mom.ecommerce

import com.evelolvetech.data.models.Order
import com.evelolvetech.data.models.OrderItem

interface OrderRepository {
    suspend fun createOrder(order: Order): Boolean
    suspend fun getOrderById(id: String): Order?
    suspend fun getOrdersByMomId(momId: String, page: Int = 0, size: Int = 20): List<Order>
    suspend fun updateOrderStatus(id: String, status: String): Boolean
    suspend fun deleteOrder(id: String): Boolean
    suspend fun getOrderItems(orderId: String): List<OrderItem>
    suspend fun getAllOrders(page: Int = 0, size: Int = 20): List<Order>
}

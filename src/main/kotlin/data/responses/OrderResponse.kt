package com.evelolvetech.data.responses

import com.evelolvetech.data.models.Order
import kotlinx.serialization.Serializable

@Serializable
data class OrderResponse(
    val id: String,
    val orderNo: String,
    val momId: String,
    val placedAt: Long,
    val status: String,
    val currency: String,
    val subtotal: Double,
    val discountTotal: Double,
    val taxTotal: Double,
    val shippingTotal: Double,
    val grandTotal: Double,
    val items: List<OrderItemResponse>,
    val createdAt: Long
) {
    companion object {
        fun fromOrder(order: Order, itemResponses: List<OrderItemResponse>) = OrderResponse(
            id = order.id,
            orderNo = order.orderNo,
            momId = order.momId,
            placedAt = order.placedAt,
            status = order.status,
            currency = order.currency,
            subtotal = order.subtotal,
            discountTotal = order.discountTotal,
            taxTotal = order.taxTotal,
            shippingTotal = order.shippingTotal,
            grandTotal = order.grandTotal,
            items = itemResponses,
            createdAt = order.createdAt
        )
    }
}

@Serializable
data class OrderItemResponse(
    val skuId: String,
    val productId: String,
    val sellerId: String,
    val qty: Int,
    val unitPrice: Double,
    val lineTotal: Double,
    val productName: String
)

@Serializable
data class OrderListResponse(
    val orders: List<OrderResponse>,
    val page: Int,
    val size: Int,
    val total: Int
)

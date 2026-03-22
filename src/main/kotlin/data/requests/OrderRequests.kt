package com.evelolvetech.data.requests

data class CreateOrderRequest(
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    val skuId: String,
    val qty: Int,
    val offerId: String? = null
)

data class UpdateOrderStatusRequest(
    val status: String
)

data class CreatePaymentRequest(
    val orderId: String,
    val provider: String,
    val method: String,
    val amount: Double
)

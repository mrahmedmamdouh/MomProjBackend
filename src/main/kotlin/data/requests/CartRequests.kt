package com.evelolvetech.data.requests

data class AddToCartRequest(
    val skuId: String,
    val qty: Int,
    val offerId: String
)

data class UpdateCartItemRequest(
    val qty: Int
)

data class CartResponse(
    val id: String,
    val momId: String,
    val items: List<CartItemResponse>,
    val totalItems: Int,
    val totalPrice: Double,
    val createdAt: Long,
    val updatedAt: Long
)

data class CartItemResponse(
    val skuId: String,
    val qty: Int,
    val priceSnapshot: Double,
    val offerId: String,
    val skuRef: String,
    val offerRef: String,
    val addedAt: Long,
    val totalPrice: Double
)

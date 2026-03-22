package com.evelolvetech.data.responses

import com.evelolvetech.data.models.Cart
import kotlinx.serialization.Serializable

@Serializable
data class CartResponse(
    val id: String,
    val momId: String,
    val items: List<CartItemResponse>,
    val totalItems: Int,
    val totalPrice: Double,
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        fun fromCart(cart: Cart, itemResponses: List<CartItemResponse>) = CartResponse(
            id = cart.id,
            momId = cart.momId,
            items = itemResponses,
            totalItems = itemResponses.sumOf { it.qty },
            totalPrice = itemResponses.sumOf { it.qty * it.priceSnapshot },
            createdAt = cart.createdAt,
            updatedAt = cart.updatedAt
        )
    }
}

@Serializable
data class CartItemResponse(
    val skuId: String,
    val qty: Int,
    val priceSnapshot: Double,
    val offerId: String,
    val productName: String,
    val skuTitle: String,
    val addedAt: Long
)

package com.evelolvetech.data.repository.api.mom.ecommerce

import com.evelolvetech.data.models.Cart
import com.evelolvetech.data.models.CartItem

interface CartRepository {
    suspend fun createCart(cart: Cart): Boolean
    suspend fun getCartByMomId(momId: String): Cart?
    suspend fun addItemToCart(momId: String, item: CartItem): Boolean
    suspend fun updateCartItem(momId: String, skuId: String, qty: Int): Boolean
    suspend fun removeItemFromCart(momId: String, skuId: String): Boolean
    suspend fun clearCart(momId: String): Boolean
    suspend fun getCartItems(momId: String): List<CartItem>
}

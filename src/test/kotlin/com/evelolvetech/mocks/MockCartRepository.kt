package com.evelolvetech.mocks

import com.evelolvetech.data.models.Cart
import com.evelolvetech.data.models.CartItem
import com.evelolvetech.data.repository.api.mom.ecommerce.CartRepository

class MockCartRepository : CartRepository {
    private val carts = mutableMapOf<String, Cart>()
    
    init {
        carts["mom_alice"] = Cart(
            id = "cart_alice",
            momId = "mom_alice",
            items = listOf(
                CartItem(
                    skuId = "sku_prenatal_batchA",
                    qty = 2,
                    priceSnapshot = 19.99,
                    offerId = "offer_prn_happy",
                    skuRef = "/skus/sku_prenatal_batchA",
                    offerRef = "/offers/offer_prn_happy"
                ),
                CartItem(
                    skuId = "sku_essentials_black",
                    qty = 1,
                    priceSnapshot = 6.00,
                    offerId = "offer_ess_black",
                    skuRef = "/skus/sku_essentials_black",
                    offerRef = "/offers/offer_ess_black"
                )
            )
        )
        carts["mom_empty"] = Cart(
            id = "cart_empty",
            momId = "mom_empty",
            items = emptyList()
        )
    }
    
    override suspend fun createCart(cart: Cart): Boolean {
        carts[cart.momId] = cart
        return true
    }
    
    override suspend fun getCartByMomId(momId: String): Cart? {
        return carts[momId]
    }
    
    override suspend fun addItemToCart(momId: String, item: CartItem): Boolean {
        val cart = carts[momId] ?: return false
        val updatedItems = cart.items.toMutableList()
        updatedItems.add(item)
        carts[momId] = cart.copy(items = updatedItems)
        return true
    }
    
    override suspend fun updateCartItem(momId: String, skuId: String, qty: Int): Boolean {
        val cart = carts[momId] ?: return false
        val itemExists = cart.items.any { it.skuId == skuId }
        if (!itemExists) return false
        
        val updatedItems = cart.items.map { item ->
            if (item.skuId == skuId) item.copy(qty = qty) else item
        }
        carts[momId] = cart.copy(items = updatedItems)
        return true
    }
    
    override suspend fun removeItemFromCart(momId: String, skuId: String): Boolean {
        val cart = carts[momId] ?: return false
        val itemExists = cart.items.any { it.skuId == skuId }
        if (!itemExists) return false
        
        val updatedItems = cart.items.filter { it.skuId != skuId }
        carts[momId] = cart.copy(items = updatedItems)
        return true
    }
    
    override suspend fun clearCart(momId: String): Boolean {
        val cart = carts[momId] ?: return false
        carts[momId] = cart.copy(items = emptyList())
        return true
    }
    
    override suspend fun getCartItems(momId: String): List<CartItem> {
        return carts[momId]?.items ?: emptyList()
    }
}

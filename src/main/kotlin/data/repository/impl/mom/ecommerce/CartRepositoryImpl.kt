package com.evelolvetech.data.repository.impl.mom.ecommerce

import com.evelolvetech.data.models.Cart
import com.evelolvetech.data.models.CartItem
import com.evelolvetech.data.repository.api.mom.ecommerce.CartRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class CartRepositoryImpl(
    db: MongoDatabase
) : CartRepository {

    private val carts: MongoCollection<Cart> = db.getCollection<Cart>()

    override suspend fun createCart(cart: Cart): Boolean {
        return try {
            carts.insertOne(cart)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getCartByMomId(momId: String): Cart? {
        return carts.findOne(Cart::momId eq momId)
    }

    override suspend fun addItemToCart(momId: String, item: CartItem): Boolean {
        return try {
            val cart = getCartByMomId(momId) ?: Cart(momId = momId)
            val updatedItems = cart.items.toMutableList()
            updatedItems.add(item)
            val updatedCart = cart.copy(items = updatedItems, updatedAt = System.currentTimeMillis())

            carts.replaceOne(Cart::momId eq momId, updatedCart, com.mongodb.client.model.ReplaceOptions().upsert(true))
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateCartItem(momId: String, skuId: String, qty: Int): Boolean {
        return try {
            val cart = getCartByMomId(momId) ?: return false
            val updatedItems = cart.items.map { item ->
                if (item.skuId == skuId) {
                    item.copy(qty = qty)
                } else {
                    item
                }
            }
            val updatedCart = cart.copy(items = updatedItems, updatedAt = System.currentTimeMillis())
            carts.replaceOne(Cart::momId eq momId, updatedCart)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removeItemFromCart(momId: String, skuId: String): Boolean {
        return try {
            val cart = getCartByMomId(momId) ?: return false
            val updatedItems = cart.items.filter { it.skuId != skuId }
            val updatedCart = cart.copy(items = updatedItems, updatedAt = System.currentTimeMillis())
            carts.replaceOne(Cart::momId eq momId, updatedCart)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun clearCart(momId: String): Boolean {
        return try {
            val result = carts.deleteOne(Cart::momId eq momId)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getCartItems(momId: String): List<CartItem> {
        return getCartByMomId(momId)?.items ?: emptyList()
    }
}

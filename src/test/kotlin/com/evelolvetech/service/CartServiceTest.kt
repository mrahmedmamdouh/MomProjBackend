package com.evelolvetech.service

import com.evelolvetech.data.models.*
import com.evelolvetech.data.requests.AddToCartRequest
import com.evelolvetech.data.requests.UpdateCartItemRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.mocks.*
import kotlinx.coroutines.runBlocking
import com.evelolvetech.service.mom.ecommerce.CartService
import kotlin.test.*

class CartServiceTest {
    
    private val mockCartRepository = MockCartRepository()
    private val mockMomRepository = MockMomRepository()
    private val mockSkuOfferRepository = MockSkuOfferRepository()
    
    private val cartService = CartService(
        mockCartRepository,
        mockMomRepository,
        mockSkuOfferRepository,
        MockAuthConfig.instance
    )
    
    init {
        mockMomRepository.moms["mom_alice"] = Mom(
            id = "mom_alice",
            email = "alice@example.com",
            fullName = "Alice Johnson",
            phone = "+1234567890",
            maritalStatus = "married",
            authUid = "auth_alice",
            photoUrl = "https://example.com/photo.jpg",
            numberOfSessions = 10,
            isAuthorized = true,
            nidId = "nid_alice",
            nidRef = "/nids/nid_alice"
        )
        
        mockMomRepository.moms["mom_unauthorized"] = Mom(
            id = "mom_unauthorized",
            email = "unauthorized@example.com",
            fullName = "Unauthorized Mom",
            phone = "+1234567891",
            maritalStatus = "single",
            authUid = "auth_unauthorized",
            photoUrl = "https://example.com/photo2.jpg",
            numberOfSessions = 3,
            isAuthorized = false,
            nidId = "nid_unauthorized",
            nidRef = "/nids/nid_unauthorized"
        )
        
        mockMomRepository.moms["mom_empty"] = Mom(
            id = "mom_empty",
            email = "empty@example.com",
            fullName = "Empty Cart Mom",
            phone = "+1234567892",
            maritalStatus = "married",
            authUid = "auth_empty",
            photoUrl = "https://example.com/photo3.jpg",
            numberOfSessions = 12,
            isAuthorized = true,
            nidId = "nid_empty",
            nidRef = "/nids/nid_empty"
        )
    }
    
    @Test
    fun testGetCartSuccess() = runBlocking {
        val momId = "mom_alice"
        
        val result = cartService.getCart(momId)
        
        assertTrue(result.success)
        assertNotNull(result.data)
        val cartResponse = result.data as com.evelolvetech.data.requests.CartResponse
        assertEquals(momId, cartResponse.momId)
        assertEquals(3, cartResponse.totalItems)
        assertEquals(45.98, cartResponse.totalPrice)
    }
    
    @Test
    fun testGetCartEmpty() = runBlocking {
        val momId = "mom_empty"
        
        val result = cartService.getCart(momId)
        
        assertTrue(result.success)
        assertNotNull(result.data)
        val cartResponse = result.data as com.evelolvetech.data.requests.CartResponse
        assertEquals(momId, cartResponse.momId)
        assertEquals(0, cartResponse.totalItems)
        assertEquals(0.0, cartResponse.totalPrice)
    }
    
    @Test
    fun testAddToCartSuccess() = runBlocking {
        val momId = "mom_alice"
        val request = AddToCartRequest(
            skuId = "sku_prenatal_batchA",
            qty = 2,
            offerId = "offer_prn_happy"
        )
        
        val result = cartService.addToCart(momId, request)
        
        assertTrue(result.success)
        assertEquals("Cart retrieved successfully", result.message)
    }
    
    @Test
    fun testAddToCartInvalidQuantity() = runBlocking {
        val momId = "mom_alice"
        val request = AddToCartRequest(
            skuId = "sku_prenatal_batchA",
            qty = 0,
            offerId = "offer_prn_happy"
        )
        
        val result = cartService.addToCart(momId, request)
        
        assertFalse(result.success)
        assertEquals("Quantity must be greater than 0", result.message)
    }
    
    @Test
    fun testAddToCartMomNotFound() = runBlocking {
        val momId = "mom_nonexistent"
        val request = AddToCartRequest(
            skuId = "sku_prenatal_batchA",
            qty = 2,
            offerId = "offer_prn_happy"
        )
        
        val result = cartService.addToCart(momId, request)
        
        assertFalse(result.success)
        assertEquals("Mom not found", result.message)
    }
    
    @Test
    fun testAddToCartSkuNotFound() = runBlocking {
        val momId = "mom_alice"
        val request = AddToCartRequest(
            skuId = "sku_nonexistent",
            qty = 2,
            offerId = "offer_prn_happy"
        )
        
        val result = cartService.addToCart(momId, request)
        
        assertFalse(result.success)
        assertEquals("Offer does not match SKU", result.message)
    }
    
    @Test
    fun testAddToCartOfferNotFound() = runBlocking {
        val momId = "mom_alice"
        val request = AddToCartRequest(
            skuId = "sku_prenatal_batchA",
            qty = 2,
            offerId = "offer_nonexistent"
        )
        
        val result = cartService.addToCart(momId, request)
        
        assertFalse(result.success)
        assertEquals("Offer not found", result.message)
    }
    
    @Test
    fun testUpdateCartItemSuccess() = runBlocking {
        val momId = "mom_alice"
        val skuId = "sku_prenatal_batchA"
        val request = UpdateCartItemRequest(qty = 5)
        
        val result = cartService.updateCartItem(momId, skuId, request)
        
        assertTrue(result.success)
        assertEquals("Cart retrieved successfully", result.message)
    }
    
    @Test
    fun testUpdateCartItemInvalidQuantity() = runBlocking {
        val momId = "mom_alice"
        val skuId = "sku_prenatal_batchA"
        val request = UpdateCartItemRequest(qty = 0)
        
        val result = cartService.updateCartItem(momId, skuId, request)
        
        assertFalse(result.success)
        assertEquals("Quantity must be greater than 0", result.message)
    }
    
    @Test
    fun testUpdateCartItemNotFound() = runBlocking {
        val momId = "mom_alice"
        val skuId = "sku_nonexistent"
        val request = UpdateCartItemRequest(qty = 5)
        
        val result = cartService.updateCartItem(momId, skuId, request)
        
        assertFalse(result.success)
        assertEquals("Cart item not found", result.message)
    }
    
    @Test
    fun testRemoveItemFromCartSuccess() = runBlocking {
        val momId = "mom_alice"
        val skuId = "sku_prenatal_batchA"
        
        val result = cartService.removeCartItem(momId, skuId)
        
        assertTrue(result.success)
        assertEquals("Cart retrieved successfully", result.message)
    }
    
    @Test
    fun testRemoveItemFromCartNotFound() = runBlocking {
        val momId = "mom_alice"
        val skuId = "sku_nonexistent"
        
        val result = cartService.removeCartItem(momId, skuId)
        
        assertFalse(result.success)
        assertEquals("Cart item not found", result.message)
    }
    
    @Test
    fun testClearCartSuccess() = runBlocking {
        val momId = "mom_alice"
        
        val result = cartService.clearCart(momId)
        
        assertTrue(result.success)
        assertEquals("Cart retrieved successfully", result.message)
    }
    
    @Test
    fun testClearCartEmpty() = runBlocking {
        val momId = "mom_empty"
        
        val result = cartService.clearCart(momId)
        
        assertTrue(result.success)
        assertEquals("Cart retrieved successfully", result.message)
    }
    
    @Test
    fun testGetCartUnauthorizedMom() = runBlocking {
        val momId = "mom_unauthorized"
        
        val result = cartService.getCart(momId)
        
        assertFalse(result.success)
        assertEquals("Access denied. Mom must be authorized to access this feature.", result.message)
    }
    
    @Test
    fun testAddToCartUnauthorizedMom() = runBlocking {
        val momId = "mom_unauthorized"
        val request = AddToCartRequest(
            skuId = "sku_prenatal_batchA",
            qty = 2,
            offerId = "offer_prn_happy"
        )
        
        val result = cartService.addToCart(momId, request)
        
        assertFalse(result.success)
        assertEquals("Access denied. Mom must be authorized to access this feature.", result.message)
    }
    
    @Test
    fun testUpdateCartItemUnauthorizedMom() = runBlocking {
        val momId = "mom_unauthorized"
        val skuId = "sku_prenatal_batchA"
        val request = UpdateCartItemRequest(qty = 5)
        
        val result = cartService.updateCartItem(momId, skuId, request)
        
        assertFalse(result.success)
        assertEquals("Access denied. Mom must be authorized to access this feature.", result.message)
    }
    
    @Test
    fun testRemoveCartItemUnauthorizedMom() = runBlocking {
        val momId = "mom_unauthorized"
        val skuId = "sku_prenatal_batchA"
        
        val result = cartService.removeCartItem(momId, skuId)
        
        assertFalse(result.success)
        assertEquals("Access denied. Mom must be authorized to access this feature.", result.message)
    }
    
    @Test
    fun testClearCartUnauthorizedMom() = runBlocking {
        val momId = "mom_unauthorized"
        
        val result = cartService.clearCart(momId)
        
        assertFalse(result.success)
        assertEquals("Access denied. Mom must be authorized to access this feature.", result.message)
    }
    
    @Test
    fun testGetCartMomNotFound() = runBlocking {
        val momId = "mom_nonexistent"
        
        val result = cartService.getCart(momId)
        
        assertFalse(result.success)
        assertEquals("Mom not found", result.message)
    }
}
package com.evelolvetech.service

import com.evelolvetech.data.models.*
import com.evelolvetech.data.repository.api.mom.ecommerce.CartRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.OrderRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.SkuOfferRepository
import com.evelolvetech.data.requests.CreateOrderRequest
import com.evelolvetech.data.requests.OrderItemRequest
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.*

class OrderServiceTest {

    private lateinit var orderService: OrderService
    private lateinit var mockOrderRepository: OrderRepository
    private lateinit var mockCartRepository: CartRepository
    private lateinit var mockSkuOfferRepository: SkuOfferRepository
    private lateinit var mockSkuRepository: com.evelolvetech.data.repository.api.mom.ecommerce.SkuRepository

    @BeforeEach
    fun setUp() {
        mockOrderRepository = mockk()
        mockCartRepository = mockk()
        mockSkuOfferRepository = mockk()
        mockSkuRepository = mockk()
        orderService = OrderService(mockOrderRepository, mockCartRepository, mockSkuOfferRepository, mockSkuRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun testGetOrdersByMomIdSuccess() = runBlocking {
        val momId = "mom_alice"
        val page = 0
        val size = 20

        val mockOrder = Order(
            id = "order_1",
            orderNo = "ORD-123",
            momId = momId,
            momRef = "/moms/$momId",
            uid = momId,
            subtotal = 100.0,
            grandTotal = 100.0,
            items = listOf(
                OrderItem(
                    skuId = "sku_1",
                    skuRef = "/sku-offers/sku_1",
                    productId = "prod_1",
                    productRef = "/products/prod_1",
                    sellerId = "seller_1",
                    sellerRef = "/sellers/seller_1",
                    qty = 2,
                    unitPrice = 50.0,
                    lineTotal = 100.0,
                    productName = "Test Product"
                )
            )
        )

        coEvery { mockOrderRepository.getOrdersByMomId(momId, page, size) } returns listOf(mockOrder)

        val result = orderService.getOrdersByMomId(momId, page, size)

        assertTrue(result.success)
        assertEquals("Orders retrieved successfully", result.message)
        assertNotNull(result.data)
        assertEquals(1, result.data!!.size)
        assertEquals("order_1", result.data!![0].id)
        assertEquals("ORD-123", result.data!![0].orderNo)
        assertEquals(momId, result.data!![0].momId)

        coVerify { mockOrderRepository.getOrdersByMomId(momId, page, size) }
    }

    @Test
    fun testGetOrdersByMomIdEmpty() = runBlocking {
        val momId = "mom_alice"
        val page = 0
        val size = 20

        coEvery { mockOrderRepository.getOrdersByMomId(momId, page, size) } returns emptyList()

        val result = orderService.getOrdersByMomId(momId, page, size)

        assertTrue(result.success)
        assertEquals("Orders retrieved successfully", result.message)
        assertNotNull(result.data)
        assertTrue(result.data!!.isEmpty())

        coVerify { mockOrderRepository.getOrdersByMomId(momId, page, size) }
    }

    @Test
    fun testGetOrdersByMomIdError() = runBlocking {
        val momId = "mom_alice"
        val page = 0
        val size = 20

        coEvery { mockOrderRepository.getOrdersByMomId(momId, page, size) } throws Exception("Database error")

        val result = orderService.getOrdersByMomId(momId, page, size)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("Error retrieving orders"))
        assertNull(result.data)

        coVerify { mockOrderRepository.getOrdersByMomId(momId, page, size) }
    }

    @Test
    fun testGetOrderByIdSuccess() = runBlocking {
        val orderId = "order_1"
        val momId = "mom_alice"

        val mockOrder = Order(
            id = orderId,
            orderNo = "ORD-123",
            momId = momId,
            momRef = "/moms/$momId",
            uid = momId,
            subtotal = 100.0,
            grandTotal = 100.0,
            items = listOf(
                OrderItem(
                    skuId = "sku_1",
                    skuRef = "/sku-offers/sku_1",
                    productId = "prod_1",
                    productRef = "/products/prod_1",
                    sellerId = "seller_1",
                    sellerRef = "/sellers/seller_1",
                    qty = 2,
                    unitPrice = 50.0,
                    lineTotal = 100.0,
                    productName = "Test Product"
                )
            )
        )

        coEvery { mockOrderRepository.getOrderById(orderId) } returns mockOrder

        val result = orderService.getOrderById(orderId, momId)

        assertTrue(result.success)
        assertEquals("Order retrieved successfully", result.message)
        assertNotNull(result.data)
        assertEquals(orderId, result.data!!.id)
        assertEquals("ORD-123", result.data!!.orderNo)
        assertEquals(momId, result.data!!.momId)

        coVerify { mockOrderRepository.getOrderById(orderId) }
    }

    @Test
    fun testGetOrderByIdNotFound() = runBlocking {
        val orderId = "order_nonexistent"
        val momId = "mom_alice"

        coEvery { mockOrderRepository.getOrderById(orderId) } returns null

        val result = orderService.getOrderById(orderId, momId)

        assertFalse(result.success)
        assertEquals("Order not found", result.message)
        assertNull(result.data)

        coVerify { mockOrderRepository.getOrderById(orderId) }
    }

    @Test
    fun testGetOrderByIdAccessDenied() = runBlocking {
        val orderId = "order_1"
        val momId = "mom_alice"
        val otherMomId = "mom_beth"

        val mockOrder = Order(
            id = orderId,
            orderNo = "ORD-123",
            momId = otherMomId,
            momRef = "/moms/$otherMomId",
            uid = otherMomId,
            subtotal = 100.0,
            grandTotal = 100.0,
            items = emptyList()
        )

        coEvery { mockOrderRepository.getOrderById(orderId) } returns mockOrder

        val result = orderService.getOrderById(orderId, momId)

        assertFalse(result.success)
        assertEquals("Access denied: Order does not belong to user", result.message)
        assertNull(result.data)

        coVerify { mockOrderRepository.getOrderById(orderId) }
    }

    @Test
    fun testCreateOrderFromCartSuccess() = runBlocking {
        val momId = "mom_alice"

        val mockCart = Cart(
            id = "cart_1",
            momId = momId,
            items = listOf(
                CartItem(
                    skuId = "sku_1",
                    qty = 2,
                    priceSnapshot = 50.0,
                    offerId = "offer_1",
                    skuRef = "/sku-offers/sku_1",
                    offerRef = "/offers/offer_1"
                )
            )
        )

        val mockSkuOffer = SkuOffer(
            id = "offer_1",
            skuId = "sku_1",
            skuRef = "/sku-offers/sku_1",
            sellerId = "seller_1",
            sellerRef = "/sellers/seller_1",
            listPrice = 60.0,
            salePrice = 50.0
        )

        val mockSku = Sku(
            id = "sku_1",
            productId = "prod_1",
            productRef = "/products/prod_1",
            skuCode = "SKU001",
            title = "Test Product"
        )

        coEvery { mockCartRepository.getCartByMomId(momId) } returns mockCart
        coEvery { mockSkuOfferRepository.getSkuOfferById("offer_1") } returns mockSkuOffer
        coEvery { mockSkuRepository.getSkuById("sku_1") } returns mockSku
        coEvery { mockOrderRepository.createOrder(any()) } returns true
        coEvery { mockCartRepository.clearCart(momId) } returns true

        val result = orderService.createOrderFromCart(momId)

        assertTrue(result.success)
        assertEquals("Order created successfully", result.message)
        assertNotNull(result.data)
        assertEquals(momId, result.data!!.momId)
        assertEquals(100.0, result.data!!.subtotal)
        assertEquals(100.0, result.data!!.grandTotal)
        assertEquals(1, result.data!!.items.size)

        coVerify { mockCartRepository.getCartByMomId(momId) }
        coVerify { mockSkuOfferRepository.getSkuOfferById("offer_1") }
        coVerify { mockOrderRepository.createOrder(any()) }
        coVerify { mockCartRepository.clearCart(momId) }
    }

    @Test
    fun testCreateOrderFromCartEmpty() = runBlocking {
        val momId = "mom_alice"

        coEvery { mockCartRepository.getCartByMomId(momId) } returns null

        val result = orderService.createOrderFromCart(momId)

        assertFalse(result.success)
        assertEquals("Cart is empty", result.message)
        assertNull(result.data)

        coVerify { mockCartRepository.getCartByMomId(momId) }
    }

    @Test
    fun testCreateOrderFromRequestSuccess() = runBlocking {
        val momId = "mom_alice"
        val request = CreateOrderRequest(
            items = listOf(
                OrderItemRequest(skuId = "sku_1", qty = 2)
            )
        )

        val mockSkuOffer = SkuOffer(
            id = "offer_1",
            skuId = "sku_1",
            skuRef = "/sku-offers/sku_1",
            sellerId = "seller_1",
            sellerRef = "/sellers/seller_1",
            listPrice = 60.0,
            salePrice = 50.0
        )

        val mockSku = Sku(
            id = "sku_1",
            productId = "prod_1",
            productRef = "/products/prod_1",
            skuCode = "SKU001",
            title = "Test Product"
        )

        coEvery { mockSkuOfferRepository.getBestOfferForSku("sku_1") } returns mockSkuOffer
        coEvery { mockSkuRepository.getSkuById("sku_1") } returns mockSku
        coEvery { mockOrderRepository.createOrder(any()) } returns true

        val result = orderService.createOrderFromRequest(momId, request)

        assertTrue(result.success)
        assertEquals("Order created successfully", result.message)
        assertNotNull(result.data)
        assertEquals(momId, result.data!!.momId)
        assertEquals(100.0, result.data!!.subtotal)
        assertEquals(100.0, result.data!!.grandTotal)
        assertEquals(1, result.data!!.items.size)

        coVerify { mockSkuOfferRepository.getBestOfferForSku("sku_1") }
        coVerify { mockOrderRepository.createOrder(any()) }
    }

    @Test
    fun testCreateOrderFromRequestEmptyItems() = runBlocking {
        val momId = "mom_alice"
        val request = CreateOrderRequest(items = emptyList())

        val result = orderService.createOrderFromRequest(momId, request)

        assertFalse(result.success)
        assertEquals("Order items cannot be empty", result.message)
        assertNull(result.data)

        coVerify(exactly = 0) { mockSkuOfferRepository.getBestOfferForSku(any()) }
        coVerify(exactly = 0) { mockOrderRepository.createOrder(any()) }
    }

    @Test
    fun testCreateOrderFromRequestSkuNotFound() = runBlocking {
        val momId = "mom_alice"
        val request = CreateOrderRequest(
            items = listOf(
                OrderItemRequest(skuId = "sku_nonexistent", qty = 2)
            )
        )

        coEvery { mockSkuOfferRepository.getBestOfferForSku("sku_nonexistent") } returns null

        val result = orderService.createOrderFromRequest(momId, request)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("No offers found for SKU"))
        assertNull(result.data)

        coVerify { mockSkuOfferRepository.getBestOfferForSku("sku_nonexistent") }
        coVerify(exactly = 0) { mockOrderRepository.createOrder(any()) }
    }

    @Test
    fun testCreateOrderFromRequestWithOfferId() = runBlocking {
        val momId = "mom_alice"
        val request = CreateOrderRequest(
            items = listOf(
                OrderItemRequest(skuId = "sku_1", qty = 2, offerId = "offer_1")
            )
        )

        val mockSkuOffer = SkuOffer(
            id = "offer_1",
            skuId = "sku_1",
            skuRef = "/sku-offers/sku_1",
            sellerId = "seller_1",
            sellerRef = "/sellers/seller_1",
            listPrice = 60.0,
            salePrice = 50.0
        )

        val mockSku = Sku(
            id = "sku_1",
            productId = "prod_1",
            productRef = "/products/prod_1",
            skuCode = "SKU001",
            title = "Test Product"
        )

        coEvery { mockSkuOfferRepository.getSkuOfferById("offer_1") } returns mockSkuOffer
        coEvery { mockSkuRepository.getSkuById("sku_1") } returns mockSku
        coEvery { mockOrderRepository.createOrder(any()) } returns true

        val result = orderService.createOrderFromRequest(momId, request)

        assertTrue(result.success)
        assertEquals("Order created successfully", result.message)
        assertNotNull(result.data)
        assertEquals(momId, result.data!!.momId)
        assertEquals(100.0, result.data!!.subtotal)
        assertEquals(100.0, result.data!!.grandTotal)
        assertEquals(1, result.data!!.items.size)

        coVerify { mockSkuOfferRepository.getSkuOfferById("offer_1") }
        coVerify { mockOrderRepository.createOrder(any()) }
    }

    @Test
    fun testCreateOrderFromRequestWithMismatchedOfferId() = runBlocking {
        val momId = "mom_alice"
        val request = CreateOrderRequest(
            items = listOf(
                OrderItemRequest(skuId = "sku_1", qty = 2, offerId = "offer_2")
            )
        )

        val mockSkuOffer = SkuOffer(
            id = "offer_2",
            skuId = "sku_2",
            skuRef = "/sku-offers/sku_2",
            sellerId = "seller_1",
            sellerRef = "/sellers/seller_1",
            listPrice = 60.0,
            salePrice = 50.0
        )

        coEvery { mockSkuOfferRepository.getSkuOfferById("offer_2") } returns mockSkuOffer

        val result = orderService.createOrderFromRequest(momId, request)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("SKU offer offer_2 does not belong to SKU sku_1"))
        assertNull(result.data)

        coVerify { mockSkuOfferRepository.getSkuOfferById("offer_2") }
        coVerify(exactly = 0) { mockSkuRepository.getSkuById(any()) }
    }

    @Test
    fun testUpdateOrderStatusSuccess() = runBlocking {
        val orderId = "order_1"
        val momId = "mom_alice"
        val status = "CONFIRMED"

        val mockOrder = Order(
            id = orderId,
            orderNo = "ORD-123",
            momId = momId,
            momRef = "/moms/$momId",
            uid = momId,
            subtotal = 100.0,
            grandTotal = 100.0,
            items = emptyList()
        )

        coEvery { mockOrderRepository.getOrderById(orderId) } returns mockOrder
        coEvery { mockOrderRepository.updateOrderStatus(orderId, status) } returns true

        val result = orderService.updateOrderStatus(orderId, momId, status)

        assertTrue(result.success)
        assertEquals("Order status updated successfully", result.message)

        coVerify { mockOrderRepository.getOrderById(orderId) }
        coVerify { mockOrderRepository.updateOrderStatus(orderId, status) }
    }

    @Test
    fun testUpdateOrderStatusNotFound() = runBlocking {
        val orderId = "order_nonexistent"
        val momId = "mom_alice"
        val status = "CONFIRMED"

        coEvery { mockOrderRepository.getOrderById(orderId) } returns null

        val result = orderService.updateOrderStatus(orderId, momId, status)

        assertFalse(result.success)
        assertEquals("Order not found", result.message)

        coVerify { mockOrderRepository.getOrderById(orderId) }
        coVerify(exactly = 0) { mockOrderRepository.updateOrderStatus(any(), any()) }
    }

    @Test
    fun testUpdateOrderStatusAccessDenied() = runBlocking {
        val orderId = "order_1"
        val momId = "mom_alice"
        val otherMomId = "mom_beth"
        val status = "CONFIRMED"

        val mockOrder = Order(
            id = orderId,
            orderNo = "ORD-123",
            momId = otherMomId,
            momRef = "/moms/$otherMomId",
            uid = otherMomId,
            subtotal = 100.0,
            grandTotal = 100.0,
            items = emptyList()
        )

        coEvery { mockOrderRepository.getOrderById(orderId) } returns mockOrder

        val result = orderService.updateOrderStatus(orderId, momId, status)

        assertFalse(result.success)
        assertEquals("Access denied: Order does not belong to user", result.message)

        coVerify { mockOrderRepository.getOrderById(orderId) }
        coVerify(exactly = 0) { mockOrderRepository.updateOrderStatus(any(), any()) }
    }

    @Test
    fun testUpdateOrderStatusInvalidStatus() = runBlocking {
        val orderId = "order_1"
        val momId = "mom_alice"
        val status = "INVALID_STATUS"

        val mockOrder = Order(
            id = orderId,
            orderNo = "ORD-123",
            momId = momId,
            momRef = "/moms/$momId",
            uid = momId,
            subtotal = 100.0,
            grandTotal = 100.0,
            items = emptyList()
        )

        coEvery { mockOrderRepository.getOrderById(orderId) } returns mockOrder

        val result = orderService.updateOrderStatus(orderId, momId, status)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("Invalid status"))
        assertTrue(result.message!!.contains("PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED"))

        coVerify { mockOrderRepository.getOrderById(orderId) }
        coVerify(exactly = 0) { mockOrderRepository.updateOrderStatus(any(), any()) }
    }

    @Test
    fun testUpdateOrderStatusDatabaseError() = runBlocking {
        val orderId = "order_1"
        val momId = "mom_alice"
        val status = "CONFIRMED"

        val mockOrder = Order(
            id = orderId,
            orderNo = "ORD-123",
            momId = momId,
            momRef = "/moms/$momId",
            uid = momId,
            subtotal = 100.0,
            grandTotal = 100.0,
            items = emptyList()
        )

        coEvery { mockOrderRepository.getOrderById(orderId) } returns mockOrder
        coEvery { mockOrderRepository.updateOrderStatus(orderId, status) } returns false

        val result = orderService.updateOrderStatus(orderId, momId, status)

        assertFalse(result.success)
        assertEquals("Failed to update order status", result.message)

        coVerify { mockOrderRepository.getOrderById(orderId) }
        coVerify { mockOrderRepository.updateOrderStatus(orderId, status) }
    }
}

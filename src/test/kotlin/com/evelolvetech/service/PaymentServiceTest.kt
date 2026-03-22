package com.evelolvetech.service

import com.evelolvetech.data.models.Order
import com.evelolvetech.data.models.Payment
import com.evelolvetech.data.requests.CreatePaymentRequest
import com.evelolvetech.mocks.MockOrderRepository
import com.evelolvetech.mocks.MockPaymentRepository
import com.evelolvetech.service.mom.ecommerce.PaymentService
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class PaymentServiceTest {

    private val mockPaymentRepository = MockPaymentRepository()
    private val mockOrderRepository = MockOrderRepository()
    private val paymentService = PaymentService(mockPaymentRepository, mockOrderRepository)

    private val testOrder = Order(
        id = "order_001",
        orderNo = "ORD-001",
        momId = "mom_beth",
        momRef = "/moms/mom_beth",
        uid = "mom_beth",
        subtotal = 59.97,
        grandTotal = 59.97,
        status = "PENDING",
        items = emptyList()
    )

    private val cancelledOrder = Order(
        id = "order_cancelled",
        orderNo = "ORD-002",
        momId = "mom_beth",
        momRef = "/moms/mom_beth",
        uid = "mom_beth",
        subtotal = 20.00,
        grandTotal = 20.00,
        status = "CANCELLED",
        items = emptyList()
    )

    init {
        mockOrderRepository.orders["order_001"] = testOrder
        mockOrderRepository.orders["order_cancelled"] = cancelledOrder
    }

    @Test
    fun testInitiatePaymentSuccess() = runBlocking {
        val request = CreatePaymentRequest(
            orderId = "order_001",
            provider = "STRIPE",
            method = "CREDIT_CARD",
            amount = 59.97
        )

        val result = paymentService.initiatePayment("mom_beth", request)

        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals("PENDING", result.data!!.status)
        assertEquals("STRIPE", result.data!!.provider)
        assertEquals(59.97, result.data!!.amount)
    }

    @Test
    fun testInitiatePaymentOrderNotFound() = runBlocking {
        val request = CreatePaymentRequest(
            orderId = "nonexistent",
            provider = "STRIPE",
            method = "CREDIT_CARD",
            amount = 10.0
        )

        val result = paymentService.initiatePayment("mom_beth", request)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("not found", ignoreCase = true))
    }

    @Test
    fun testInitiatePaymentWrongUser() = runBlocking {
        val request = CreatePaymentRequest(
            orderId = "order_001",
            provider = "STRIPE",
            method = "CREDIT_CARD",
            amount = 59.97
        )

        val result = paymentService.initiatePayment("mom_alice", request)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("Access denied"))
    }

    @Test
    fun testInitiatePaymentCancelledOrder() = runBlocking {
        val request = CreatePaymentRequest(
            orderId = "order_cancelled",
            provider = "STRIPE",
            method = "CREDIT_CARD",
            amount = 20.0
        )

        val result = paymentService.initiatePayment("mom_beth", request)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("cancelled", ignoreCase = true))
    }

    @Test
    fun testInitiatePaymentInvalidProvider() = runBlocking {
        val request = CreatePaymentRequest(
            orderId = "order_001",
            provider = "BITCOIN",
            method = "CREDIT_CARD",
            amount = 59.97
        )

        val result = paymentService.initiatePayment("mom_beth", request)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("Invalid payment provider"))
    }

    @Test
    fun testInitiatePaymentInvalidMethod() = runBlocking {
        val request = CreatePaymentRequest(
            orderId = "order_001",
            provider = "STRIPE",
            method = "CRYPTO",
            amount = 59.97
        )

        val result = paymentService.initiatePayment("mom_beth", request)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("Invalid payment method"))
    }

    @Test
    fun testInitiatePaymentZeroAmount() = runBlocking {
        val request = CreatePaymentRequest(
            orderId = "order_001",
            provider = "STRIPE",
            method = "CREDIT_CARD",
            amount = 0.0
        )

        val result = paymentService.initiatePayment("mom_beth", request)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("greater than zero"))
    }

    @Test
    fun testAuthorizePaymentSuccess() = runBlocking {
        val payment = Payment(
            id = "pay_001",
            orderId = "order_001",
            orderRef = "/orders/order_001",
            uid = "mom_beth",
            provider = "STRIPE",
            method = "CREDIT_CARD",
            amount = 59.97,
            status = "PENDING"
        )
        mockPaymentRepository.payments["pay_001"] = payment

        val result = paymentService.authorizePayment("pay_001", "mom_beth", "txn_ref_123")

        assertTrue(result.success)
        assertEquals("AUTHORIZED", mockPaymentRepository.payments["pay_001"]!!.status)
    }

    @Test
    fun testAuthorizePaymentWrongStatus() = runBlocking {
        val payment = Payment(
            id = "pay_auth_already",
            orderId = "order_001",
            orderRef = "/orders/order_001",
            uid = "mom_beth",
            provider = "STRIPE",
            method = "CREDIT_CARD",
            amount = 59.97,
            status = "AUTHORIZED"
        )
        mockPaymentRepository.payments["pay_auth_already"] = payment

        val result = paymentService.authorizePayment("pay_auth_already", "mom_beth", "txn_ref")

        assertFalse(result.success)
        assertTrue(result.message!!.contains("PENDING"))
    }

    @Test
    fun testCapturePaymentSuccess() = runBlocking {
        val payment = Payment(
            id = "pay_capture",
            orderId = "order_001",
            orderRef = "/orders/order_001",
            uid = "mom_beth",
            provider = "STRIPE",
            method = "CREDIT_CARD",
            amount = 59.97,
            status = "AUTHORIZED",
            transactionRef = "txn_ref_123"
        )
        mockPaymentRepository.payments["pay_capture"] = payment

        val result = paymentService.capturePayment("pay_capture", "mom_beth")

        assertTrue(result.success)
        assertEquals("CAPTURED", mockPaymentRepository.payments["pay_capture"]!!.status)
        assertEquals("CONFIRMED", mockOrderRepository.orders["order_001"]!!.status)
    }

    @Test
    fun testRefundPaymentSuccess() = runBlocking {
        val payment = Payment(
            id = "pay_refund",
            orderId = "order_001",
            orderRef = "/orders/order_001",
            uid = "mom_beth",
            provider = "STRIPE",
            method = "CREDIT_CARD",
            amount = 59.97,
            status = "CAPTURED",
            transactionRef = "txn_ref_123"
        )
        mockPaymentRepository.payments["pay_refund"] = payment
        mockOrderRepository.orders["order_001"] = testOrder.copy(status = "CONFIRMED")

        val result = paymentService.refundPayment("pay_refund", "mom_beth")

        assertTrue(result.success)
        assertEquals("REFUNDED", mockPaymentRepository.payments["pay_refund"]!!.status)
        assertEquals("CANCELLED", mockOrderRepository.orders["order_001"]!!.status)
    }

    @Test
    fun testGetPaymentHistory() = runBlocking {
        mockPaymentRepository.payments.clear()
        val payment1 = Payment(
            id = "pay_h1", orderId = "order_001", orderRef = "/orders/order_001",
            uid = "mom_beth", provider = "STRIPE", method = "CREDIT_CARD",
            amount = 30.0, status = "CAPTURED"
        )
        val payment2 = Payment(
            id = "pay_h2", orderId = "order_001", orderRef = "/orders/order_001",
            uid = "mom_beth", provider = "FAWRY", method = "WALLET",
            amount = 15.0, status = "PENDING"
        )
        mockPaymentRepository.payments["pay_h1"] = payment1
        mockPaymentRepository.payments["pay_h2"] = payment2

        val result = paymentService.getPaymentHistory("mom_beth")

        assertTrue(result.success)
        assertEquals(2, result.data!!.size)
    }

    @Test
    fun testDuplicatePaymentPrevention() = runBlocking {
        mockPaymentRepository.payments.clear()
        val captured = Payment(
            id = "pay_dup", orderId = "order_001", orderRef = "/orders/order_001",
            uid = "mom_beth", provider = "STRIPE", method = "CREDIT_CARD",
            amount = 59.97, status = "CAPTURED"
        )
        mockPaymentRepository.payments["pay_dup"] = captured

        val request = CreatePaymentRequest(
            orderId = "order_001",
            provider = "PAYPAL",
            method = "WALLET",
            amount = 59.97
        )

        val result = paymentService.initiatePayment("mom_beth", request)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("already been processed"))
    }
}

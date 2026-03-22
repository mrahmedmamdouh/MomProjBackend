package com.evelolvetech.service

import com.evelolvetech.data.models.*
import com.evelolvetech.data.repository.api.mom.ecommerce.CartRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.OrderRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.SkuOfferRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.SkuRepository
import com.evelolvetech.data.requests.CreateOrderRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.data.responses.OrderResponse
import com.evelolvetech.data.responses.OrderItemResponse
import com.evelolvetech.util.Constants
import java.util.*

class OrderService(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val skuOfferRepository: SkuOfferRepository,
    private val skuRepository: SkuRepository
) {

    suspend fun getOrdersByMomId(momId: String, page: Int = 0, size: Int = 20): BasicApiResponse<List<OrderResponse>> {
        return try {
            val orders = orderRepository.getOrdersByMomId(momId, page, size)
            val orderResponses = orders.map { order ->
                val itemResponses = order.items.map { item ->
                    OrderItemResponse(
                        skuId = item.skuId,
                        productId = item.productId,
                        sellerId = item.sellerId,
                        qty = item.qty,
                        unitPrice = item.unitPrice,
                        lineTotal = item.lineTotal,
                        productName = item.productName
                    )
                }
                OrderResponse.fromOrder(order, itemResponses)
            }
            BasicApiResponse(
                success = true,
                data = orderResponses,
                message = "Orders retrieved successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(
                success = false,
                message = "Error retrieving orders: ${e.message}"
            )
        }
    }

    suspend fun getOrderById(orderId: String, momId: String): BasicApiResponse<OrderResponse> {
        return try {
            val order = orderRepository.getOrderById(orderId)
            if (order == null) {
                BasicApiResponse(
                    success = false,
                    message = "Order not found"
                )
            } else if (order.momId != momId) {
                BasicApiResponse(
                    success = false,
                    message = "Access denied: Order does not belong to user"
                )
            } else {
                val itemResponses = order.items.map { item ->
                    OrderItemResponse(
                        skuId = item.skuId,
                        productId = item.productId,
                        sellerId = item.sellerId,
                        qty = item.qty,
                        unitPrice = item.unitPrice,
                        lineTotal = item.lineTotal,
                        productName = item.productName
                    )
                }
                val orderResponse = OrderResponse.fromOrder(order, itemResponses)
                BasicApiResponse(
                    success = true,
                    data = orderResponse,
                    message = "Order retrieved successfully"
                )
            }
        } catch (e: Exception) {
            BasicApiResponse(
                success = false,
                message = "Error retrieving order: ${e.message}"
            )
        }
    }

    suspend fun createOrderFromCart(momId: String): BasicApiResponse<OrderResponse> {
        return try {
            val cart = cartRepository.getCartByMomId(momId)
            if (cart == null || cart.items.isEmpty()) {
                return BasicApiResponse(
                    success = false,
                    message = "Cart is empty"
                )
            }

            val orderItems = mutableListOf<OrderItem>()
            var subtotal = 0.0

            for (cartItem in cart.items) {
                val skuOffer = skuOfferRepository.getSkuOfferById(cartItem.offerId)
                if (skuOffer == null) {
                    return BasicApiResponse(
                        success = false,
                        message = "SKU offer not found: ${cartItem.offerId}"
                    )
                }

                val sku = skuRepository.getSkuById(cartItem.skuId)
                if (sku == null) {
                    return BasicApiResponse(
                        success = false,
                        message = "SKU not found: ${cartItem.skuId}"
                    )
                }

                val lineTotal = cartItem.priceSnapshot * cartItem.qty
                subtotal += lineTotal

                val orderItem = OrderItem(
                    skuId = cartItem.skuId,
                    skuRef = cartItem.skuRef,
                    productId = sku.productId,
                    productRef = sku.productRef,
                    sellerId = skuOffer.sellerId,
                    sellerRef = skuOffer.sellerRef,
                    qty = cartItem.qty,
                    unitPrice = cartItem.priceSnapshot,
                    lineTotal = lineTotal,
                    productName = sku.title
                )
                orderItems.add(orderItem)
            }

            val orderNo = generateOrderNumber()
            val grandTotal = subtotal

            val order = Order(
                orderNo = orderNo,
                momId = momId,
                momRef = "/moms/$momId",
                uid = momId,
                subtotal = subtotal,
                grandTotal = grandTotal,
                items = orderItems
            )

            val orderCreated = orderRepository.createOrder(order)
            if (!orderCreated) {
                return BasicApiResponse(
                    success = false,
                    message = "Failed to create order"
                )
            }

            val cartCleared = cartRepository.clearCart(momId)
            if (!cartCleared) {
                val orderDeleted = orderRepository.deleteOrder(order.id)
                val message = if (orderDeleted) {
                    "Failed to clear cart, order rolled back"
                } else {
                    "Failed to clear cart and rollback order - system may be in inconsistent state"
                }
                return BasicApiResponse(
                    success = false,
                    message = message
                )
            }

            val itemResponses = orderItems.map { item ->
                OrderItemResponse(
                    skuId = item.skuId,
                    productId = item.productId,
                    sellerId = item.sellerId,
                    qty = item.qty,
                    unitPrice = item.unitPrice,
                    lineTotal = item.lineTotal,
                    productName = item.productName
                )
            }

            val orderResponse = OrderResponse.fromOrder(order, itemResponses)
            BasicApiResponse(
                success = true,
                data = orderResponse,
                message = "Order created successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(
                success = false,
                message = "Error creating order: ${e.message}"
            )
        }
    }

    suspend fun createOrderFromRequest(momId: String, request: CreateOrderRequest): BasicApiResponse<OrderResponse> {
        return try {
            if (request.items.isEmpty()) {
                return BasicApiResponse(
                    success = false,
                    message = "Order items cannot be empty"
                )
            }

            val orderItems = mutableListOf<OrderItem>()
            var subtotal = 0.0

            for (itemRequest in request.items) {
                val skuOffer = if (itemRequest.offerId != null) {
                    skuOfferRepository.getSkuOfferById(itemRequest.offerId)
                } else {
                    skuOfferRepository.getBestOfferForSku(itemRequest.skuId)
                }
                
                if (skuOffer == null) {
                    val message = if (itemRequest.offerId != null) {
                        "SKU offer not found: ${itemRequest.offerId}"
                    } else {
                        "No offers found for SKU: ${itemRequest.skuId}"
                    }
                    return BasicApiResponse(
                        success = false,
                        message = message
                    )
                }

                if (itemRequest.offerId != null && skuOffer.skuId != itemRequest.skuId) {
                    return BasicApiResponse(
                        success = false,
                        message = "SKU offer ${itemRequest.offerId} does not belong to SKU ${itemRequest.skuId}"
                    )
                }

                val sku = skuRepository.getSkuById(itemRequest.skuId)
                if (sku == null) {
                    return BasicApiResponse(
                        success = false,
                        message = "SKU not found: ${itemRequest.skuId}"
                    )
                }

                val lineTotal = skuOffer.salePrice * itemRequest.qty
                subtotal += lineTotal

                val orderItem = OrderItem(
                    skuId = itemRequest.skuId,
                    skuRef = "/skus/${itemRequest.skuId}",
                    productId = sku.productId,
                    productRef = sku.productRef,
                    sellerId = skuOffer.sellerId,
                    sellerRef = skuOffer.sellerRef,
                    qty = itemRequest.qty,
                    unitPrice = skuOffer.salePrice,
                    lineTotal = lineTotal,
                    productName = sku.title
                )
                orderItems.add(orderItem)
            }

            val orderNo = generateOrderNumber()
            val grandTotal = subtotal

            val order = Order(
                orderNo = orderNo,
                momId = momId,
                momRef = "/moms/$momId",
                uid = momId,
                subtotal = subtotal,
                grandTotal = grandTotal,
                items = orderItems
            )

            val orderCreated = orderRepository.createOrder(order)
            if (!orderCreated) {
                return BasicApiResponse(
                    success = false,
                    message = "Failed to create order"
                )
            }

            val itemResponses = orderItems.map { item ->
                OrderItemResponse(
                    skuId = item.skuId,
                    productId = item.productId,
                    sellerId = item.sellerId,
                    qty = item.qty,
                    unitPrice = item.unitPrice,
                    lineTotal = item.lineTotal,
                    productName = item.productName
                )
            }

            val orderResponse = OrderResponse.fromOrder(order, itemResponses)
            BasicApiResponse(
                success = true,
                data = orderResponse,
                message = "Order created successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(
                success = false,
                message = "Error creating order: ${e.message}"
            )
        }
    }

    suspend fun updateOrderStatus(orderId: String, momId: String, status: String): BasicApiResponse<Unit> {
        return try {
            val order = orderRepository.getOrderById(orderId)
            if (order == null) {
                BasicApiResponse(
                    success = false,
                    message = "Order not found"
                )
            } else if (order.momId != momId) {
                BasicApiResponse(
                    success = false,
                    message = "Access denied: Order does not belong to user"
                )
            } else {
                val validStatuses = listOf("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED")
                if (status !in validStatuses) {
                    BasicApiResponse(
                        success = false,
                        message = "Invalid status. Must be one of: ${validStatuses.joinToString(", ")}"
                    )
                } else {
                    val updated = orderRepository.updateOrderStatus(orderId, status)
                    if (updated) {
                        BasicApiResponse(
                            success = true,
                            message = "Order status updated successfully"
                        )
                    } else {
                        BasicApiResponse(
                            success = false,
                            message = "Failed to update order status"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            BasicApiResponse(
                success = false,
                message = "Error updating order status: ${e.message}"
            )
        }
    }

    private fun generateOrderNumber(): String {
        val timestamp = System.currentTimeMillis()
        val uuid = java.util.UUID.randomUUID().toString().substring(0, 8)
        return "ORD-${timestamp}-${uuid}"
    }
}

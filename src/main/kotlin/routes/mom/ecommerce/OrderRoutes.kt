package com.evelolvetech.routes.mom.ecommerce

import com.evelolvetech.auth.getCurrentUserIdSafe
import com.evelolvetech.auth.momRoute
import com.evelolvetech.data.requests.CreateOrderRequest
import com.evelolvetech.service.OrderService
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.util.respondWithError
import com.evelolvetech.util.respondWithMapping
import com.evelolvetech.util.respondWithValidationError
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.orderRoutes(orderService: OrderService, momService: MomService) {

    momRoute("/api/orders", momService) {
        get {
            try {
                val momId = call.getCurrentUserIdSafe()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20

                if (page < 0) {
                    call.respondWithValidationError("Page must be non-negative")
                    return@get
                }

                if (size <= 0 || size > 100) {
                    call.respondWithValidationError("Size must be between 1 and 100")
                    return@get
                }

                val result = orderService.getOrdersByMomId(momId, page, size)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error retrieving orders: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        get("/{id}") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val orderId = call.parameters["id"] ?: run {
                    call.respondWithValidationError("Order ID is required")
                    return@get
                }

                val result = orderService.getOrderById(orderId, momId)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error retrieving order: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        post {
            try {
                val momId = call.getCurrentUserIdSafe()
                val request = call.receive<CreateOrderRequest>()

                if (request.items.isEmpty()) {
                    call.respondWithValidationError("Order items cannot be empty")
                    return@post
                }

                for (item in request.items) {
                    if (item.qty <= 0) {
                        call.respondWithValidationError("Quantity must be greater than 0")
                        return@post
                    }
                }

                val result = orderService.createOrderFromRequest(momId, request)
                call.respondWithMapping(result, statusCode = HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respondWithError("Error creating order: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        post("/from-cart") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val result = orderService.createOrderFromCart(momId)
                call.respondWithMapping(result, statusCode = HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respondWithError("Error creating order from cart: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        put("/{id}/status") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val orderId = call.parameters["id"] ?: run {
                    call.respondWithValidationError("Order ID is required")
                    return@put
                }

                val request = call.receive<Map<String, String>>()
                val status = request["status"] ?: run {
                    call.respondWithValidationError("Status is required")
                    return@put
                }

                if (status.isBlank()) {
                    call.respondWithValidationError("Status cannot be blank")
                    return@put
                }

                val result = orderService.updateOrderStatus(orderId, momId, status)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error updating order status: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }
    }
}

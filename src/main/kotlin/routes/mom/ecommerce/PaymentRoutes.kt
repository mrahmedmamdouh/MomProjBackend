package com.evelolvetech.routes.mom.ecommerce

import com.evelolvetech.auth.getCurrentUserIdSafe
import com.evelolvetech.auth.momRoute
import com.evelolvetech.data.requests.AuthorizePaymentRequest
import com.evelolvetech.data.requests.CreatePaymentRequest
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.service.mom.ecommerce.PaymentService
import com.evelolvetech.util.respondWithError
import com.evelolvetech.util.respondWithMapping
import com.evelolvetech.util.respondWithValidationError
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.paymentRoutes(paymentService: PaymentService, momService: MomService) {

    momRoute("/api/payments", momService) {

        get {
            try {
                val momId = call.getCurrentUserIdSafe()
                val result = paymentService.getPaymentHistory(momId)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error retrieving payment history: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        get("/{id}") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val paymentId = call.parameters["id"] ?: run {
                    call.respondWithValidationError("Payment ID is required")
                    return@get
                }

                val result = paymentService.getPaymentById(paymentId, momId)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error retrieving payment: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        get("/order/{orderId}") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val orderId = call.parameters["orderId"] ?: run {
                    call.respondWithValidationError("Order ID is required")
                    return@get
                }

                val result = paymentService.getPaymentsByOrderId(orderId, momId)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error retrieving payments: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        post {
            try {
                val momId = call.getCurrentUserIdSafe()
                val request = call.receive<CreatePaymentRequest>()

                if (request.orderId.isBlank()) {
                    call.respondWithValidationError("Order ID is required")
                    return@post
                }

                if (request.provider.isBlank()) {
                    call.respondWithValidationError("Payment provider is required")
                    return@post
                }

                if (request.method.isBlank()) {
                    call.respondWithValidationError("Payment method is required")
                    return@post
                }

                val result = paymentService.initiatePayment(momId, request)
                call.respondWithMapping(result, statusCode = HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respondWithError("Error initiating payment: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        post("/{id}/authorize") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val paymentId = call.parameters["id"] ?: run {
                    call.respondWithValidationError("Payment ID is required")
                    return@post
                }

                val request = call.receive<AuthorizePaymentRequest>()
                if (request.transactionRef.isBlank()) {
                    call.respondWithValidationError("Transaction reference is required")
                    return@post
                }

                val result = paymentService.authorizePayment(paymentId, momId, request.transactionRef)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error authorizing payment: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        post("/{id}/capture") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val paymentId = call.parameters["id"] ?: run {
                    call.respondWithValidationError("Payment ID is required")
                    return@post
                }

                val result = paymentService.capturePayment(paymentId, momId)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error capturing payment: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        post("/{id}/refund") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val paymentId = call.parameters["id"] ?: run {
                    call.respondWithValidationError("Payment ID is required")
                    return@post
                }

                val result = paymentService.refundPayment(paymentId, momId)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error refunding payment: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }
    }
}

package com.evelolvetech.service.mom.ecommerce

import com.evelolvetech.data.models.Payment
import com.evelolvetech.data.repository.api.mom.ecommerce.OrderRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.PaymentRepository
import com.evelolvetech.data.requests.CreatePaymentRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.data.responses.PaymentResponse
import com.evelolvetech.util.Constants

class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository
) {

    suspend fun initiatePayment(momId: String, request: CreatePaymentRequest): BasicApiResponse<PaymentResponse> {
        return try {
            val order = orderRepository.getOrderById(request.orderId)
                ?: return BasicApiResponse(success = false, message = Constants.ORDER_NOT_FOUND)

            if (order.momId != momId) {
                return BasicApiResponse(success = false, message = "Access denied: Order does not belong to user")
            }

            if (order.status == "CANCELLED") {
                return BasicApiResponse(success = false, message = "Cannot process payment for a cancelled order")
            }

            val existingPayments = paymentRepository.getPaymentsByOrderId(request.orderId)
            val hasCompletedPayment = existingPayments.any { it.status == "CAPTURED" || it.status == "AUTHORIZED" }
            if (hasCompletedPayment) {
                return BasicApiResponse(success = false, message = "Payment has already been processed for this order")
            }

            val validationError = validatePaymentRequest(request)
            if (validationError != null) {
                return BasicApiResponse(success = false, message = validationError)
            }

            val payment = Payment(
                orderId = request.orderId,
                orderRef = "/orders/${request.orderId}",
                uid = momId,
                provider = request.provider.uppercase(),
                method = request.method.uppercase(),
                amount = request.amount,
                status = "PENDING"
            )

            val created = paymentRepository.createPayment(payment)
            if (!created) {
                return BasicApiResponse(success = false, message = Constants.PAYMENT_FAILED)
            }

            BasicApiResponse(
                success = true,
                data = PaymentResponse.fromPayment(payment),
                message = "Payment initiated successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error initiating payment: ${e.message}")
        }
    }

    suspend fun authorizePayment(paymentId: String, momId: String, transactionRef: String): BasicApiResponse<PaymentResponse> {
        return try {
            val payment = paymentRepository.getPaymentById(paymentId)
                ?: return BasicApiResponse(success = false, message = "Payment not found")

            if (payment.uid != momId) {
                return BasicApiResponse(success = false, message = "Access denied: Payment does not belong to user")
            }

            if (payment.status != "PENDING") {
                return BasicApiResponse(success = false, message = "Payment can only be authorized from PENDING status")
            }

            val updated = paymentRepository.updatePaymentStatus(paymentId, "AUTHORIZED", transactionRef)
            if (!updated) {
                return BasicApiResponse(success = false, message = "Failed to authorize payment")
            }

            val updatedPayment = paymentRepository.getPaymentById(paymentId)
            BasicApiResponse(
                success = true,
                data = updatedPayment?.let { PaymentResponse.fromPayment(it) },
                message = "Payment authorized successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error authorizing payment: ${e.message}")
        }
    }

    suspend fun capturePayment(paymentId: String, momId: String): BasicApiResponse<PaymentResponse> {
        return try {
            val payment = paymentRepository.getPaymentById(paymentId)
                ?: return BasicApiResponse(success = false, message = "Payment not found")

            if (payment.uid != momId) {
                return BasicApiResponse(success = false, message = "Access denied: Payment does not belong to user")
            }

            if (payment.status != "AUTHORIZED") {
                return BasicApiResponse(success = false, message = "Payment can only be captured from AUTHORIZED status")
            }

            val updated = paymentRepository.updatePaymentStatus(paymentId, "CAPTURED", payment.transactionRef)
            if (!updated) {
                return BasicApiResponse(success = false, message = "Failed to capture payment")
            }

            orderRepository.updateOrderStatus(payment.orderId, "CONFIRMED")

            val updatedPayment = paymentRepository.getPaymentById(paymentId)
            BasicApiResponse(
                success = true,
                data = updatedPayment?.let { PaymentResponse.fromPayment(it) },
                message = "Payment captured successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error capturing payment: ${e.message}")
        }
    }

    suspend fun refundPayment(paymentId: String, momId: String): BasicApiResponse<PaymentResponse> {
        return try {
            val payment = paymentRepository.getPaymentById(paymentId)
                ?: return BasicApiResponse(success = false, message = "Payment not found")

            if (payment.uid != momId) {
                return BasicApiResponse(success = false, message = "Access denied: Payment does not belong to user")
            }

            if (payment.status != "CAPTURED") {
                return BasicApiResponse(success = false, message = "Only captured payments can be refunded")
            }

            val updated = paymentRepository.updatePaymentStatus(paymentId, "REFUNDED", payment.transactionRef)
            if (!updated) {
                return BasicApiResponse(success = false, message = "Failed to refund payment")
            }

            orderRepository.updateOrderStatus(payment.orderId, "CANCELLED")

            val updatedPayment = paymentRepository.getPaymentById(paymentId)
            BasicApiResponse(
                success = true,
                data = updatedPayment?.let { PaymentResponse.fromPayment(it) },
                message = "Payment refunded successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error refunding payment: ${e.message}")
        }
    }

    suspend fun getPaymentById(paymentId: String, momId: String): BasicApiResponse<PaymentResponse> {
        return try {
            val payment = paymentRepository.getPaymentById(paymentId)
                ?: return BasicApiResponse(success = false, message = "Payment not found")

            if (payment.uid != momId) {
                return BasicApiResponse(success = false, message = "Access denied: Payment does not belong to user")
            }

            BasicApiResponse(success = true, data = PaymentResponse.fromPayment(payment))
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving payment: ${e.message}")
        }
    }

    suspend fun getPaymentsByOrderId(orderId: String, momId: String): BasicApiResponse<List<PaymentResponse>> {
        return try {
            val order = orderRepository.getOrderById(orderId)
                ?: return BasicApiResponse(success = false, message = Constants.ORDER_NOT_FOUND)

            if (order.momId != momId) {
                return BasicApiResponse(success = false, message = "Access denied: Order does not belong to user")
            }

            val payments = paymentRepository.getPaymentsByOrderId(orderId)
            BasicApiResponse(success = true, data = payments.map { PaymentResponse.fromPayment(it) })
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving payments: ${e.message}")
        }
    }

    suspend fun getPaymentHistory(momId: String): BasicApiResponse<List<PaymentResponse>> {
        return try {
            val payments = paymentRepository.getPaymentsByMomUid(momId)
            BasicApiResponse(success = true, data = payments.map { PaymentResponse.fromPayment(it) })
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving payment history: ${e.message}")
        }
    }

    private fun validatePaymentRequest(request: CreatePaymentRequest): String? {
        val validProviders = listOf("STRIPE", "PAYPAL", "FAWRY", "VODAFONE_CASH", "INSTAPAY")
        if (request.provider.uppercase() !in validProviders) {
            return "Invalid payment provider. Must be one of: ${validProviders.joinToString(", ")}"
        }

        val validMethods = listOf("CREDIT_CARD", "DEBIT_CARD", "WALLET", "BANK_TRANSFER", "CASH_ON_DELIVERY")
        if (request.method.uppercase() !in validMethods) {
            return "Invalid payment method. Must be one of: ${validMethods.joinToString(", ")}"
        }

        if (request.amount <= 0) {
            return "Payment amount must be greater than zero"
        }

        return null
    }
}

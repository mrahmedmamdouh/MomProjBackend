package com.evelolvetech.routes.mom.ecommerce

import com.evelolvetech.auth.getCurrentUserIdSafe
import com.evelolvetech.auth.momRoute
import com.evelolvetech.data.requests.AddToCartRequest
import com.evelolvetech.data.requests.CartResponse
import com.evelolvetech.data.requests.UpdateCartItemRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.service.mom.ecommerce.CartService
import com.evelolvetech.util.HttpStatusMapper
import com.evelolvetech.util.respondWithMapping
import com.evelolvetech.util.respondWithError
import com.evelolvetech.util.respondWithValidationError
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.cartRoutes(cartService: CartService, momService: MomService, gson: Gson) {
    momRoute("/api/cart", momService) {
        get {
            try {
                val momId = call.getCurrentUserIdSafe()
                val result = cartService.getCart(momId)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Internal server error: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        post("add") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val request = try {
                    val jsonString = call.receiveText()
                    gson.fromJson(jsonString, AddToCartRequest::class.java)
                } catch (e: Exception) {
                    call.respondWithValidationError("Invalid request data. Please check that all required fields are provided and properly formatted.")
                    return@post
                }
                val result = cartService.addToCart(momId, request)
                call.respondWithMapping(result, statusCode = HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respondWithError("Internal server error: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        put("item/{skuId}") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val skuId = call.parameters["skuId"]
                if (skuId.isNullOrBlank()) {
                    call.respondWithValidationError("SKU ID is required")
                    return@put
                }

                val request = try {
                    val jsonString = call.receiveText()
                    gson.fromJson(jsonString, UpdateCartItemRequest::class.java)
                } catch (e: Exception) {
                    call.respondWithValidationError("Invalid request data. Please check that all required fields are provided and properly formatted.")
                    return@put
                }
                val result = cartService.updateCartItem(momId, skuId, request)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Internal server error: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        delete("item/{skuId}") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val skuId = call.parameters["skuId"]
                if (skuId.isNullOrBlank()) {
                    call.respondWithValidationError("SKU ID is required")
                    return@delete
                }

                val result = cartService.removeCartItem(momId, skuId)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Internal server error: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        delete {
            try {
                val momId = call.getCurrentUserIdSafe()
                val result = cartService.clearCart(momId)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Internal server error: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }
    }
}
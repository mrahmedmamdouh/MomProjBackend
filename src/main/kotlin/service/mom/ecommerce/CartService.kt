package com.evelolvetech.service.mom.ecommerce

import com.evelolvetech.data.models.CartItem
import com.evelolvetech.data.repository.api.mom.ecommerce.CartRepository
import com.evelolvetech.data.repository.api.mom.MomRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.SkuOfferRepository
import com.evelolvetech.data.requests.AddToCartRequest
import com.evelolvetech.data.requests.CartItemResponse
import com.evelolvetech.data.requests.CartResponse
import com.evelolvetech.data.requests.UpdateCartItemRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.util.*

class CartService(
    private val cartRepository: CartRepository,
    private val momRepository: MomRepository,
    private val skuOfferRepository: SkuOfferRepository,
    private val authConfig: com.evelolvetech.util.AuthConfig
) {

    suspend fun getCart(momId: String): BasicApiResponse<CartResponse> {
        return try {
            MomAuthUtil.checkMomAuthorization(momId, momRepository, authConfig) {
                val cart = cartRepository.getCartByMomId(momId) ?: return@checkMomAuthorization BasicApiResponse(
                    success = true,
                    message = "Cart is empty",
                    data = CartResponse(
                        id = "",
                        momId = momId,
                        items = emptyList(),
                        totalItems = 0,
                        totalPrice = 0.0,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )

                val cartItems = cart.items.map { item ->
                    CartItemResponse(
                        skuId = item.skuId,
                        qty = item.qty,
                        priceSnapshot = item.priceSnapshot,
                        offerId = item.offerId,
                        skuRef = item.skuRef,
                        offerRef = item.offerRef,
                        addedAt = item.addedAt,
                        totalPrice = item.priceSnapshot * item.qty
                    )
                }

                val totalItems = cart.items.sumOf { it.qty }
                val totalPrice = cart.items.sumOf { it.priceSnapshot * it.qty }

                BasicApiResponse(
                    success = true,
                    message = "Cart retrieved successfully",
                    data = CartResponse(
                        id = cart.id,
                        momId = cart.momId,
                        items = cartItems,
                        totalItems = totalItems,
                        totalPrice = totalPrice,
                        createdAt = cart.createdAt,
                        updatedAt = cart.updatedAt
                    )
                )
            }
        } catch (e: Exception) {
            BasicApiResponse(
                success = false,
                message = "Failed to retrieve cart: ${e.message}",
                data = null
            )
        }
    }

    suspend fun addToCart(momId: String, request: AddToCartRequest): BasicApiResponse<CartResponse> {
        return try {
            val validationResult = validateAddToCartRequest(request)
            if (validationResult != ValidationEvent.Success) {
                return when (validationResult) {
                    ValidationEvent.ErrorFieldEmpty -> BasicApiResponse(
                        success = false,
                        message = "SKU ID and Offer ID are required",
                        data = null
                    )

                    ValidationEvent.ErrorInvalidQuantity -> BasicApiResponse(
                        success = false,
                        message = "Quantity must be greater than 0",
                        data = null
                    )

                    else -> BasicApiResponse(
                        success = false,
                        message = "Validation failed",
                        data = null
                    )
                }
            }

            MomAuthUtil.checkMomAuthorization(momId, momRepository, authConfig) {
                val offer = skuOfferRepository.getSkuOfferById(request.offerId) ?: return@checkMomAuthorization BasicApiResponse(
                    success = false,
                    message = "Offer not found",
                    data = null
                )

                if (offer.skuId != request.skuId) {
                    return@checkMomAuthorization BasicApiResponse(
                        success = false,
                        message = "Offer does not match SKU",
                        data = null
                    )
                }

                val cartItem = CartItem(
                    skuId = request.skuId,
                    qty = request.qty,
                    priceSnapshot = offer.salePrice,
                    offerId = request.offerId,
                    skuRef = "/skus/${request.skuId}",
                    offerRef = "/skuOffers/${request.offerId}",
                    addedAt = System.currentTimeMillis()
                )

                val success = cartRepository.addItemToCart(momId, cartItem)
                if (!success) {
                    return@checkMomAuthorization BasicApiResponse(
                        success = false,
                        message = "Failed to add item to cart",
                        data = null
                    )
                }

                getCart(momId)
            }
        } catch (e: Exception) {
            BasicApiResponse(
                success = false,
                message = "Failed to add item to cart: ${e.message}",
                data = null
            )
        }
    }

    suspend fun updateCartItem(
        momId: String,
        skuId: String,
        request: UpdateCartItemRequest
    ): BasicApiResponse<CartResponse> {
        return try {
            val validationResult = validateUpdateCartItemRequest(request)
            if (validationResult != ValidationEvent.Success) {
                return when (validationResult) {
                    ValidationEvent.ErrorInvalidQuantity -> BasicApiResponse(
                        success = false,
                        message = "Quantity must be greater than 0",
                        data = null
                    )

                    else -> BasicApiResponse(
                        success = false,
                        message = "Validation failed",
                        data = null
                    )
                }
            }

            MomAuthUtil.checkMomAuthorization(momId, momRepository, authConfig) {
                val success = cartRepository.updateCartItem(momId, skuId, request.qty)
                if (!success) {
                    return@checkMomAuthorization BasicApiResponse(
                        success = false,
                        message = "Cart item not found",
                        data = null
                    )
                }

                getCart(momId)
            }
        } catch (e: CartOperationException) {
            BasicApiResponse(
                success = false,
                message = e.message ?: "Cart operation failed",
                data = null
            )
        } catch (e: Exception) {
            BasicApiResponse(
                success = false,
                message = "Unexpected error during cart update: ${e.message}",
                data = null
            )
        }
    }

    suspend fun removeCartItem(momId: String, skuId: String): BasicApiResponse<CartResponse> {
        return try {
            MomAuthUtil.checkMomAuthorization(momId, momRepository, authConfig) {
                val success = cartRepository.removeItemFromCart(momId, skuId)
                if (!success) {
                    return@checkMomAuthorization BasicApiResponse(
                        success = false,
                        message = "Cart item not found",
                        data = null
                    )
                }

                getCart(momId)
            }
        } catch (e: CartOperationException) {
            BasicApiResponse(
                success = false,
                message = e.message ?: "Cart item removal failed",
                data = null
            )
        } catch (e: Exception) {
            BasicApiResponse(
                success = false,
                message = "Unexpected error during cart item removal: ${e.message}",
                data = null
            )
        }
    }

    suspend fun clearCart(momId: String): BasicApiResponse<CartResponse> {
        return try {
            MomAuthUtil.checkMomAuthorization(momId, momRepository, authConfig) {
                val success = cartRepository.clearCart(momId)
                if (!success) {
                    return@checkMomAuthorization BasicApiResponse(
                        success = false,
                        message = "Failed to clear cart",
                        data = null
                    )
                }

                getCart(momId)
            }
        } catch (e: CartOperationException) {
            BasicApiResponse(
                success = false,
                message = e.message ?: "Cart clear operation failed",
                data = null
            )
        } catch (e: Exception) {
            BasicApiResponse(
                success = false,
                message = "Unexpected error during cart clear: ${e.message}",
                data = null
            )
        }
    }

    private fun validateAddToCartRequest(request: AddToCartRequest): ValidationEvent {
        if (request.skuId.isBlank()) {
            return ValidationEvent.ErrorFieldEmpty
        }

        if (request.qty <= 0) {
            return ValidationEvent.ErrorInvalidQuantity
        }

        if (request.offerId.isBlank()) {
            return ValidationEvent.ErrorFieldEmpty
        }

        return ValidationEvent.Success
    }

    private fun validateUpdateCartItemRequest(request: UpdateCartItemRequest): ValidationEvent {
        if (request.qty <= 0) {
            return ValidationEvent.ErrorInvalidQuantity
        }

        return ValidationEvent.Success
    }

    sealed class ValidationEvent {
        object ErrorFieldEmpty : ValidationEvent()
        object ErrorInvalidQuantity : ValidationEvent()
        object ErrorOfferNotFound : ValidationEvent()
        object ErrorOfferMismatch : ValidationEvent()
        object Success : ValidationEvent()
    }
}

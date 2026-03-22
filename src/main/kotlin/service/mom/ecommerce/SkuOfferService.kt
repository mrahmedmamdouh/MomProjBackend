package com.evelolvetech.service.mom.ecommerce

import com.evelolvetech.data.models.SkuOffer
import com.evelolvetech.data.repository.api.mom.ecommerce.SkuOfferRepository
import com.evelolvetech.data.repository.api.mom.MomRepository
import com.evelolvetech.data.requests.CreateSkuOfferRequest
import com.evelolvetech.data.requests.UpdateSkuOfferRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.util.MomAuthUtil

class SkuOfferService(
    private val skuOfferRepository: SkuOfferRepository,
    private val momRepository: MomRepository,
    private val authConfig: com.evelolvetech.util.AuthConfig
) {
    suspend fun createSkuOffer(request: CreateSkuOfferRequest): Boolean {
        val validationResult = validateCreateSkuOfferRequest(request)
        if (validationResult != ValidationEvent.Success) {
            return false
        }

        val currency = request.currency ?: return false

        val skuOffer = SkuOffer(
            skuId = request.skuId,
            skuRef = "/skus/${request.skuId}",
            sellerId = request.sellerId,
            sellerRef = "/sellers/${request.sellerId}",
            listPrice = request.listPrice,
            salePrice = request.salePrice,
            currency = currency,
            activeFrom = request.activeFrom,
            activeTo = request.activeTo
        )
        return skuOfferRepository.createSkuOffer(skuOffer)
    }

    suspend fun getSkuOfferById(id: String): SkuOffer? {
        return skuOfferRepository.getSkuOfferById(id)
    }

    suspend fun getSkuOffersBySkuId(skuId: String): List<SkuOffer> {
        return skuOfferRepository.getSkuOffersBySkuId(skuId)
    }

    suspend fun getSkuOffersBySellerId(sellerId: String): List<SkuOffer> {
        return skuOfferRepository.getSkuOffersBySellerId(sellerId)
    }

    suspend fun updateSkuOffer(id: String, request: UpdateSkuOfferRequest): Boolean {
        return skuOfferRepository.updateSkuOffer(
            id = id,
            listPrice = request.listPrice,
            salePrice = request.salePrice,
            currency = request.currency,
            isActive = request.isActive,
            activeFrom = request.activeFrom,
            activeTo = request.activeTo
        )
    }

    suspend fun deleteSkuOffer(id: String): Boolean {
        return skuOfferRepository.deleteSkuOffer(id)
    }

    suspend fun getActiveSkuOffers(): List<SkuOffer> {
        return skuOfferRepository.getActiveSkuOffers()
    }

    suspend fun getActiveSkuOffersForMom(momId: String): BasicApiResponse<List<SkuOffer>> {
        return MomAuthUtil.checkMomAuthorization(momId, momRepository, authConfig) {
            val skuOffers = skuOfferRepository.getActiveSkuOffers()
            BasicApiResponse(
                success = true,
                message = "SKU offers retrieved successfully",
                data = skuOffers
            )
        }
    }

    suspend fun getBestOfferForSku(skuId: String): SkuOffer? {
        return skuOfferRepository.getBestOfferForSku(skuId)
    }

    suspend fun getBestOfferForSkuForMom(momId: String, skuId: String): BasicApiResponse<SkuOffer?> {
        return MomAuthUtil.checkMomAuthorization(momId, momRepository, authConfig) {
            val bestOffer = skuOfferRepository.getBestOfferForSku(skuId)
            BasicApiResponse(
                success = true,
                message = if (bestOffer != null) "Best offer found" else "No offers available for this SKU",
                data = bestOffer
            )
        }
    }

    suspend fun getSkuOfferByIdForMom(momId: String, offerId: String): BasicApiResponse<SkuOffer?> {
        return MomAuthUtil.checkMomAuthorization(momId, momRepository, authConfig) {
            val skuOffer = skuOfferRepository.getSkuOfferById(offerId)
            BasicApiResponse(
                success = true,
                message = if (skuOffer != null) "SKU offer found" else "SKU offer not found",
                data = skuOffer
            )
        }
    }

    suspend fun getSkuOffersBySkuIdForMom(momId: String, skuId: String): BasicApiResponse<List<SkuOffer>> {
        return MomAuthUtil.checkMomAuthorization(momId, momRepository, authConfig) {
            val skuOffers = skuOfferRepository.getSkuOffersBySkuId(skuId)
            BasicApiResponse(
                success = true,
                message = "SKU offers retrieved successfully",
                data = skuOffers
            )
        }
    }

    suspend fun getSkuOffersBySellerIdForMom(momId: String, sellerId: String): BasicApiResponse<List<SkuOffer>> {
        return MomAuthUtil.checkMomAuthorization(momId, momRepository, authConfig) {
            val skuOffers = skuOfferRepository.getSkuOffersBySellerId(sellerId)
            BasicApiResponse(
                success = true,
                message = "SKU offers retrieved successfully",
                data = skuOffers
            )
        }
    }

    suspend fun createSkuOfferForAdmin(request: CreateSkuOfferRequest): BasicApiResponse<Unit> {
        val validationResult = validateCreateSkuOfferRequest(request)
        if (validationResult != ValidationEvent.Success) {
            return when (validationResult) {
                ValidationEvent.ErrorFieldEmpty -> BasicApiResponse(
                    success = false,
                    message = "Required fields are missing or empty",
                    data = null
                )
                ValidationEvent.ErrorCurrencyRequired -> BasicApiResponse(
                    success = false,
                    message = "Currency is required",
                    data = null
                )
                ValidationEvent.ErrorInvalidPrice -> BasicApiResponse(
                    success = false,
                    message = "Invalid price values",
                    data = null
                )
                ValidationEvent.ErrorSalePriceHigher -> BasicApiResponse(
                    success = false,
                    message = "Sale price cannot be higher than list price",
                    data = null
                )
                ValidationEvent.Success -> BasicApiResponse(
                    success = true,
                    message = "Validation passed",
                    data = null
                )
            }
        }

        val isSuccessful = createSkuOffer(request)
        return if (isSuccessful) {
            BasicApiResponse(
                success = true,
                message = "SKU offer created successfully",
                data = null
            )
        } else {
            BasicApiResponse(
                success = false,
                message = "Failed to create SKU offer",
                data = null
            )
        }
    }

    suspend fun updateSkuOfferForAdmin(id: String, request: UpdateSkuOfferRequest): BasicApiResponse<Unit> {
        val validationResult = validateUpdateSkuOfferRequest(request)
        if (validationResult != ValidationEvent.Success) {
            return when (validationResult) {
                ValidationEvent.ErrorInvalidPrice -> BasicApiResponse(
                    success = false,
                    message = "Invalid price values",
                    data = null
                )
                ValidationEvent.ErrorSalePriceHigher -> BasicApiResponse(
                    success = false,
                    message = "Sale price cannot be higher than list price",
                    data = null
                )
                else -> BasicApiResponse(
                    success = false,
                    message = "Validation failed",
                    data = null
                )
            }
        }

        val isSuccessful = updateSkuOffer(id, request)
        return if (isSuccessful) {
            BasicApiResponse(
                success = true,
                message = "SKU offer updated successfully",
                data = null
            )
        } else {
            BasicApiResponse(
                success = false,
                message = "Failed to update SKU offer",
                data = null
            )
        }
    }

    suspend fun deleteSkuOfferForAdmin(id: String): BasicApiResponse<Unit> {
        val isSuccessful = deleteSkuOffer(id)
        return if (isSuccessful) {
            BasicApiResponse(
                success = true,
                message = "SKU offer deleted successfully",
                data = null
            )
        } else {
            BasicApiResponse(
                success = false,
                message = "Failed to delete SKU offer",
                data = null
            )
        }
    }

    fun validateCreateSkuOfferRequest(request: CreateSkuOfferRequest): ValidationEvent {
        if (request.skuId.isBlank() || request.sellerId.isBlank()) {
            return ValidationEvent.ErrorFieldEmpty
        }
        if (request.currency.isNullOrBlank()) {
            return ValidationEvent.ErrorCurrencyRequired
        }
        if (request.listPrice <= 0 || request.salePrice <= 0) {
            return ValidationEvent.ErrorInvalidPrice
        }
        if (request.salePrice > request.listPrice) {
            return ValidationEvent.ErrorSalePriceHigher
        }
        return ValidationEvent.Success
    }

    fun validateUpdateSkuOfferRequest(request: UpdateSkuOfferRequest): ValidationEvent {
        if (request.listPrice != null && request.listPrice <= 0) {
            return ValidationEvent.ErrorInvalidPrice
        }
        if (request.salePrice != null && request.salePrice <= 0) {
            return ValidationEvent.ErrorInvalidPrice
        }
        if (request.listPrice != null && request.salePrice != null && request.salePrice > request.listPrice) {
            return ValidationEvent.ErrorSalePriceHigher
        }
        return ValidationEvent.Success
    }

    sealed class ValidationEvent {
        object ErrorFieldEmpty : ValidationEvent()
        object ErrorCurrencyRequired : ValidationEvent()
        object ErrorInvalidPrice : ValidationEvent()
        object ErrorSalePriceHigher : ValidationEvent()
        object Success : ValidationEvent()
    }
}

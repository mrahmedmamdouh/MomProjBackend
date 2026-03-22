package com.evelolvetech.service.mom.ecommerce

import com.evelolvetech.data.models.ProductRating
import com.evelolvetech.data.repository.api.mom.ecommerce.ProductRatingRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.ProductRepository
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.data.responses.ProductRatingResponse
import com.evelolvetech.data.responses.ProductRatingSummaryResponse
import com.evelolvetech.util.Constants

class ProductRatingService(
    private val productRatingRepository: ProductRatingRepository,
    private val productRepository: ProductRepository
) {

    suspend fun createRating(
        momId: String,
        productId: String,
        rating: Int,
        title: String,
        comment: String
    ): BasicApiResponse<ProductRatingResponse> {
        return try {
            productRepository.getProductById(productId)
                ?: return BasicApiResponse(success = false, message = Constants.PRODUCT_NOT_FOUND)

            val validationError = validateRating(rating)
            if (validationError != null) {
                return BasicApiResponse(success = false, message = validationError)
            }

            val existingRating = productRatingRepository.getUserRatingForProduct(productId, momId)
            if (existingRating != null) {
                return BasicApiResponse(success = false, message = "You have already rated this product. Use update instead.")
            }

            val productRating = ProductRating(
                productId = productId,
                uid = momId,
                rating = rating,
                title = title.trim(),
                comment = comment.trim()
            )

            val created = productRatingRepository.createRating(productRating)
            if (!created) {
                return BasicApiResponse(success = false, message = "Failed to submit rating")
            }

            val averageRating = productRatingRepository.getAverageRating(productId)
            val ratingCount = productRatingRepository.getRatingCount(productId)

            BasicApiResponse(
                success = true,
                data = ProductRatingResponse.fromProductRating(productRating, averageRating, ratingCount),
                message = "Rating submitted successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error creating rating: ${e.message}")
        }
    }

    suspend fun updateRating(
        ratingId: String,
        momId: String,
        rating: Int,
        title: String?,
        comment: String?
    ): BasicApiResponse<ProductRatingResponse> {
        return try {
            val existingRating = productRatingRepository.getRatingById(ratingId)
                ?: return BasicApiResponse(success = false, message = "Rating not found")

            if (existingRating.uid != momId) {
                return BasicApiResponse(success = false, message = "Access denied: Rating does not belong to user")
            }

            val validationError = validateRating(rating)
            if (validationError != null) {
                return BasicApiResponse(success = false, message = validationError)
            }

            val updated = productRatingRepository.updateRating(
                id = ratingId,
                rating = rating,
                title = title?.trim(),
                comment = comment?.trim()
            )

            if (!updated) {
                return BasicApiResponse(success = false, message = "Failed to update rating")
            }

            val updatedRating = productRatingRepository.getRatingById(ratingId)
            val averageRating = productRatingRepository.getAverageRating(existingRating.productId)
            val ratingCount = productRatingRepository.getRatingCount(existingRating.productId)

            BasicApiResponse(
                success = true,
                data = updatedRating?.let { ProductRatingResponse.fromProductRating(it, averageRating, ratingCount) },
                message = "Rating updated successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error updating rating: ${e.message}")
        }
    }

    suspend fun deleteRating(ratingId: String, momId: String): BasicApiResponse<Unit> {
        return try {
            val existingRating = productRatingRepository.getRatingById(ratingId)
                ?: return BasicApiResponse(success = false, message = "Rating not found")

            if (existingRating.uid != momId) {
                return BasicApiResponse(success = false, message = "Access denied: Rating does not belong to user")
            }

            val deleted = productRatingRepository.deleteRating(ratingId)
            if (!deleted) {
                return BasicApiResponse(success = false, message = "Failed to delete rating")
            }

            BasicApiResponse(success = true, message = "Rating deleted successfully")
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error deleting rating: ${e.message}")
        }
    }

    suspend fun getRatingsByProductId(productId: String, page: Int, size: Int): BasicApiResponse<ProductRatingSummaryResponse> {
        return try {
            productRepository.getProductById(productId)
                ?: return BasicApiResponse(success = false, message = Constants.PRODUCT_NOT_FOUND)

            val ratings = productRatingRepository.getRatingsByProductId(productId, page, size)
            val averageRating = productRatingRepository.getAverageRating(productId)
            val ratingCount = productRatingRepository.getRatingCount(productId)

            val ratingResponses = ratings.map { ProductRatingResponse.fromProductRating(it, averageRating, ratingCount) }

            BasicApiResponse(
                success = true,
                data = ProductRatingSummaryResponse(
                    productId = productId,
                    averageRating = averageRating,
                    totalRatings = ratingCount,
                    ratings = ratingResponses,
                    page = page,
                    size = size
                )
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving ratings: ${e.message}")
        }
    }

    suspend fun getUserRatingForProduct(productId: String, momId: String): BasicApiResponse<ProductRatingResponse> {
        return try {
            val rating = productRatingRepository.getUserRatingForProduct(productId, momId)
                ?: return BasicApiResponse(success = false, message = "You have not rated this product yet")

            val averageRating = productRatingRepository.getAverageRating(productId)
            val ratingCount = productRatingRepository.getRatingCount(productId)

            BasicApiResponse(
                success = true,
                data = ProductRatingResponse.fromProductRating(rating, averageRating, ratingCount)
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving user rating: ${e.message}")
        }
    }

    suspend fun getMyRatings(momId: String): BasicApiResponse<List<ProductRatingResponse>> {
        return try {
            val ratings = productRatingRepository.getRatingsByUid(momId)
            val responses = ratings.map { rating ->
                val averageRating = productRatingRepository.getAverageRating(rating.productId)
                val ratingCount = productRatingRepository.getRatingCount(rating.productId)
                ProductRatingResponse.fromProductRating(rating, averageRating, ratingCount)
            }
            BasicApiResponse(success = true, data = responses)
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving your ratings: ${e.message}")
        }
    }

    private fun validateRating(rating: Int): String? {
        if (rating < 1 || rating > 5) {
            return "Rating must be between 1 and 5"
        }
        return null
    }
}

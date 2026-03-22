package com.evelolvetech.data.responses

import com.evelolvetech.data.models.ProductRating
import kotlinx.serialization.Serializable

@Serializable
data class ProductRatingResponse(
    val id: String,
    val productId: String,
    val uid: String,
    val rating: Int,
    val title: String,
    val comment: String,
    val productAverageRating: Double,
    val productRatingCount: Int,
    val createdAt: Long
) {
    companion object {
        fun fromProductRating(
            rating: ProductRating,
            averageRating: Double,
            ratingCount: Int
        ) = ProductRatingResponse(
            id = rating.id,
            productId = rating.productId,
            uid = rating.uid,
            rating = rating.rating,
            title = rating.title,
            comment = rating.comment,
            productAverageRating = averageRating,
            productRatingCount = ratingCount,
            createdAt = rating.createdAt
        )
    }
}

@Serializable
data class ProductRatingSummaryResponse(
    val productId: String,
    val averageRating: Double,
    val totalRatings: Int,
    val ratings: List<ProductRatingResponse>,
    val page: Int,
    val size: Int
)

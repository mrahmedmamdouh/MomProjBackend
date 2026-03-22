package com.evelolvetech.mocks

import com.evelolvetech.data.models.ProductRating
import com.evelolvetech.data.repository.api.mom.ecommerce.ProductRatingRepository

class MockProductRatingRepository : ProductRatingRepository {
    val ratings = mutableMapOf<String, ProductRating>()

    override suspend fun createRating(rating: ProductRating): Boolean {
        ratings[rating.id] = rating
        return true
    }

    override suspend fun getRatingById(id: String): ProductRating? {
        return ratings[id]
    }

    override suspend fun getRatingsByProductId(productId: String, page: Int, size: Int): List<ProductRating> {
        return ratings.values
            .filter { it.productId == productId }
            .sortedByDescending { it.createdAt }
            .drop(page * size)
            .take(size)
    }

    override suspend fun getRatingsByUid(uid: String): List<ProductRating> {
        return ratings.values.filter { it.uid == uid }
    }

    override suspend fun updateRating(id: String, rating: Int, title: String?, comment: String?): Boolean {
        val existing = ratings[id] ?: return false
        ratings[id] = existing.copy(
            rating = rating,
            title = title ?: existing.title,
            comment = comment ?: existing.comment
        )
        return true
    }

    override suspend fun deleteRating(id: String): Boolean {
        return ratings.remove(id) != null
    }

    override suspend fun getAverageRating(productId: String): Double {
        val productRatings = ratings.values.filter { it.productId == productId }
        if (productRatings.isEmpty()) return 0.0
        return productRatings.map { it.rating }.average()
    }

    override suspend fun getRatingCount(productId: String): Int {
        return ratings.values.count { it.productId == productId }
    }

    override suspend fun getUserRatingForProduct(productId: String, uid: String): ProductRating? {
        return ratings.values.find { it.productId == productId && it.uid == uid }
    }
}

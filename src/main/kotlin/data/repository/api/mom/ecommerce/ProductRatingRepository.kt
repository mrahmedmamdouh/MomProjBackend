package com.evelolvetech.data.repository.api.mom.ecommerce

import com.evelolvetech.data.models.ProductRating

interface ProductRatingRepository {
    suspend fun createRating(rating: ProductRating): Boolean
    suspend fun getRatingById(id: String): ProductRating?
    suspend fun getRatingsByProductId(productId: String, page: Int = 0, size: Int = 20): List<ProductRating>
    suspend fun getRatingsByUid(uid: String): List<ProductRating>
    suspend fun updateRating(id: String, rating: Int, title: String?, comment: String?): Boolean
    suspend fun deleteRating(id: String): Boolean
    suspend fun getAverageRating(productId: String): Double
    suspend fun getRatingCount(productId: String): Int
    suspend fun getUserRatingForProduct(productId: String, uid: String): ProductRating?
}

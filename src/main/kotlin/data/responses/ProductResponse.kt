package com.evelolvetech.data.responses

import com.evelolvetech.data.models.Product
import kotlinx.serialization.Serializable

@Serializable
data class ProductResponse(
    val id: String,
    val name: String,
    val slug: String,
    val description: String,
    val status: String,
    val defaultSellerId: String,
    val categoryIds: List<String>,
    val minSessionsToPurchase: Int,
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0,
    val createdAt: Long
) {
    companion object {
        fun fromProduct(product: Product, averageRating: Double = 0.0, ratingCount: Int = 0) = ProductResponse(
            id = product.id,
            name = product.name,
            slug = product.slug,
            description = product.description,
            status = product.status,
            defaultSellerId = product.defaultSellerId,
            categoryIds = product.categoryIds,
            minSessionsToPurchase = product.minSessionsToPurchase,
            averageRating = averageRating,
            ratingCount = ratingCount,
            createdAt = product.createdAt
        )
    }
}

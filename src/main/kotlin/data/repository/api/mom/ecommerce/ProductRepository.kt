package com.evelolvetech.data.repository.api.mom.ecommerce

import com.evelolvetech.data.models.Product

interface ProductRepository {
    suspend fun createProduct(product: Product): Boolean
    suspend fun getProductById(id: String): Product?
    suspend fun getAllProducts(page: Int = 0, size: Int = 20): List<Product>
    suspend fun updateProduct(
        id: String,
        name: String?,
        slug: String?,
        description: String?,
        status: String?,
        defaultSellerId: String?,
        defaultSellerRef: String?,
        categoryIds: List<String>?,
        minSessionsToPurchase: Int?
    ): Boolean

    suspend fun deleteProduct(id: String): Boolean
    suspend fun getProductsByCategory(categoryId: String, page: Int = 0, size: Int = 20): List<Product>
    suspend fun searchProducts(query: String, page: Int = 0, size: Int = 20): List<Product>
    suspend fun getProductsByMinSessions(minSessions: Int, page: Int = 0, size: Int = 20): List<Product>
}

package com.evelolvetech.mocks

import com.evelolvetech.data.models.Product
import com.evelolvetech.data.repository.api.mom.ecommerce.ProductRepository

class MockProductRepository : ProductRepository {
    private val products = mutableMapOf<String, Product>()

    init {
        products["prod_baby_essentials"] = Product(
            id = "prod_baby_essentials",
            name = "Baby Essentials Kit",
            slug = "baby-essentials-kit",
            description = "Essential items for baby care",
            defaultSellerId = "seller_happy",
            defaultSellerRef = "/sellers/seller_happy",
            categoryIds = listOf("cat_baby"),
            minSessionsToPurchase = 0,
            createdAt = System.currentTimeMillis()
        )
        products["prod_prenatal"] = Product(
            id = "prod_prenatal",
            name = "Prenatal Vitamins",
            slug = "prenatal-vitamins",
            description = "Essential vitamins for expecting mothers",
            defaultSellerId = "seller_happy",
            defaultSellerRef = "/sellers/seller_happy",
            categoryIds = listOf("cat_fitness", "cat_nutrition"),
            minSessionsToPurchase = 0,
            createdAt = System.currentTimeMillis()
        )
        products["prod_fitness"] = Product(
            id = "prod_fitness",
            name = "Fitness Equipment",
            slug = "fitness-equipment",
            description = "Home fitness equipment for moms",
            defaultSellerId = "seller_essentials",
            defaultSellerRef = "/sellers/seller_essentials",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 0,
            createdAt = System.currentTimeMillis()
        )
    }

    override suspend fun createProduct(product: Product): Boolean {
        products[product.id] = product
        return true
    }

    override suspend fun getProductById(id: String): Product? {
        return products[id]
    }

    override suspend fun getAllProducts(page: Int, size: Int): List<Product> {
        return products.values.toList()
    }

    override suspend fun getProductsByCategory(categoryId: String, page: Int, size: Int): List<Product> {
        return products.values.filter { it.categoryIds.contains(categoryId) }
    }

    override suspend fun searchProducts(query: String, page: Int, size: Int): List<Product> {
        val lowercaseQuery = query.lowercase()
        return products.values.filter {
            it.name.lowercase().contains(lowercaseQuery) ||
                    it.description.lowercase().contains(lowercaseQuery)
        }
    }

    override suspend fun updateProduct(
        id: String,
        name: String?,
        slug: String?,
        description: String?,
        status: String?,
        defaultSellerId: String?,
        defaultSellerRef: String?,
        categoryIds: List<String>?,
        minSessionsToPurchase: Int?
    ): Boolean {
        val product = products[id] ?: return false
        val updatedProduct = product.copy(
            name = name ?: product.name,
            slug = slug ?: product.slug,
            description = description ?: product.description,
            defaultSellerId = defaultSellerId ?: product.defaultSellerId,
            defaultSellerRef = defaultSellerRef ?: product.defaultSellerRef,
            categoryIds = categoryIds ?: product.categoryIds,
            minSessionsToPurchase = minSessionsToPurchase ?: product.minSessionsToPurchase
        )
        products[id] = updatedProduct
        return true
    }

    override suspend fun deleteProduct(id: String): Boolean {
        return products.remove(id) != null
    }

    override suspend fun getProductsByMinSessions(minSessions: Int, page: Int, size: Int): List<Product> {
        return products.values.filter { it.minSessionsToPurchase <= minSessions }
    }
}

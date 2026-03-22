package com.evelolvetech.service

import com.evelolvetech.data.requests.CreateProductRequest
import com.evelolvetech.data.requests.UpdateProductRequest
import com.evelolvetech.mocks.*
import kotlinx.coroutines.runBlocking
import com.evelolvetech.service.mom.ecommerce.ProductService
import kotlin.test.*

class ProductServiceTest {

    private val mockProductRepository = MockProductRepository()
    private val mockSellerRepository = MockSellerRepository()
    private val mockCategoryRepository = MockCategoryRepository()
    private val mockMomRepository = MockMomRepository()
    private val productService = ProductService(mockProductRepository, mockSellerRepository, mockCategoryRepository, mockMomRepository, MockAuthConfig.instance)

    @Test
    fun testCreateProductSuccess() = runBlocking {
        val request = CreateProductRequest(
            name = "Test Product",
            slug = "test-product",
            description = "A test product",
            defaultSellerId = "seller_test",
            categoryIds = listOf("cat_fitness", "cat_nutrition")
        )

        val result = productService.createProduct(request)

        assertTrue(result)
    }

    @Test
    fun testGetProductByIdSuccess() = runBlocking {
        val productId = "prod_baby_essentials"

        val result = productService.getProductById(productId)

        assertNotNull(result)
        assertEquals("Baby Essentials Kit", result?.name)
        assertEquals("baby-essentials-kit", result?.slug)
        assertEquals("seller_happy", result?.defaultSellerId)
    }

    @Test
    fun testGetProductByIdNotFound() = runBlocking {
        val productId = "prod_nonexistent"

        val result = productService.getProductById(productId)

        assertNull(result)
    }

    @Test
    fun testGetAllProducts() = runBlocking {
        val result = productService.getAllProducts()

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertEquals(3, result.size)
    }

    @Test
    fun testGetProductsByCategory() = runBlocking {
        val categoryId = "cat_fitness"

        val result = productService.getProductsByCategory(categoryId)

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertEquals(2, result.size)
        assertTrue(result.any { it.name == "Prenatal Vitamins" })
        assertTrue(result.any { it.name == "Fitness Equipment" })
    }

    @Test
    fun testGetProductsByCategoryNotFound() = runBlocking {
        val categoryId = "cat_nonexistent"

        val result = productService.getProductsByCategory(categoryId)

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testSearchProducts() = runBlocking {
        val query = "prenatal"

        val result = productService.searchProducts(query)

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertEquals(1, result.size)
        assertEquals("Prenatal Vitamins", result.first().name)
    }

    @Test
    fun testSearchProductsNoResults() = runBlocking {
        val query = "nonexistent"

        val result = productService.searchProducts(query)

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testUpdateProductSuccess() = runBlocking {
        val productId = "prod_baby_essentials"
        val request = UpdateProductRequest(
            name = "Updated Baby Essentials",
            slug = "updated-baby-essentials",
            description = "Updated description",
            status = "ACTIVE",
            defaultSellerId = "seller_updated",
            categoryIds = listOf("cat_baby"),
            minSessionsToPurchase = 0
        )

        val result = productService.updateProduct(productId, request)

        assertTrue(result)
    }

    @Test
    fun testUpdateProductNotFound() = runBlocking {
        val productId = "prod_nonexistent"
        val request = UpdateProductRequest(
            name = "Updated Product",
            slug = "updated-product",
            description = "Updated description",
            status = "ACTIVE",
            defaultSellerId = "seller_updated",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 0
        )

        val result = productService.updateProduct(productId, request)

        assertFalse(result)
    }

    @Test
    fun testDeleteProductSuccess() = runBlocking {
        val productId = "prod_baby_essentials"

        val result = productService.deleteProduct(productId)

        assertTrue(result)
    }

    @Test
    fun testDeleteProductNotFound() = runBlocking {
        val productId = "prod_nonexistent"

        val result = productService.deleteProduct(productId)

        assertFalse(result)
    }
}


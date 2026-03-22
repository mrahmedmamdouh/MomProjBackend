package com.evelolvetech.service.mom.ecommerce

import com.evelolvetech.data.models.Product
import com.evelolvetech.data.repository.api.mom.ecommerce.ProductRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.SellerRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.CategoryRepository
import com.evelolvetech.data.repository.api.mom.MomRepository
import com.evelolvetech.data.requests.CreateProductRequest
import com.evelolvetech.data.requests.UpdateProductRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.util.MomAuthUtil
import org.bson.types.ObjectId

class ProductService(
    private val productRepository: ProductRepository,
    private val sellerRepository: SellerRepository,
    private val categoryRepository: CategoryRepository,
    private val momRepository: MomRepository,
    private val authConfig: com.evelolvetech.util.AuthConfig
) {
    suspend fun createProduct(request: CreateProductRequest): Boolean {
        val product = Product(
            id = ObjectId().toString(),
            name = request.name,
            slug = request.slug,
            description = request.description,
            defaultSellerId = request.defaultSellerId,
            defaultSellerRef = "/sellers/${request.defaultSellerId}",
            categoryIds = request.categoryIds,
            minSessionsToPurchase = request.minSessionsToPurchase
        )
        return productRepository.createProduct(product)
    }

    suspend fun getProductById(id: String): Product? {
        return productRepository.getProductById(id)
    }

    suspend fun getAllProducts(page: Int = 0, size: Int = 20): List<Product> {
        return productRepository.getAllProducts(page, size)
    }

    suspend fun getAllProductsForMom(momId: String, page: Int = 0, size: Int = 20): BasicApiResponse<List<Product>> {
        return MomAuthUtil.checkMomAuthorization(momId, momRepository, authConfig) {
            val products = productRepository.getAllProducts(page, size)
            BasicApiResponse(
                success = true,
                message = "Products retrieved successfully",
                data = products
            )
        }
    }

    suspend fun getProductByIdForMom(momId: String, productId: String): BasicApiResponse<Product?> {
        return MomAuthUtil.checkMomAuthorization(momId, momRepository, authConfig) {
            val product = productRepository.getProductById(productId)
            BasicApiResponse(
                success = true,
                message = if (product != null) "Product found" else "Product not found",
                data = product
            )
        }
    }

    suspend fun getProductsByCategoryForMom(momId: String, categoryId: String, page: Int = 0, size: Int = 20): BasicApiResponse<List<Product>> {
        return MomAuthUtil.checkMomAuthorization(momId, momRepository, authConfig) {
            val products = productRepository.getProductsByCategory(categoryId, page, size)
            BasicApiResponse(
                success = true,
                message = "Products retrieved successfully",
                data = products
            )
        }
    }

    suspend fun searchProductsForMom(momId: String, query: String, page: Int = 0, size: Int = 20): BasicApiResponse<List<Product>> {
        return MomAuthUtil.checkMomAuthorization(momId, momRepository, authConfig) {
            val products = productRepository.searchProducts(query, page, size)
            BasicApiResponse(
                success = true,
                message = "Products retrieved successfully",
                data = products
            )
        }
    }

    suspend fun updateProduct(id: String, request: UpdateProductRequest): Boolean {
        val defaultSellerRef = request.defaultSellerId?.let { "/sellers/$it" }
        return productRepository.updateProduct(
            id = id,
            name = request.name,
            slug = request.slug,
            description = request.description,
            status = request.status,
            defaultSellerId = request.defaultSellerId,
            defaultSellerRef = defaultSellerRef,
            categoryIds = request.categoryIds,
            minSessionsToPurchase = request.minSessionsToPurchase
        )
    }

    suspend fun deleteProduct(id: String): Boolean {
        return productRepository.deleteProduct(id)
    }

    suspend fun getProductsByCategory(categoryId: String, page: Int = 0, size: Int = 20): List<Product> {
        return productRepository.getProductsByCategory(categoryId, page, size)
    }

    suspend fun searchProducts(query: String, page: Int = 0, size: Int = 20): List<Product> {
        return productRepository.searchProducts(query, page, size)
    }

    suspend fun getProductsByMinSessions(minSessions: Int, page: Int = 0, size: Int = 20): List<Product> {
        return productRepository.getProductsByMinSessions(minSessions, page, size)
    }

    suspend fun validateCreateProductRequest(request: CreateProductRequest): ValidationEvent {
        if (request.name.isBlank() || request.slug.isBlank() || request.description.isBlank()) {
            return ValidationEvent.ErrorFieldEmpty
        }
        if (request.defaultSellerId.isBlank()) {
            return ValidationEvent.ErrorInvalidSeller
        }
        if (request.categoryIds.isEmpty()) {
            return ValidationEvent.ErrorNoCategories
        }
        if (request.minSessionsToPurchase < 0) {
            return ValidationEvent.ErrorInvalidSessions
        }
        
        val seller = sellerRepository.getSellerById(request.defaultSellerId)
        if (seller == null) {
            return ValidationEvent.ErrorInvalidSeller
        }
        
        val categories = categoryRepository.getCategoriesByIds(request.categoryIds)
        if (categories.size != request.categoryIds.size) {
            return ValidationEvent.ErrorInvalidCategories
        }
        
        return ValidationEvent.Success
    }

    fun validateUpdateProductRequest(request: UpdateProductRequest): ValidationEvent {
        if (request.name?.isBlank() == true || request.slug?.isBlank() == true || request.description?.isBlank() == true) {
            return ValidationEvent.ErrorFieldEmpty
        }
        if (request.defaultSellerId?.isBlank() == true) {
            return ValidationEvent.ErrorInvalidSeller
        }
        if (request.minSessionsToPurchase != null && request.minSessionsToPurchase < 0) {
            return ValidationEvent.ErrorInvalidSessions
        }
        return ValidationEvent.Success
    }

    sealed class ValidationEvent {
        object ErrorFieldEmpty : ValidationEvent()
        object ErrorInvalidSeller : ValidationEvent()
        object ErrorNoCategories : ValidationEvent()
        object ErrorInvalidCategories : ValidationEvent()
        object ErrorInvalidSessions : ValidationEvent()
        object Success : ValidationEvent()
    }
}

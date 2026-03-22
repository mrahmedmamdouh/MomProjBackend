package com.evelolvetech.service.mom.ecommerce

import com.evelolvetech.data.models.Category
import com.evelolvetech.data.repository.api.mom.ecommerce.CategoryRepository
import com.evelolvetech.data.requests.CreateCategoryRequest
import com.evelolvetech.data.requests.UpdateCategoryRequest
import org.bson.types.ObjectId

class CategoryService(
    private val categoryRepository: CategoryRepository
) {
    suspend fun createCategory(request: CreateCategoryRequest): Category? {
        val category = Category(
            id = ObjectId().toString(),
            name = request.name,
            slug = generateSlug(request.name, request.slug)
        )
        return if (categoryRepository.createCategory(category)) category else null
    }

    suspend fun getCategoryById(id: String): Category? {
        return categoryRepository.getCategoryById(id)
    }

    suspend fun getAllCategories(): List<Category> {
        return categoryRepository.getAllCategories()
    }

    suspend fun updateCategory(id: String, request: UpdateCategoryRequest): Boolean {
        return categoryRepository.updateCategory(id, request.name, request.slug)
    }

    suspend fun deleteCategory(id: String): Boolean {
        return categoryRepository.deleteCategory(id)
    }

    suspend fun getCategoriesByIds(ids: List<String>): List<Category> {
        return categoryRepository.getCategoriesByIds(ids)
    }

    suspend fun validateCreateCategoryRequest(request: CreateCategoryRequest): ValidationEvent {
        if (request.name.isBlank()) {
            return ValidationEvent.ErrorFieldEmpty
        }
        
        val existingByName = categoryRepository.getCategoryByName(request.name)
        if (existingByName != null) {
            return ValidationEvent.ErrorDuplicateName
        }
        
        val slug = generateSlug(request.name, request.slug)
        val existingBySlug = categoryRepository.getCategoryBySlug(slug)
        if (existingBySlug != null) {
            return ValidationEvent.ErrorDuplicateSlug
        }
        
        return ValidationEvent.Success
    }

    private fun generateSlug(name: String?, providedSlug: String?): String {
        if (!providedSlug.isNullOrBlank()) {
            return providedSlug.lowercase()
                .replace(Regex("[^a-z0-9\\s-]"), "")
                .replace(Regex("\\s+"), "-")
                .replace(Regex("-+"), "-")
                .trim('-')
        }
        
        if (!name.isNullOrBlank()) {
            return name.lowercase()
                .replace(Regex("[^a-z0-9\\s-]"), "")
                .replace(Regex("\\s+"), "-")
                .replace(Regex("-+"), "-")
                .trim('-')
        }
        
        return ""
    }

    sealed class ValidationEvent {
        object ErrorFieldEmpty : ValidationEvent()
        object ErrorDuplicateName : ValidationEvent()
        object ErrorDuplicateSlug : ValidationEvent()
        object Success : ValidationEvent()
    }
}

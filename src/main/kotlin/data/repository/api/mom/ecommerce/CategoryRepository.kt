package com.evelolvetech.data.repository.api.mom.ecommerce

import com.evelolvetech.data.models.Category

interface CategoryRepository {
    suspend fun createCategory(category: Category): Boolean
    suspend fun getCategoryById(id: String): Category?
    suspend fun getAllCategories(): List<Category>
    suspend fun updateCategory(id: String, name: String?, slug: String?): Boolean
    suspend fun deleteCategory(id: String): Boolean
    suspend fun getCategoriesByIds(ids: List<String>): List<Category>
    suspend fun getCategoryByName(name: String): Category?
    suspend fun getCategoryBySlug(slug: String): Category?
}

package com.evelolvetech.mocks

import com.evelolvetech.data.models.Category
import com.evelolvetech.data.repository.api.mom.ecommerce.CategoryRepository

class MockCategoryRepository : CategoryRepository {
    private val categories = mutableMapOf<String, Category>()

    init {
        categories["cat_fitness"] = Category(
            id = "cat_fitness",
            name = "Fitness & Wellness",
            slug = "fitness-wellness"
        )
        categories["cat_nutrition"] = Category(
            id = "cat_nutrition",
            name = "Nutrition & Supplements",
            slug = "nutrition-supplements"
        )
        categories["cat_baby"] = Category(
            id = "cat_baby",
            name = "Baby Care",
            slug = "baby-care"
        )
        categories["cat_mom"] = Category(
            id = "cat_mom",
            name = "Mom Care",
            slug = "mom-care"
        )
        categories["cat_health"] = Category(
            id = "cat_health",
            name = "Health & Medical",
            slug = "health-medical"
        )
    }

    override suspend fun createCategory(category: Category): Boolean {
        categories[category.id] = category
        return true
    }

    override suspend fun getCategoryById(id: String): Category? {
        return categories[id]
    }

    override suspend fun getAllCategories(): List<Category> {
        return categories.values.toList()
    }

    override suspend fun updateCategory(id: String, name: String?, slug: String?): Boolean {
        val category = categories[id] ?: return false
        val updatedCategory = category.copy(
            name = name ?: category.name,
            slug = slug ?: category.slug
        )
        categories[id] = updatedCategory
        return true
    }

    override suspend fun deleteCategory(id: String): Boolean {
        return categories.remove(id) != null
    }

    override suspend fun getCategoriesByIds(ids: List<String>): List<Category> {
        return ids.mapNotNull { categories[it] }
    }

    override suspend fun getCategoryByName(name: String): Category? {
        return categories.values.find { it.name == name }
    }

    override suspend fun getCategoryBySlug(slug: String): Category? {
        return categories.values.find { it.slug == slug }
    }
}

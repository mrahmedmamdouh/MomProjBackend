package com.evelolvetech.data.repository.impl.mom.ecommerce

import com.evelolvetech.data.models.Category
import com.evelolvetech.data.repository.api.mom.ecommerce.CategoryRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.`in`

class CategoryRepositoryImpl(
    db: MongoDatabase
) : CategoryRepository {

    private val categories: MongoCollection<Category> = db.getCollection<Category>()

    override suspend fun createCategory(category: Category): Boolean {
        return try {
            categories.insertOne(category)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getCategoryById(id: String): Category? {
        return categories.findOne(Category::id eq id)
    }

    override suspend fun getAllCategories(): List<Category> {
        return categories.find().toList()
    }

    override suspend fun updateCategory(id: String, name: String?, slug: String?): Boolean {
        return try {
            val updates = mutableListOf<Bson>()
            name?.let { updates.add(Updates.set(Category::name.name, it)) }
            slug?.let { updates.add(Updates.set(Category::slug.name, it)) }

            if (updates.isNotEmpty()) {
                val result = categories.updateOne(
                    Category::id eq id,
                    Updates.combine(updates)
                )
                result.modifiedCount > 0
            } else false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteCategory(id: String): Boolean {
        return try {
            val result = categories.deleteOne(Category::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getCategoriesByIds(ids: List<String>): List<Category> {
        return categories.find(Category::id `in` ids).toList()
    }

    override suspend fun getCategoryByName(name: String): Category? {
        return categories.findOne(Category::name eq name)
    }

    override suspend fun getCategoryBySlug(slug: String): Category? {
        return categories.findOne(Category::slug eq slug)
    }
}

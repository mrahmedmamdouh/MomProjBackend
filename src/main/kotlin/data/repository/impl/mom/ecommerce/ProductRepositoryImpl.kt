package com.evelolvetech.data.repository.impl.mom.ecommerce

import com.evelolvetech.data.models.Product
import com.evelolvetech.data.repository.api.mom.ecommerce.ProductRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.*

class ProductRepositoryImpl(
    db: MongoDatabase
) : ProductRepository {

    private val products: MongoCollection<Product> = db.getCollection<Product>()

    override suspend fun createProduct(product: Product): Boolean {
        return try {
            products.insertOne(product)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getProductById(id: String): Product? {
        return products.findOne(Product::id eq id)
    }

    override suspend fun getAllProducts(page: Int, size: Int): List<Product> {
        return products.find()
            .skip(page * size)
            .limit(size)
            .toList()
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
        return try {
            val updates = mutableListOf<Bson>()
            name?.let { updates.add(Updates.set(Product::name.name, it)) }
            slug?.let { updates.add(Updates.set(Product::slug.name, it)) }
            description?.let { updates.add(Updates.set(Product::description.name, it)) }
            status?.let { updates.add(Updates.set(Product::status.name, it)) }
            defaultSellerId?.let { updates.add(Updates.set(Product::defaultSellerId.name, it)) }
            defaultSellerRef?.let { updates.add(Updates.set(Product::defaultSellerRef.name, it)) }
            categoryIds?.let { updates.add(Updates.set(Product::categoryIds.name, it)) }
            minSessionsToPurchase?.let { updates.add(Updates.set(Product::minSessionsToPurchase.name, it)) }

            if (updates.isNotEmpty()) {
                val result = products.updateOne(
                    Product::id eq id,
                    Updates.combine(updates)
                )
                result.modifiedCount > 0
            } else false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteProduct(id: String): Boolean {
        return try {
            val result = products.deleteOne(Product::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getProductsByCategory(categoryId: String, page: Int, size: Int): List<Product> {
        return products.find(Product::categoryIds `in` listOf(categoryId))
            .skip(page * size)
            .limit(size)
            .toList()
    }

    override suspend fun searchProducts(query: String, page: Int, size: Int): List<Product> {
        val regex = Filters.regex("name", query, "i")
        return products.find<Product>(regex)
            .skip(page * size)
            .limit(size)
            .toList()
    }

    override suspend fun getProductsByMinSessions(minSessions: Int, page: Int, size: Int): List<Product> {
        return products.find(Product::minSessionsToPurchase lte minSessions)
            .skip(page * size)
            .limit(size)
            .toList()
    }
}

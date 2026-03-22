package com.evelolvetech.data.repository.impl.mom.ecommerce

import com.evelolvetech.data.models.ProductRating
import com.evelolvetech.data.repository.api.mom.ecommerce.ProductRatingRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.*

class ProductRatingRepositoryImpl(
    db: MongoDatabase
) : ProductRatingRepository {

    private val ratings: MongoCollection<ProductRating> = db.getCollection<ProductRating>()

    override suspend fun createRating(rating: ProductRating): Boolean {
        return try {
            ratings.insertOne(rating)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getRatingById(id: String): ProductRating? {
        return ratings.findOne(ProductRating::id eq id)
    }

    override suspend fun getRatingsByProductId(productId: String, page: Int, size: Int): List<ProductRating> {
        return ratings.find(ProductRating::productId eq productId)
            .skip(page * size)
            .limit(size)
            .sort(descending(ProductRating::createdAt))
            .toList()
    }

    override suspend fun getRatingsByUid(uid: String): List<ProductRating> {
        return ratings.find(ProductRating::uid eq uid).toList()
    }

    override suspend fun updateRating(id: String, rating: Int, title: String?, comment: String?): Boolean {
        return try {
            val updates = mutableListOf(setValue(ProductRating::rating, rating))
            title?.let { updates.add(setValue(ProductRating::title, it)) }
            comment?.let { updates.add(setValue(ProductRating::comment, it)) }

            val result = ratings.updateOne(
                ProductRating::id eq id,
                combine(updates)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteRating(id: String): Boolean {
        return try {
            val result = ratings.deleteOne(ProductRating::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getAverageRating(productId: String): Double {
        return try {
            val productRatings = ratings.find(ProductRating::productId eq productId).toList()
            if (productRatings.isEmpty()) 0.0
            else productRatings.map { it.rating }.average()
        } catch (e: Exception) {
            0.0
        }
    }

    override suspend fun getRatingCount(productId: String): Int {
        return try {
            ratings.countDocuments(ProductRating::productId eq productId).toInt()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getUserRatingForProduct(productId: String, uid: String): ProductRating? {
        return ratings.findOne(
            and(
                ProductRating::productId eq productId,
                ProductRating::uid eq uid
            )
        )
    }
}

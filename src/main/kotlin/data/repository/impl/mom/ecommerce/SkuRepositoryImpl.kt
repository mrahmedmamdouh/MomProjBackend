package com.evelolvetech.data.repository.impl.mom.ecommerce

import com.evelolvetech.data.models.Sku
import com.evelolvetech.data.repository.api.mom.ecommerce.SkuRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class SkuRepositoryImpl(
    db: MongoDatabase
) : SkuRepository {

    private val skus: MongoCollection<Sku> = db.getCollection<Sku>()

    override suspend fun createSku(sku: Sku): Boolean {
        return try {
            skus.insertOne(sku)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getSkuById(id: String): Sku? {
        return skus.findOne(Sku::id eq id)
    }

    override suspend fun getSkusByProductId(productId: String): List<Sku> {
        return skus.find(Sku::productId eq productId).toList()
    }

    override suspend fun updateSku(
        id: String,
        skuCode: String?,
        title: String?,
        taxClass: String?,
        isActive: Boolean?
    ): Boolean {
        return try {
            val updates = mutableListOf<Bson>()
            skuCode?.let { updates.add(Updates.set(Sku::skuCode.name, it)) }
            title?.let { updates.add(Updates.set(Sku::title.name, it)) }
            taxClass?.let { updates.add(Updates.set(Sku::taxClass.name, it)) }
            isActive?.let { updates.add(Updates.set(Sku::isActive.name, it)) }

            if (updates.isNotEmpty()) {
                val result = skus.updateOne(
                    Sku::id eq id,
                    Updates.combine(updates)
                )
                result.modifiedCount > 0
            } else false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteSku(id: String): Boolean {
        return try {
            val result = skus.deleteOne(Sku::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getActiveSkus(): List<Sku> {
        return skus.find(Sku::isActive eq true).toList()
    }
}

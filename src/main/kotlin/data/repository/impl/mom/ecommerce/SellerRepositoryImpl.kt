package com.evelolvetech.data.repository.impl.mom.ecommerce

import com.evelolvetech.data.models.Seller
import com.evelolvetech.data.repository.api.mom.ecommerce.SellerRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class SellerRepositoryImpl(
    db: MongoDatabase
) : SellerRepository {

    private val sellers: MongoCollection<Seller> = db.getCollection<Seller>()

    override suspend fun createSeller(seller: Seller): Boolean {
        return try {
            sellers.insertOne(seller)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getSellerById(id: String): Seller? {
        return sellers.findOne(Seller::id eq id)
    }

    override suspend fun getAllSellers(): List<Seller> {
        return sellers.find().toList()
    }

    override suspend fun updateSeller(id: String, name: String?, status: String?): Boolean {
        return try {
            val updates = mutableListOf<Bson>()
            name?.let { updates.add(Updates.set(Seller::name.name, it)) }
            status?.let { updates.add(Updates.set(Seller::status.name, it)) }

            if (updates.isNotEmpty()) {
                val result = sellers.updateOne(
                    Seller::id eq id,
                    Updates.combine(updates)
                )
                result.modifiedCount > 0
            } else false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteSeller(id: String): Boolean {
        return try {
            val result = sellers.deleteOne(Seller::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getActiveSellers(): List<Seller> {
        return sellers.find(Seller::status eq "ACTIVE").toList()
    }
}

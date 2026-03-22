package com.evelolvetech.data.repository.impl.mom.ecommerce

import com.evelolvetech.data.models.SkuOffer
import com.evelolvetech.data.repository.api.mom.ecommerce.SkuOfferRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.*

class SkuOfferRepositoryImpl(
    db: MongoDatabase
) : SkuOfferRepository {

    private val skuOffers: MongoCollection<SkuOffer> = db.getCollection<SkuOffer>()

    override suspend fun createSkuOffer(skuOffer: SkuOffer): Boolean {
        return try {
            skuOffers.insertOne(skuOffer)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getSkuOfferById(id: String): SkuOffer? {
        return skuOffers.findOne(SkuOffer::id eq id)
    }

    override suspend fun getSkuOffersBySkuId(skuId: String): List<SkuOffer> {
        return skuOffers.find(SkuOffer::skuId eq skuId).toList()
    }

    override suspend fun getSkuOffersBySellerId(sellerId: String): List<SkuOffer> {
        return skuOffers.find(SkuOffer::sellerId eq sellerId).toList()
    }

    override suspend fun updateSkuOffer(
        id: String,
        listPrice: Double?,
        salePrice: Double?,
        currency: String?,
        isActive: Boolean?,
        activeFrom: Long?,
        activeTo: Long?
    ): Boolean {
        return try {
            val updates = mutableListOf<Bson>()
            listPrice?.let { updates.add(Updates.set(SkuOffer::listPrice.name, it)) }
            salePrice?.let { updates.add(Updates.set(SkuOffer::salePrice.name, it)) }
            currency?.let { updates.add(Updates.set(SkuOffer::currency.name, it)) }
            isActive?.let { updates.add(Updates.set(SkuOffer::isActive.name, it)) }
            activeFrom?.let { updates.add(Updates.set(SkuOffer::activeFrom.name, it)) }
            activeTo?.let { updates.add(Updates.set(SkuOffer::activeTo.name, it)) }

            if (updates.isNotEmpty()) {
                val result = skuOffers.updateOne(
                    SkuOffer::id eq id,
                    Updates.combine(updates)
                )
                result.modifiedCount > 0
            } else false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteSkuOffer(id: String): Boolean {
        return try {
            val result = skuOffers.deleteOne(SkuOffer::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getActiveSkuOffers(): List<SkuOffer> {
        return skuOffers.find(SkuOffer::isActive eq true).toList()
    }

    override suspend fun getBestOfferForSku(skuId: String): SkuOffer? {
        return skuOffers.find(
            and(
                SkuOffer::skuId eq skuId,
                SkuOffer::isActive eq true
            )
        ).sort(ascending(SkuOffer::salePrice)).first()
    }
}

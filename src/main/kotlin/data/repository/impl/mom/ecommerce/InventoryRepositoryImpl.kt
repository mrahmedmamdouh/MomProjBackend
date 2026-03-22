package com.evelolvetech.data.repository.impl.mom.ecommerce

import com.evelolvetech.data.models.Inventory
import com.evelolvetech.data.repository.api.mom.ecommerce.InventoryRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.*

class InventoryRepositoryImpl(
    db: MongoDatabase
) : InventoryRepository {

    private val inventory: MongoCollection<Inventory> = db.getCollection<Inventory>()

    override suspend fun createInventory(inventory: Inventory): Boolean {
        return try {
            this.inventory.insertOne(inventory)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getInventoryBySkuId(skuId: String): Inventory? {
        return inventory.findOne(Inventory::skuId eq skuId)
    }

    override suspend fun updateInventory(skuId: String, onHand: Int?, reserved: Int?): Boolean {
        return try {
            val updates = mutableListOf<org.bson.conversions.Bson>()
            onHand?.let { updates.add(setValue(Inventory::onHand, it)) }
            reserved?.let { updates.add(setValue(Inventory::reserved, it)) }
            updates.add(setValue(Inventory::updatedAt, System.currentTimeMillis()))

            val result = inventory.updateOne(
                Inventory::skuId eq skuId,
                combine(updates)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun reserveInventory(skuId: String, quantity: Int): Boolean {
        return try {
            val currentInventory = getInventoryBySkuId(skuId) ?: return false
            if (currentInventory.onHand >= quantity) {
                val result = inventory.updateOne(
                    Inventory::skuId eq skuId,
                    combine(
                        inc(Inventory::onHand, -quantity),
                        inc(Inventory::reserved, quantity),
                        setValue(Inventory::updatedAt, System.currentTimeMillis())
                    )
                )
                result.modifiedCount > 0
            } else false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun releaseInventory(skuId: String, quantity: Int): Boolean {
        return try {
            val result = inventory.updateOne(
                Inventory::skuId eq skuId,
                combine(
                    inc(Inventory::onHand, quantity),
                    inc(Inventory::reserved, -quantity),
                    setValue(Inventory::updatedAt, System.currentTimeMillis())
                )
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getLowStockItems(threshold: Int): List<Inventory> {
        return inventory.find(Inventory::onHand lt threshold).toList()
    }
}

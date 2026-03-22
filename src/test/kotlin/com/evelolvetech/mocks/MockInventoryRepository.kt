package com.evelolvetech.mocks

import com.evelolvetech.data.models.Inventory
import com.evelolvetech.data.repository.api.mom.ecommerce.InventoryRepository

class MockInventoryRepository : InventoryRepository {
    val inventory = mutableMapOf<String, Inventory>()

    override suspend fun createInventory(inv: Inventory): Boolean {
        inventory[inv.skuId] = inv
        return true
    }

    override suspend fun getInventoryBySkuId(skuId: String): Inventory? {
        return inventory[skuId]
    }

    override suspend fun updateInventory(skuId: String, onHand: Int?, reserved: Int?): Boolean {
        val existing = inventory[skuId] ?: return false
        inventory[skuId] = existing.copy(
            onHand = onHand ?: existing.onHand,
            reserved = reserved ?: existing.reserved,
            updatedAt = System.currentTimeMillis()
        )
        return true
    }

    override suspend fun reserveInventory(skuId: String, quantity: Int): Boolean {
        val existing = inventory[skuId] ?: return false
        if (existing.onHand < quantity) return false
        inventory[skuId] = existing.copy(
            onHand = existing.onHand - quantity,
            reserved = existing.reserved + quantity,
            updatedAt = System.currentTimeMillis()
        )
        return true
    }

    override suspend fun releaseInventory(skuId: String, quantity: Int): Boolean {
        val existing = inventory[skuId] ?: return false
        if (existing.reserved < quantity) return false
        inventory[skuId] = existing.copy(
            onHand = existing.onHand + quantity,
            reserved = existing.reserved - quantity,
            updatedAt = System.currentTimeMillis()
        )
        return true
    }

    override suspend fun getLowStockItems(threshold: Int): List<Inventory> {
        return inventory.values.filter { it.onHand < threshold }
    }
}

package com.evelolvetech.data.repository.api.mom.ecommerce

import com.evelolvetech.data.models.Inventory

interface InventoryRepository {
    suspend fun createInventory(inventory: Inventory): Boolean
    suspend fun getInventoryBySkuId(skuId: String): Inventory?
    suspend fun updateInventory(skuId: String, onHand: Int?, reserved: Int?): Boolean
    suspend fun reserveInventory(skuId: String, quantity: Int): Boolean
    suspend fun releaseInventory(skuId: String, quantity: Int): Boolean
    suspend fun getLowStockItems(threshold: Int = 10): List<Inventory>
}

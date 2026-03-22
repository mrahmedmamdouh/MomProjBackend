package com.evelolvetech.data.responses

import com.evelolvetech.data.models.Inventory
import kotlinx.serialization.Serializable

@Serializable
data class InventoryResponse(
    val id: String,
    val skuId: String,
    val onHand: Int,
    val reserved: Int,
    val available: Int,
    val updatedAt: Long
) {
    companion object {
        fun fromInventory(inventory: Inventory) = InventoryResponse(
            id = inventory.id,
            skuId = inventory.skuId,
            onHand = inventory.onHand,
            reserved = inventory.reserved,
            available = inventory.onHand - inventory.reserved,
            updatedAt = inventory.updatedAt
        )
    }
}

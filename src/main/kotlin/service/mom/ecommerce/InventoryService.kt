package com.evelolvetech.service.mom.ecommerce

import com.evelolvetech.data.models.Inventory
import com.evelolvetech.data.repository.api.mom.ecommerce.InventoryRepository
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.data.responses.InventoryResponse
import com.evelolvetech.util.Constants

class InventoryService(
    private val inventoryRepository: InventoryRepository
) {

    suspend fun getInventoryBySkuId(skuId: String): BasicApiResponse<InventoryResponse> {
        return try {
            val inventory = inventoryRepository.getInventoryBySkuId(skuId)
                ?: return BasicApiResponse(success = false, message = "Inventory record not found for SKU: $skuId")

            BasicApiResponse(success = true, data = InventoryResponse.fromInventory(inventory))
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving inventory: ${e.message}")
        }
    }

    suspend fun createInventory(skuId: String, onHand: Int): BasicApiResponse<InventoryResponse> {
        return try {
            val existingInventory = inventoryRepository.getInventoryBySkuId(skuId)
            if (existingInventory != null) {
                return BasicApiResponse(success = false, message = "Inventory record already exists for SKU: $skuId")
            }

            if (onHand < 0) {
                return BasicApiResponse(success = false, message = "On-hand quantity cannot be negative")
            }

            val inventory = Inventory(skuId = skuId, onHand = onHand, reserved = 0)

            val created = inventoryRepository.createInventory(inventory)
            if (!created) {
                return BasicApiResponse(success = false, message = "Failed to create inventory record")
            }

            BasicApiResponse(
                success = true,
                data = InventoryResponse.fromInventory(inventory),
                message = "Inventory record created successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error creating inventory: ${e.message}")
        }
    }

    suspend fun updateStock(skuId: String, onHand: Int?, reserved: Int?): BasicApiResponse<InventoryResponse> {
        return try {
            inventoryRepository.getInventoryBySkuId(skuId)
                ?: return BasicApiResponse(success = false, message = "Inventory record not found for SKU: $skuId")

            onHand?.let {
                if (it < 0) return BasicApiResponse(success = false, message = "On-hand quantity cannot be negative")
            }
            reserved?.let {
                if (it < 0) return BasicApiResponse(success = false, message = "Reserved quantity cannot be negative")
            }

            val updated = inventoryRepository.updateInventory(skuId, onHand, reserved)
            if (!updated) {
                return BasicApiResponse(success = false, message = "Failed to update inventory")
            }

            val updatedInventory = inventoryRepository.getInventoryBySkuId(skuId)
            BasicApiResponse(
                success = true,
                data = updatedInventory?.let { InventoryResponse.fromInventory(it) },
                message = "Inventory updated successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error updating inventory: ${e.message}")
        }
    }

    suspend fun reserveStock(skuId: String, quantity: Int): BasicApiResponse<InventoryResponse> {
        return try {
            if (quantity <= 0) {
                return BasicApiResponse(success = false, message = "Quantity must be greater than zero")
            }

            val inventory = inventoryRepository.getInventoryBySkuId(skuId)
                ?: return BasicApiResponse(success = false, message = "Inventory record not found for SKU: $skuId")

            if (inventory.onHand < quantity) {
                return BasicApiResponse(success = false, message = Constants.INSUFFICIENT_INVENTORY)
            }

            val reserved = inventoryRepository.reserveInventory(skuId, quantity)
            if (!reserved) {
                return BasicApiResponse(success = false, message = "Failed to reserve inventory")
            }

            val updatedInventory = inventoryRepository.getInventoryBySkuId(skuId)
            BasicApiResponse(
                success = true,
                data = updatedInventory?.let { InventoryResponse.fromInventory(it) },
                message = "Stock reserved successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error reserving inventory: ${e.message}")
        }
    }

    suspend fun releaseStock(skuId: String, quantity: Int): BasicApiResponse<InventoryResponse> {
        return try {
            if (quantity <= 0) {
                return BasicApiResponse(success = false, message = "Quantity must be greater than zero")
            }

            val inventory = inventoryRepository.getInventoryBySkuId(skuId)
                ?: return BasicApiResponse(success = false, message = "Inventory record not found for SKU: $skuId")

            if (inventory.reserved < quantity) {
                return BasicApiResponse(success = false, message = "Cannot release more than reserved quantity")
            }

            val released = inventoryRepository.releaseInventory(skuId, quantity)
            if (!released) {
                return BasicApiResponse(success = false, message = "Failed to release inventory")
            }

            val updatedInventory = inventoryRepository.getInventoryBySkuId(skuId)
            BasicApiResponse(
                success = true,
                data = updatedInventory?.let { InventoryResponse.fromInventory(it) },
                message = "Stock released successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error releasing inventory: ${e.message}")
        }
    }

    suspend fun getLowStockItems(threshold: Int): BasicApiResponse<List<InventoryResponse>> {
        return try {
            if (threshold < 0) {
                return BasicApiResponse(success = false, message = "Threshold must be non-negative")
            }

            val items = inventoryRepository.getLowStockItems(threshold)
            BasicApiResponse(
                success = true,
                data = items.map { InventoryResponse.fromInventory(it) },
                message = "Low stock items retrieved successfully (threshold: $threshold)"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving low stock items: ${e.message}")
        }
    }
}

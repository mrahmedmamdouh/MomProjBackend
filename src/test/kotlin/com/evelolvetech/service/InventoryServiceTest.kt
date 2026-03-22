package com.evelolvetech.service

import com.evelolvetech.data.models.Inventory
import com.evelolvetech.mocks.MockInventoryRepository
import com.evelolvetech.service.mom.ecommerce.InventoryService
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class InventoryServiceTest {

    private val mockInventoryRepository = MockInventoryRepository()
    private val inventoryService = InventoryService(mockInventoryRepository)

    @Test
    fun testCreateInventorySuccess() = runBlocking {
        mockInventoryRepository.inventory.clear()

        val result = inventoryService.createInventory("sku_new_item", 100)

        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals("sku_new_item", result.data!!.skuId)
        assertEquals(100, result.data!!.onHand)
        assertEquals(0, result.data!!.reserved)
        assertEquals(100, result.data!!.available)
    }

    @Test
    fun testCreateInventoryDuplicate() = runBlocking {
        mockInventoryRepository.inventory.clear()
        mockInventoryRepository.inventory["sku_existing"] = Inventory(
            skuId = "sku_existing", onHand = 50, reserved = 5
        )

        val result = inventoryService.createInventory("sku_existing", 200)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("already exists"))
    }

    @Test
    fun testCreateInventoryNegativeQuantity() = runBlocking {
        val result = inventoryService.createInventory("sku_negative", -5)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("cannot be negative"))
    }

    @Test
    fun testGetInventoryBySkuIdSuccess() = runBlocking {
        mockInventoryRepository.inventory["sku_test"] = Inventory(
            skuId = "sku_test", onHand = 75, reserved = 10
        )

        val result = inventoryService.getInventoryBySkuId("sku_test")

        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals(75, result.data!!.onHand)
        assertEquals(10, result.data!!.reserved)
        assertEquals(65, result.data!!.available)
    }

    @Test
    fun testGetInventoryBySkuIdNotFound() = runBlocking {
        val result = inventoryService.getInventoryBySkuId("sku_nonexistent")

        assertFalse(result.success)
        assertTrue(result.message!!.contains("not found"))
    }

    @Test
    fun testUpdateStockSuccess() = runBlocking {
        mockInventoryRepository.inventory["sku_update"] = Inventory(
            skuId = "sku_update", onHand = 50, reserved = 5
        )

        val result = inventoryService.updateStock("sku_update", onHand = 200, reserved = null)

        assertTrue(result.success)
        assertEquals(200, mockInventoryRepository.inventory["sku_update"]!!.onHand)
        assertEquals(5, mockInventoryRepository.inventory["sku_update"]!!.reserved)
    }

    @Test
    fun testUpdateStockNegativeOnHand() = runBlocking {
        mockInventoryRepository.inventory["sku_neg"] = Inventory(
            skuId = "sku_neg", onHand = 50, reserved = 5
        )

        val result = inventoryService.updateStock("sku_neg", onHand = -10, reserved = null)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("cannot be negative"))
    }

    @Test
    fun testReserveStockSuccess() = runBlocking {
        mockInventoryRepository.inventory["sku_reserve"] = Inventory(
            skuId = "sku_reserve", onHand = 100, reserved = 0
        )

        val result = inventoryService.reserveStock("sku_reserve", 25)

        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals(75, result.data!!.onHand)
        assertEquals(25, result.data!!.reserved)
        assertEquals(50, result.data!!.available)
    }

    @Test
    fun testReserveStockInsufficientInventory() = runBlocking {
        mockInventoryRepository.inventory["sku_low"] = Inventory(
            skuId = "sku_low", onHand = 5, reserved = 0
        )

        val result = inventoryService.reserveStock("sku_low", 10)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("Insufficient"))
    }

    @Test
    fun testReserveStockZeroQuantity() = runBlocking {
        mockInventoryRepository.inventory["sku_zero"] = Inventory(
            skuId = "sku_zero", onHand = 100, reserved = 0
        )

        val result = inventoryService.reserveStock("sku_zero", 0)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("greater than zero"))
    }

    @Test
    fun testReserveStockNotFound() = runBlocking {
        val result = inventoryService.reserveStock("sku_ghost", 5)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("not found"))
    }

    @Test
    fun testReleaseStockSuccess() = runBlocking {
        mockInventoryRepository.inventory["sku_release"] = Inventory(
            skuId = "sku_release", onHand = 75, reserved = 25
        )

        val result = inventoryService.releaseStock("sku_release", 10)

        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals(85, result.data!!.onHand)
        assertEquals(15, result.data!!.reserved)
        assertEquals(70, result.data!!.available)
    }

    @Test
    fun testReleaseStockExceedsReserved() = runBlocking {
        mockInventoryRepository.inventory["sku_over_release"] = Inventory(
            skuId = "sku_over_release", onHand = 75, reserved = 5
        )

        val result = inventoryService.releaseStock("sku_over_release", 10)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("Cannot release more"))
    }

    @Test
    fun testReleaseStockZeroQuantity() = runBlocking {
        mockInventoryRepository.inventory["sku_rel_zero"] = Inventory(
            skuId = "sku_rel_zero", onHand = 50, reserved = 10
        )

        val result = inventoryService.releaseStock("sku_rel_zero", 0)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("greater than zero"))
    }

    @Test
    fun testGetLowStockItems() = runBlocking {
        mockInventoryRepository.inventory.clear()
        mockInventoryRepository.inventory["sku_plenty"] = Inventory(
            skuId = "sku_plenty", onHand = 500, reserved = 0
        )
        mockInventoryRepository.inventory["sku_low_1"] = Inventory(
            skuId = "sku_low_1", onHand = 3, reserved = 0
        )
        mockInventoryRepository.inventory["sku_low_2"] = Inventory(
            skuId = "sku_low_2", onHand = 7, reserved = 2
        )
        mockInventoryRepository.inventory["sku_borderline"] = Inventory(
            skuId = "sku_borderline", onHand = 10, reserved = 0
        )

        val result = inventoryService.getLowStockItems(10)

        assertTrue(result.success)
        assertEquals(2, result.data!!.size)
        assertTrue(result.data!!.all { it.onHand < 10 })
    }

    @Test
    fun testGetLowStockItemsNegativeThreshold() = runBlocking {
        val result = inventoryService.getLowStockItems(-1)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("non-negative"))
    }

    @Test
    fun testReserveAndReleaseFullCycle() = runBlocking {
        mockInventoryRepository.inventory["sku_cycle"] = Inventory(
            skuId = "sku_cycle", onHand = 100, reserved = 0
        )

        val reserveResult = inventoryService.reserveStock("sku_cycle", 30)
        assertTrue(reserveResult.success)
        assertEquals(70, mockInventoryRepository.inventory["sku_cycle"]!!.onHand)
        assertEquals(30, mockInventoryRepository.inventory["sku_cycle"]!!.reserved)

        val releaseResult = inventoryService.releaseStock("sku_cycle", 30)
        assertTrue(releaseResult.success)
        assertEquals(100, mockInventoryRepository.inventory["sku_cycle"]!!.onHand)
        assertEquals(0, mockInventoryRepository.inventory["sku_cycle"]!!.reserved)
    }
}

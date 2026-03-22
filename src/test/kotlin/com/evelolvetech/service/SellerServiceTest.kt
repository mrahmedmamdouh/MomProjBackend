package com.evelolvetech.service

import com.evelolvetech.data.requests.CreateSellerRequest
import com.evelolvetech.data.requests.UpdateSellerRequest
import com.evelolvetech.mocks.MockSellerRepository
import kotlinx.coroutines.runBlocking
import com.evelolvetech.service.mom.ecommerce.SellerService
import kotlin.test.*

class SellerServiceTest {

    private val mockSellerRepository = MockSellerRepository()
    private val sellerService = SellerService(mockSellerRepository)

    @Test
    fun testCreateSellerSuccess() = runBlocking {
        val request = CreateSellerRequest(
            name = "Test Seller"
        )

        val result = sellerService.createSeller(request)

        assertTrue(result)
    }

    @Test
    fun testGetSellerByIdSuccess() = runBlocking {
        val sellerId = "seller_happy"

        val result = sellerService.getSellerById(sellerId)

        assertNotNull(result)
        assertEquals("Happy Health Store", result?.name)
    }

    @Test
    fun testGetSellerByIdNotFound() = runBlocking {
        val sellerId = "seller_nonexistent"

        val result = sellerService.getSellerById(sellerId)

        assertNull(result)
    }

    @Test
    fun testGetAllSellers() = runBlocking {
        val result = sellerService.getAllSellers()

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertEquals(2, result.size)
    }

    @Test
    fun testUpdateSellerSuccess() = runBlocking {
        val sellerId = "seller_happy"
        val request = UpdateSellerRequest(
            name = "Updated Happy Health Store",
            status = "ACTIVE"
        )

        val result = sellerService.updateSeller(sellerId, request)

        assertTrue(result)
    }

    @Test
    fun testUpdateSellerNotFound() = runBlocking {
        val sellerId = "seller_nonexistent"
        val request = UpdateSellerRequest(
            name = "Updated Seller",
            status = "ACTIVE"
        )

        val result = sellerService.updateSeller(sellerId, request)

        assertFalse(result)
    }

    @Test
    fun testDeleteSellerSuccess() = runBlocking {
        val sellerId = "seller_happy"

        val result = sellerService.deleteSeller(sellerId)

        assertTrue(result)
    }

    @Test
    fun testDeleteSellerNotFound() = runBlocking {
        val sellerId = "seller_nonexistent"

        val result = sellerService.deleteSeller(sellerId)

        assertFalse(result)
    }
}

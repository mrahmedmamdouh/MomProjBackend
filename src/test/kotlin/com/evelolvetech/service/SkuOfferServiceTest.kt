package com.evelolvetech.service

import com.evelolvetech.data.requests.CreateSkuOfferRequest
import com.evelolvetech.data.requests.UpdateSkuOfferRequest
import com.evelolvetech.mocks.*
import kotlinx.coroutines.runBlocking
import com.evelolvetech.service.mom.ecommerce.SkuOfferService
import kotlin.test.*

class SkuOfferServiceTest {

    private val mockSkuOfferRepository = MockSkuOfferRepository()
    private val mockMomRepository = MockMomRepository()

    private val skuOfferService = SkuOfferService(
        mockSkuOfferRepository,
        mockMomRepository,
        MockAuthConfig.instance
    )

    @Test
    fun testCreateSkuOfferSuccess() = runBlocking {
        val request = CreateSkuOfferRequest(
            skuId = "sku_prenatal_batchA",
            sellerId = "seller_happy",
            listPrice = 24.99,
            salePrice = 19.99,
            currency = "USD"
        )

        val result = skuOfferService.createSkuOffer(request)

        assertTrue(result)
    }

    @Test
    fun testCreateSkuOfferInvalidRequest() = runBlocking {
        val request = CreateSkuOfferRequest(
            skuId = "",
            sellerId = "seller_happy",
            listPrice = 24.99,
            salePrice = 19.99,
            currency = "USD"
        )

        val result = skuOfferService.createSkuOffer(request)

        assertFalse(result)
    }

    @Test
    fun testGetSkuOfferByIdSuccess() = runBlocking {
        val offerId = "offer_prn_happy"

        val result = skuOfferService.getSkuOfferById(offerId)

        assertNotNull(result)
        assertEquals("sku_prenatal_batchA", result?.skuId)
        assertEquals("seller_happy", result?.sellerId)
        assertEquals(19.99, result?.salePrice)
    }

    @Test
    fun testGetSkuOfferByIdNotFound() = runBlocking {
        val offerId = "offer_nonexistent"

        val result = skuOfferService.getSkuOfferById(offerId)

        assertNull(result)
    }

    @Test
    fun testGetSkuOffersBySkuId() = runBlocking {
        val skuId = "sku_prenatal_batchA"

        val result = skuOfferService.getSkuOffersBySkuId(skuId)

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertEquals(1, result.size)
    }

    @Test
    fun testGetSkuOffersBySellerId() = runBlocking {
        val sellerId = "seller_happy"

        val result = skuOfferService.getSkuOffersBySellerId(sellerId)

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertEquals(1, result.size)
    }

    @Test
    fun testUpdateSkuOfferSuccess() = runBlocking {
        val offerId = "offer_prn_happy"
        val request = UpdateSkuOfferRequest(
            listPrice = 29.99,
            salePrice = 24.99,
            currency = "USD",
            isActive = true,
            activeFrom = System.currentTimeMillis(),
            activeTo = System.currentTimeMillis() + 86400000
        )

        val result = skuOfferService.updateSkuOffer(offerId, request)

        assertTrue(result)
    }

    @Test
    fun testUpdateSkuOfferNotFound() = runBlocking {
        val offerId = "offer_nonexistent"
        val request = UpdateSkuOfferRequest(
            listPrice = 29.99,
            salePrice = 24.99,
            currency = "USD",
            isActive = true,
            activeFrom = System.currentTimeMillis(),
            activeTo = System.currentTimeMillis() + 86400000
        )

        val result = skuOfferService.updateSkuOffer(offerId, request)

        assertFalse(result)
    }

    @Test
    fun testDeleteSkuOfferSuccess() = runBlocking {
        val offerId = "offer_prn_happy"

        val result = skuOfferService.deleteSkuOffer(offerId)

        assertTrue(result)
    }

    @Test
    fun testDeleteSkuOfferNotFound() = runBlocking {
        val offerId = "offer_nonexistent"

        val result = skuOfferService.deleteSkuOffer(offerId)

        assertFalse(result)
    }

    @Test
    fun testGetActiveSkuOffers() = runBlocking {
        val result = skuOfferService.getActiveSkuOffers()

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertEquals(2, result.size)
    }

    @Test
    fun testGetBestOfferForSku() = runBlocking {
        val skuId = "sku_prenatal_batchA"

        val result = skuOfferService.getBestOfferForSku(skuId)

        assertNotNull(result)
        assertEquals("sku_prenatal_batchA", result?.skuId)
        assertEquals(19.99, result?.salePrice)
    }

    @Test
    fun testGetBestOfferForSkuNotFound() = runBlocking {
        val skuId = "sku_nonexistent"

        val result = skuOfferService.getBestOfferForSku(skuId)

        assertNull(result)
    }

    @Test
    fun testValidateCreateSkuOfferRequest() = runBlocking {
        val validRequest = CreateSkuOfferRequest(
            skuId = "sku_prenatal_batchA",
            sellerId = "seller_happy",
            listPrice = 24.99,
            salePrice = 19.99,
            currency = "USD"
        )

        val result = skuOfferService.validateCreateSkuOfferRequest(validRequest)

        assertTrue(result is SkuOfferService.ValidationEvent.Success)
    }

    @Test
    fun testValidateCreateSkuOfferRequestEmptyFields() = runBlocking {
        val invalidRequest = CreateSkuOfferRequest(
            skuId = "",
            sellerId = "seller_happy",
            listPrice = 24.99,
            salePrice = 19.99,
            currency = "USD"
        )

        val result = skuOfferService.validateCreateSkuOfferRequest(invalidRequest)

        assertTrue(result is SkuOfferService.ValidationEvent.ErrorFieldEmpty)
    }

    @Test
    fun testValidateCreateSkuOfferRequestInvalidPrice() = runBlocking {
        val invalidRequest = CreateSkuOfferRequest(
            skuId = "sku_prenatal_batchA",
            sellerId = "seller_happy",
            listPrice = 0.0,
            salePrice = 19.99,
            currency = "USD"
        )

        val result = skuOfferService.validateCreateSkuOfferRequest(invalidRequest)

        assertTrue(result is SkuOfferService.ValidationEvent.ErrorInvalidPrice)
    }
}
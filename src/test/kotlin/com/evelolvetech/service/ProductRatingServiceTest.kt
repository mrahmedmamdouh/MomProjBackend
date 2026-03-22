package com.evelolvetech.service

import com.evelolvetech.data.models.ProductRating
import com.evelolvetech.mocks.MockProductRatingRepository
import com.evelolvetech.mocks.MockProductRepository
import com.evelolvetech.service.mom.ecommerce.ProductRatingService
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class ProductRatingServiceTest {

    private val mockRatingRepository = MockProductRatingRepository()
    private val mockProductRepository = MockProductRepository()
    private val ratingService = ProductRatingService(mockRatingRepository, mockProductRepository)

    @Test
    fun testCreateRatingSuccess() = runBlocking {
        val result = ratingService.createRating(
            momId = "mom_beth",
            productId = "prod_prenatal",
            rating = 5,
            title = "Amazing product!",
            comment = "This kit had everything I needed for my newborn."
        )

        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals(5, result.data!!.rating)
        assertEquals("prod_prenatal", result.data!!.productId)
        assertEquals("mom_beth", result.data!!.uid)
    }

    @Test
    fun testCreateRatingProductNotFound() = runBlocking {
        val result = ratingService.createRating(
            momId = "mom_beth",
            productId = "nonexistent",
            rating = 4,
            title = "Good",
            comment = "Test"
        )

        assertFalse(result.success)
        assertTrue(result.message!!.contains("not found", ignoreCase = true))
    }

    @Test
    fun testCreateRatingInvalidRatingTooHigh() = runBlocking {
        val result = ratingService.createRating(
            momId = "mom_beth",
            productId = "prod_prenatal",
            rating = 6,
            title = "Invalid",
            comment = "Test"
        )

        assertFalse(result.success)
        assertTrue(result.message!!.contains("between 1 and 5"))
    }

    @Test
    fun testCreateRatingInvalidRatingTooLow() = runBlocking {
        val result = ratingService.createRating(
            momId = "mom_beth",
            productId = "prod_fitness",
            rating = 0,
            title = "Invalid",
            comment = "Test"
        )

        assertFalse(result.success)
        assertTrue(result.message!!.contains("between 1 and 5"))
    }

    @Test
    fun testCreateDuplicateRatingPrevented() = runBlocking {
        mockRatingRepository.ratings.clear()

        val first = ratingService.createRating(
            momId = "mom_beth",
            productId = "prod_baby_essentials",
            rating = 5,
            title = "Love it",
            comment = "Great"
        )
        assertTrue(first.success)

        val duplicate = ratingService.createRating(
            momId = "mom_beth",
            productId = "prod_baby_essentials",
            rating = 3,
            title = "Changed my mind",
            comment = "It was ok"
        )
        assertFalse(duplicate.success)
        assertTrue(duplicate.message!!.contains("already rated"))
    }

    @Test
    fun testUpdateRatingSuccess() = runBlocking {
        mockRatingRepository.ratings.clear()
        val rating = ProductRating(
            id = "rating_001",
            productId = "prod_baby_essentials",
            uid = "mom_beth",
            rating = 3,
            title = "OK product",
            comment = "Average"
        )
        mockRatingRepository.ratings["rating_001"] = rating

        val result = ratingService.updateRating(
            ratingId = "rating_001",
            momId = "mom_beth",
            rating = 5,
            title = "Actually amazing!",
            comment = "Grew on me over time"
        )

        assertTrue(result.success)
        assertEquals(5, mockRatingRepository.ratings["rating_001"]!!.rating)
    }

    @Test
    fun testUpdateRatingWrongUser() = runBlocking {
        val rating = ProductRating(
            id = "rating_owned",
            productId = "prod_baby_essentials",
            uid = "mom_beth",
            rating = 4,
            title = "Good",
            comment = "Nice"
        )
        mockRatingRepository.ratings["rating_owned"] = rating

        val result = ratingService.updateRating(
            ratingId = "rating_owned",
            momId = "mom_alice",
            rating = 1,
            title = "Hacked",
            comment = "Not my rating"
        )

        assertFalse(result.success)
        assertTrue(result.message!!.contains("Access denied"))
    }

    @Test
    fun testDeleteRatingSuccess() = runBlocking {
        val rating = ProductRating(
            id = "rating_delete",
            productId = "prod_baby_essentials",
            uid = "mom_beth",
            rating = 2,
            title = "Not great",
            comment = "Disappointed"
        )
        mockRatingRepository.ratings["rating_delete"] = rating

        val result = ratingService.deleteRating("rating_delete", "mom_beth")

        assertTrue(result.success)
        assertNull(mockRatingRepository.ratings["rating_delete"])
    }

    @Test
    fun testDeleteRatingWrongUser() = runBlocking {
        val rating = ProductRating(
            id = "rating_nodelete",
            productId = "prod_baby_essentials",
            uid = "mom_beth",
            rating = 4,
            title = "Good",
            comment = "Nice"
        )
        mockRatingRepository.ratings["rating_nodelete"] = rating

        val result = ratingService.deleteRating("rating_nodelete", "mom_alice")

        assertFalse(result.success)
        assertNotNull(mockRatingRepository.ratings["rating_nodelete"])
    }

    @Test
    fun testGetRatingsByProductIdWithAverage() = runBlocking {
        mockRatingRepository.ratings.clear()
        mockRatingRepository.ratings["r1"] = ProductRating(
            id = "r1", productId = "prod_baby_essentials", uid = "mom_1", rating = 5,
            title = "Perfect", comment = "Love it"
        )
        mockRatingRepository.ratings["r2"] = ProductRating(
            id = "r2", productId = "prod_baby_essentials", uid = "mom_2", rating = 3,
            title = "OK", comment = "Average"
        )
        mockRatingRepository.ratings["r3"] = ProductRating(
            id = "r3", productId = "prod_baby_essentials", uid = "mom_3", rating = 4,
            title = "Good", comment = "Solid"
        )

        val result = ratingService.getRatingsByProductId("prod_baby_essentials", 0, 20)

        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals(3, result.data!!.totalRatings)
        assertEquals(4.0, result.data!!.averageRating)
        assertEquals(3, result.data!!.ratings.size)
    }

    @Test
    fun testGetMyRatings() = runBlocking {
        mockRatingRepository.ratings.clear()
        mockRatingRepository.ratings["my1"] = ProductRating(
            id = "my1", productId = "prod_baby_essentials", uid = "mom_beth", rating = 5,
            title = "Great", comment = "Loved it"
        )
        mockRatingRepository.ratings["my2"] = ProductRating(
            id = "my2", productId = "prod_prenatal", uid = "mom_beth", rating = 4,
            title = "Good", comment = "Helpful"
        )
        mockRatingRepository.ratings["other"] = ProductRating(
            id = "other", productId = "prod_baby_essentials", uid = "mom_alice", rating = 3,
            title = "Meh", comment = "Okay"
        )

        val result = ratingService.getMyRatings("mom_beth")

        assertTrue(result.success)
        assertEquals(2, result.data!!.size)
    }

    @Test
    fun testGetUserRatingForProductNotRatedYet() = runBlocking {
        mockRatingRepository.ratings.clear()

        val result = ratingService.getUserRatingForProduct("prod_baby_essentials", "mom_new")

        assertFalse(result.success)
        assertTrue(result.message!!.contains("not rated"))
    }
}

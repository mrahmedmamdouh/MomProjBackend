package com.evelolvetech.data.responses

import kotlin.test.*

class DataResponsesTest {

    @Test
    fun `BasicApiResponse should have correct properties for success`() {
        val response = BasicApiResponse(
            success = true,
            message = "Success",
            data = "test data"
        )

        assertTrue(response.success)
        assertEquals("test data", response.data)
        assertEquals("Success", response.message)
    }

    @Test
    fun `BasicApiResponse should have correct properties for error`() {
        val response = BasicApiResponse<Unit>(
            success = false,
            message = "Error occurred"
        )

        assertFalse(response.success)
        assertNull(response.data)
        assertEquals("Error occurred", response.message)
    }

    @Test
    fun `BasicApiResponse should handle null data`() {
        val response = BasicApiResponse<String>(
            success = true,
            message = "Success with null data",
            data = null
        )

        assertTrue(response.success)
        assertNull(response.data)
        assertEquals("Success with null data", response.message)
    }

    @Test
    fun `BasicApiResponse should handle null message`() {
        val response = BasicApiResponse(
            success = true,
            message = null,
            data = "test data"
        )

        assertTrue(response.success)
        assertEquals("test data", response.data)
        assertNull(response.message)
    }

    @Test
    fun `Response classes should be accessible`() {
        assertNotNull(BasicApiResponse::class)
    }

    @Test
    fun `BasicApiResponse should be data class`() {
        val response1 = BasicApiResponse(true, "message", "data")
        val response2 = BasicApiResponse(true, "message", "data")
        
        assertEquals(response1, response2)
        assertEquals(response1.hashCode(), response2.hashCode())
    }

    @Test
    fun `BasicApiResponse should work with different data types`() {
        val stringResponse = BasicApiResponse<String>(true, "Success", "string data")
        val intResponse = BasicApiResponse<Int>(true, "Success", 42)
        val listResponse = BasicApiResponse<List<Int>>(true, "Success", listOf(1, 2, 3))

        assertEquals("string data", stringResponse.data)
        assertEquals(42, intResponse.data)
        assertEquals(listOf(1, 2, 3), listResponse.data)
    }
}

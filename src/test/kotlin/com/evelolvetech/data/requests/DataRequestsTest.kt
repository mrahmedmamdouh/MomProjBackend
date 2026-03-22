package com.evelolvetech.data.requests

import kotlin.test.*

class DataRequestsTest {

    @Test
    fun `CreateCategoryRequest should have correct properties`() {
        val categoryRequest = CreateCategoryRequest(
            name = "Electronics",
            slug = "electronics"
        )

        assertEquals("Electronics", categoryRequest.name)
        assertEquals("electronics", categoryRequest.slug)
    }

    @Test
    fun `CreateCategoryRequest should handle null slug`() {
        val categoryRequest = CreateCategoryRequest(
            name = "Electronics",
            slug = null
        )

        assertEquals("Electronics", categoryRequest.name)
        assertNull(categoryRequest.slug)
    }

    @Test
    fun `AuthorizationRequest should have correct properties`() {
        val authRequest = AuthorizationRequest(
            isAuthorized = true
        )

        assertTrue(authRequest.isAuthorized)
    }

    @Test
    fun `AuthorizationRequest should handle false authorization`() {
        val authRequest = AuthorizationRequest(
            isAuthorized = false
        )

        assertFalse(authRequest.isAuthorized)
    }

    @Test
    fun `Request classes should be accessible`() {
        assertNotNull(CreateCategoryRequest::class)
        assertNotNull(UpdateCategoryRequest::class)
        assertNotNull(AuthorizationRequest::class)
    }

    @Test
    fun `Request classes should be data classes`() {
        val categoryRequest1 = CreateCategoryRequest("Electronics", "electronics")
        val categoryRequest2 = CreateCategoryRequest("Electronics", "electronics")
        
        assertEquals(categoryRequest1, categoryRequest2)
        assertEquals(categoryRequest1.hashCode(), categoryRequest2.hashCode())
    }
}

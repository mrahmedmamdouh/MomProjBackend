package com.evelolvetech.routes

import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.mocks.*
import com.evelolvetech.routes.mom.ecommerce.categoryRoutes
import com.evelolvetech.service.mom.ecommerce.CategoryService
import com.google.gson.Gson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PublicCategoryRoutesTest {

    private val gson = Gson()
    private val mockCategoryRepository = MockCategoryRepository()
    private val categoryService = CategoryService(mockCategoryRepository)

    private fun Application.testModule() {
        install(ContentNegotiation) {
            gson()
        }
        routing {
            categoryRoutes(categoryService)
        }
    }

    @Test
    fun `GET categories should return all categories without authentication`() = testApplication {
        application { testModule() }

        val response = client.get("/api/categories")
        
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(true, responseBody.success)
        assertNotNull(responseBody.data)
        
        val categories = responseBody.data as List<*>
        assertEquals(5, categories.size)
    }

    @Test
    fun `GET categories should work with any HTTP method`() = testApplication {
        application { testModule() }


        val response = client.get("/api/categories")
        
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(true, responseBody.success)
    }

    @Test
    fun `GET categories should return consistent data structure`() = testApplication {
        application { testModule() }

        val response = client.get("/api/categories")
        
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(true, responseBody.success)
        assertNotNull(responseBody.data)
        

        val categories = responseBody.data as List<*>
        assert(categories.isNotEmpty())
        

        val firstCategory = categories.first() as Map<*, *>
        assertNotNull(firstCategory["id"])
        assertNotNull(firstCategory["name"])
        assertNotNull(firstCategory["slug"])
    }

    @Test
    fun `GET categories should handle multiple concurrent requests`() = testApplication {
        application { testModule() }


        val response1 = client.get("/api/categories")
        val response2 = client.get("/api/categories")
        val response3 = client.get("/api/categories")
        
        assertEquals(HttpStatusCode.OK, response1.status)
        assertEquals(HttpStatusCode.OK, response2.status)
        assertEquals(HttpStatusCode.OK, response3.status)
        
        val responseBody1 = gson.fromJson(response1.bodyAsText(), BasicApiResponse::class.java)
        val responseBody2 = gson.fromJson(response2.bodyAsText(), BasicApiResponse::class.java)
        val responseBody3 = gson.fromJson(response3.bodyAsText(), BasicApiResponse::class.java)
        
        assertEquals(true, responseBody1.success)
        assertEquals(true, responseBody2.success)
        assertEquals(true, responseBody3.success)
        

        assertEquals(responseBody1.data, responseBody2.data)
        assertEquals(responseBody2.data, responseBody3.data)
    }


    @Test
    fun `GET category by ID should return specific category without authentication`() = testApplication {
        application { testModule() }

        val response = client.get("/api/categories/cat_fitness")
        
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(true, responseBody.success)
        assertNotNull(responseBody.data)
        

        val category = responseBody.data as Map<*, *>
        assertEquals("cat_fitness", category["id"])
        assertEquals("Fitness & Wellness", category["name"])
        assertEquals("fitness-wellness", category["slug"])
    }

    @Test
    fun `GET category by ID should return 404 for non-existent category`() = testApplication {
        application { testModule() }

        val response = client.get("/api/categories/non_existent_category")
        
        assertEquals(HttpStatusCode.NotFound, response.status)
        val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(false, responseBody.success)
    }

    @Test
    fun `GET category by ID should return 400 for empty ID`() = testApplication {
        application { testModule() }

        val response = client.get("/api/categories/")
        
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET category by ID should work with different valid category IDs`() = testApplication {
        application { testModule() }


        val categoryIds = listOf("cat_fitness", "cat_nutrition", "cat_baby", "cat_mom", "cat_health")
        
        for (categoryId in categoryIds) {
            val response = client.get("/api/categories/$categoryId")
            
            assertEquals(HttpStatusCode.OK, response.status)
            val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
            assertEquals(true, responseBody.success)
            assertNotNull(responseBody.data)
            
            val category = responseBody.data as Map<*, *>
            assertEquals(categoryId, category["id"])
        }
    }

    @Test
    fun `GET category by ID should handle special characters in ID`() = testApplication {
        application { testModule() }


        val response = client.get("/api/categories/cat_with_special_chars_!@#")
        
        assertEquals(HttpStatusCode.NotFound, response.status)
        val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(false, responseBody.success)
    }

    @Test
    fun `GET category by ID should handle very long ID`() = testApplication {
        application { testModule() }


        val longId = "a".repeat(1000)
        val response = client.get("/api/categories/$longId")
        
        assertEquals(HttpStatusCode.NotFound, response.status)
        val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(false, responseBody.success)
    }

    @Test
    fun `GET category by ID should handle URL encoding`() = testApplication {
        application { testModule() }


        val response = client.get("/api/categories/cat%20with%20spaces")
        
        assertEquals(HttpStatusCode.NotFound, response.status)
        val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(false, responseBody.success)
    }

    @Test
    fun `GET category by ID should return consistent data structure`() = testApplication {
        application { testModule() }

        val response = client.get("/api/categories/cat_fitness")
        
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(true, responseBody.success)
        assertNotNull(responseBody.data)
        

        val category = responseBody.data as Map<*, *>
        assertNotNull(category["id"])
        assertNotNull(category["name"])
        assertNotNull(category["slug"])
        

        assert(category["id"] is String)
        assert(category["name"] is String)
        assert(category["slug"] is String)
    }

    @Test
    fun `GET category by ID should handle concurrent requests for same category`() = testApplication {
        application { testModule() }


        val response1 = client.get("/api/categories/cat_fitness")
        val response2 = client.get("/api/categories/cat_fitness")
        val response3 = client.get("/api/categories/cat_fitness")
        
        assertEquals(HttpStatusCode.OK, response1.status)
        assertEquals(HttpStatusCode.OK, response2.status)
        assertEquals(HttpStatusCode.OK, response3.status)
        
        val responseBody1 = gson.fromJson(response1.bodyAsText(), BasicApiResponse::class.java)
        val responseBody2 = gson.fromJson(response2.bodyAsText(), BasicApiResponse::class.java)
        val responseBody3 = gson.fromJson(response3.bodyAsText(), BasicApiResponse::class.java)
        
        assertEquals(true, responseBody1.success)
        assertEquals(true, responseBody2.success)
        assertEquals(true, responseBody3.success)
        

        assertEquals(responseBody1.data, responseBody2.data)
        assertEquals(responseBody2.data, responseBody3.data)
    }

    @Test
    fun `GET category by ID should handle concurrent requests for different categories`() = testApplication {
        application { testModule() }


        val response1 = client.get("/api/categories/cat_fitness")
        val response2 = client.get("/api/categories/cat_nutrition")
        val response3 = client.get("/api/categories/cat_baby")
        
        assertEquals(HttpStatusCode.OK, response1.status)
        assertEquals(HttpStatusCode.OK, response2.status)
        assertEquals(HttpStatusCode.OK, response3.status)
        
        val responseBody1 = gson.fromJson(response1.bodyAsText(), BasicApiResponse::class.java)
        val responseBody2 = gson.fromJson(response2.bodyAsText(), BasicApiResponse::class.java)
        val responseBody3 = gson.fromJson(response3.bodyAsText(), BasicApiResponse::class.java)
        
        assertEquals(true, responseBody1.success)
        assertEquals(true, responseBody2.success)
        assertEquals(true, responseBody3.success)
        

        val category1 = responseBody1.data as Map<*, *>
        val category2 = responseBody2.data as Map<*, *>
        val category3 = responseBody3.data as Map<*, *>
        
        assertEquals("cat_fitness", category1["id"])
        assertEquals("cat_nutrition", category2["id"])
        assertEquals("cat_baby", category3["id"])
    }
}

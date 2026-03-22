package com.evelolvetech.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.evelolvetech.data.models.UserType
import com.evelolvetech.data.requests.CreateCategoryRequest
import com.evelolvetech.data.requests.UpdateCategoryRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.mocks.*
import com.evelolvetech.routes.admin.adminCategoryRoutes
import com.evelolvetech.service.mom.ecommerce.CategoryService
import com.google.gson.Gson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AdminCategoryRoutesTest {

    private val gson = Gson()
    private val algorithm = Algorithm.HMAC256("test-secret")
    private val mockCategoryRepository = MockCategoryRepository()
    private val categoryService = CategoryService(mockCategoryRepository)

    init {


    }

    private fun Application.testModule() {
        install(ContentNegotiation) {
            gson()
        }
        install(Authentication) {
            jwt("jwt") {
                verifier(
                    JWT.require(algorithm)
                        .withAudience("jwt-audience")
                        .withIssuer("https://jwt-provider-domain/")
                        .build()
                )
                realm = "Mom Care Platform"
                validate { credential ->
                    if (credential.payload.getClaim("userId").asString() != "") {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                }
            }
        }
        routing {
            adminCategoryRoutes(categoryService, gson)
        }
    }

    private fun generateAdminToken(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "admin_test")
            .withClaim("userType", "ADMIN")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }

    private fun generateMomToken(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "mom_test")
            .withClaim("userType", "MOM")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }

    private fun generateDoctorToken(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "doctor_test")
            .withClaim("userType", "DOCTOR")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }


    @Test
    fun `POST admin categories with valid admin token should succeed`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val createRequest = CreateCategoryRequest(
            name = "New Category",
            slug = "new-category"
        )
        
        val response = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }
        
        assertEquals(HttpStatusCode.Created, response.status)
        val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
        assertNotNull(responseBody.data)
    }

    @Test
    fun `POST admin categories with mom token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateMomToken()
        val createRequest = CreateCategoryRequest(
            name = "New Category",
            slug = "new-category"
        )

        val response = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST admin categories with doctor token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateDoctorToken()
        val createRequest = CreateCategoryRequest(
            name = "New Category",
            slug = "new-category"
        )

        val response = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST admin categories without token should return unauthorized`() = testApplication {
        application { testModule() }

        val createRequest = CreateCategoryRequest(
            name = "New Category",
            slug = "new-category"
        )

        val response = client.post("/api/admin/categories") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST admin categories with invalid JSON should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()

        val response = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("invalid json")
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST admin categories with empty name should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val createRequest = CreateCategoryRequest(
            name = "",
            slug = "test-slug"
        )

        val response = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST admin categories with duplicate name should return conflict`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val createRequest = CreateCategoryRequest(
            name = "Fitness & Wellness", // This name already exists in mock data
            slug = "different-slug"
        )

        val response = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }
        
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `POST admin categories with duplicate slug should return conflict`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val createRequest = CreateCategoryRequest(
            name = "Different Name",
            slug = "fitness-wellness" // This slug already exists in mock data
        )

        val response = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }
        
        assertEquals(HttpStatusCode.Conflict, response.status)
    }


    @Test
    fun `PUT admin categories with valid admin token should succeed`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val updateRequest = UpdateCategoryRequest(
            name = "Updated Category",
            slug = "updated-category"
        )
        
        val response = client.put("/api/admin/categories/cat_fitness") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT admin categories with mom token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateMomToken()
        val updateRequest = UpdateCategoryRequest(
            name = "Updated Category",
            slug = "updated-category"
        )
        
        val response = client.put("/api/admin/categories/cat_fitness") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT admin categories with doctor token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateDoctorToken()
        val updateRequest = UpdateCategoryRequest(
            name = "Updated Category",
            slug = "updated-category"
        )
        
        val response = client.put("/api/admin/categories/cat_fitness") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT admin categories without token should return unauthorized`() = testApplication {
        application { testModule() }

        val updateRequest = UpdateCategoryRequest(
            name = "Updated Category",
            slug = "updated-category"
        )
        
        val response = client.put("/api/admin/categories/cat_fitness") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT admin categories with invalid JSON should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()

        val response = client.put("/api/admin/categories/cat_fitness") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("invalid json")
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT admin categories with missing ID should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val updateRequest = UpdateCategoryRequest(
            name = "Updated Category",
            slug = "updated-category"
        )
        
        val response = client.put("/api/admin/categories/") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT admin categories with non-existent ID should return success false`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val updateRequest = UpdateCategoryRequest(
            name = "Updated Category",
            slug = "updated-category"
        )
        
        val response = client.put("/api/admin/categories/non_existent_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(false, responseBody.success)
    }


    @Test
    fun `DELETE admin categories with valid admin token should succeed`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        
        val response = client.delete("/api/admin/categories/cat_fitness") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `DELETE admin categories with mom token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateMomToken()
        
        val response = client.delete("/api/admin/categories/cat_fitness") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `DELETE admin categories with doctor token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateDoctorToken()
        
        val response = client.delete("/api/admin/categories/cat_fitness") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `DELETE admin categories without token should return unauthorized`() = testApplication {
        application { testModule() }
        
        val response = client.delete("/api/admin/categories/cat_fitness")
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `DELETE admin categories with missing ID should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        
        val response = client.delete("/api/admin/categories/") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `DELETE admin categories with non-existent ID should return success false`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        
        val response = client.delete("/api/admin/categories/non_existent_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(false, responseBody.success)
    }
}

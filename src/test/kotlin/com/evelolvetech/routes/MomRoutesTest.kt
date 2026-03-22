package com.evelolvetech.routes

import com.evelolvetech.data.models.Mom
import com.evelolvetech.data.models.UserType
import com.evelolvetech.mocks.*
import com.evelolvetech.routes.mom.momRoutes
import com.evelolvetech.service.mom.MomService
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import com.google.gson.JsonObject
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class MomRoutesTest {

    private val mockMomRepository = MockMomRepository()
    private val mockNidRepository = MockNidRepository()
    private val mockUserRepository = MockUserRepository()
    private val mockHashingService = MockHashingService()
    private val mockTransactionService = MockTransactionService()
    private val gson = Gson()

    private val momService = MomService(
        mockMomRepository,
        mockNidRepository,
        mockUserRepository,
        mockHashingService,
        mockTransactionService,
        MockAuthConfig.instance
    )

    private val jwtSecret = "secret"
    private val algorithm = Algorithm.HMAC256(jwtSecret)

    private fun generateMomToken(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "mom_test_id")
            .withClaim("userType", "MOM")
            .withClaim("email", "test@mom.com")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }

    private fun Application.testModule() {
        install(ContentNegotiation) {
            gson()
        }
        routing {
            momRoutes(momService, gson)
        }
    }

    init {
        val testMom = Mom(
            id = "mom_test_id",
            authUid = "auth_test_mom",
            fullName = "Test Mom",
            email = "test@mom.com",
            phone = "+1234567890",
            maritalStatus = "MARRIED",
            numberOfSessions = 10,
            isAuthorized = true,
            nidId = "nid_test_mom_id",
            nidRef = "nid_test_mom_ref",
            photoUrl = "test_photo.jpg",
            createdAt = System.currentTimeMillis()
        )

        mockMomRepository.moms["mom_test_id"] = testMom
    }

    @Test
    fun `GET mom profile should not expose numberOfSessions in response`() = testApplication {
        application { testModule() }

        val momToken = generateMomToken()
        
        val response = client.get("/api/moms/profile") {
            header(HttpHeaders.Authorization, "Bearer $momToken")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
        
        assertNotNull(jsonResponse)
        assertEquals(true, jsonResponse.get("success").asBoolean)
        
        val data = jsonResponse.getAsJsonObject("data")
        assertNotNull(data)
        
        assertFalse(data.has("numberOfSessions"), "Mom profile response should not contain numberOfSessions")
        assertEquals("mom_test_id", data.get("id").asString)
        assertEquals("Test Mom", data.get("fullName").asString)
        assertEquals("test@mom.com", data.get("email").asString)
    }

    @Test
    fun `GET mom check-authorization should not expose numberOfSessions in response`() = testApplication {
        application { testModule() }

        val momToken = generateMomToken()
        
        val response = client.get("/api/moms/check-authorization") {
            header(HttpHeaders.Authorization, "Bearer $momToken")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
        
        assertNotNull(jsonResponse)
        assertEquals(true, jsonResponse.get("success").asBoolean)
        
        val data = jsonResponse.getAsJsonObject("data")
        assertNotNull(data)
        
        assertFalse(data.has("numberOfSessions"), "Mom authorization response should not contain numberOfSessions")
        assertEquals("mom_test_id", data.get("momId").asString)
        assertEquals(true, data.get("isAuthorized").asBoolean)
    }
}

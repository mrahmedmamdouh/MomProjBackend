package com.evelolvetech.routes

import com.evelolvetech.data.models.UserType
import com.evelolvetech.mocks.*
import com.evelolvetech.routes.auth.authRoutes
import com.evelolvetech.service.auth.AuthService
import com.evelolvetech.service.doctor.DoctorService
import com.evelolvetech.service.mom.MomService
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class AuthRoutesTest {

    private val mockUserRepository = MockUserRepository()
    private val mockHashingService = MockHashingService()
    private val mockRefreshTokenRepository = MockRefreshTokenRepository()
    private val mockAuthConfig = MockAuthConfig.instance
    private val mockMomRepository = MockMomRepository()
    private val mockDoctorRepository = MockDoctorRepository()
    private val mockNidRepository = MockNidRepository()
    private val mockTransactionService = MockTransactionService()

    private val authService = AuthService(mockUserRepository, mockHashingService, mockRefreshTokenRepository, mockAuthConfig)
    private val momService = MomService(mockMomRepository, mockNidRepository, mockUserRepository, mockHashingService, mockTransactionService, mockAuthConfig)
    private val doctorService = DoctorService(mockDoctorRepository, mockNidRepository, mockUserRepository, mockHashingService, mockTransactionService)

    private val jwtIssuer = "https://jwt-provider-domain/"
    private val jwtAudience = "jwt-audience"
    private val jwtSecret = "test-secret"
    private val algorithm = Algorithm.HMAC256(jwtSecret)
    private val gson = Gson()

    private fun Application.testModule() {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Authentication) {
            jwt {
                verifier(
                    JWT.require(algorithm)
                        .withAudience(jwtAudience)
                        .withIssuer(jwtIssuer)
                        .build()
                )
                validate { credential ->
                    if (credential.payload.audience.contains(jwtAudience)) {
                        JWTPrincipal(credential.payload)
                    } else null
                }
            }
        }

        routing {
            authRoutes(authService, momService, doctorService, jwtIssuer, jwtAudience, jwtSecret, gson)
        }
    }

    @Test
    fun `test doctor registration security fix - invalid JSON with files should return 400`() = testApplication {
        application {
            testModule()
        }

        val response = client.post("/api/auth/register/doctor") {
            setBody(
                """
                --boundary
                Content-Disposition: form-data; name="data"

                {"invalid": "json format"
                --boundary
                Content-Disposition: form-data; name="photo"; filename="test.jpg"
                Content-Type: image/jpeg

                fake-image-data
                --boundary--
                """.trimIndent()
            )
            contentType(ContentType.MultiPart.FormData.withParameter("boundary", "boundary"))
        }


        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `test mom registration security - invalid JSON with files should return 400`() = testApplication {
        application {
            testModule()
        }

        val response = client.post("/api/auth/register/mom") {
            setBody(
                """
                --boundary
                Content-Disposition: form-data; name="data"

                {"invalid": "json format"
                --boundary
                Content-Disposition: form-data; name="photo"; filename="test.jpg"
                Content-Type: image/jpeg

                fake-image-data
                --boundary--
                """.trimIndent()
            )
            contentType(ContentType.MultiPart.FormData.withParameter("boundary", "boundary"))
        }


        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `test login endpoint exists and responds`() = testApplication {
        application {
            testModule()
        }

        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }


        assertNotEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `test refresh endpoint exists and responds`() = testApplication {
        application {
            testModule()
        }

        val response = client.post("/api/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }


        assertNotEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `test logout endpoint exists and responds`() = testApplication {
        application {
            testModule()
        }

        val response = client.post("/api/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }


        assertNotEquals(HttpStatusCode.NotFound, response.status)
    }
}
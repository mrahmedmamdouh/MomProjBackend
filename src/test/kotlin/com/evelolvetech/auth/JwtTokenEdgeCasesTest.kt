package com.evelolvetech.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.evelolvetech.data.requests.AuthorizationRequest
import com.evelolvetech.mocks.*
import com.evelolvetech.routes.admin.adminRoutes
import com.evelolvetech.service.mom.ecommerce.CategoryService
import com.evelolvetech.service.doctor.DoctorService
import com.evelolvetech.service.mom.ecommerce.ProductService
import com.evelolvetech.service.mom.ecommerce.SkuOfferService
import com.evelolvetech.service.mom.MomService
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
import kotlin.test.*

class JwtTokenEdgeCasesTest {

    private val mockCategoryRepository = MockCategoryRepository()
    private val mockProductRepository = MockProductRepository()
    private val mockSkuOfferRepository = MockSkuOfferRepository()
    private val mockSkuRepository = MockSkuRepository()
    private val mockSellerRepository = MockSellerRepository()
    private val mockDoctorRepository = MockDoctorRepository()
    private val mockNidRepository = MockNidRepository()
    private val mockHashingService = MockHashingService()
    private val mockMomRepository = MockMomRepository()
    private val mockUserRepository = MockUserRepository()
    private val mockTransactionService = MockTransactionService()

    private val categoryService = CategoryService(mockCategoryRepository)
    private val productService = ProductService(mockProductRepository, mockSellerRepository, mockCategoryRepository, mockMomRepository, MockAuthConfig.instance)
    private val skuOfferService = SkuOfferService(mockSkuOfferRepository, mockMomRepository, MockAuthConfig.instance)
    private val doctorService = DoctorService(mockDoctorRepository, mockNidRepository, mockUserRepository, mockHashingService, mockTransactionService)
    private val momService = MomService(mockMomRepository, mockNidRepository, mockUserRepository, mockHashingService, mockTransactionService, MockAuthConfig.instance)

    private val jwtSecret = "secret"
    private val wrongSecret = "wrong-secret"
    private val algorithm = Algorithm.HMAC256(jwtSecret)
    private val wrongAlgorithm = Algorithm.HMAC256(wrongSecret)
    private val gson = Gson()

    init {

        val testDoctor = com.evelolvetech.data.models.Doctor(
            id = "doctor_test_id",
            name = "Test Doctor",
            email = "test@doctor.com",
            phone = "+1234567890",
            specialization = "CLINICAL_PSYCHOLOGIST",
            isAuthorized = false,
            authUid = "auth_test_doctor",
            nidId = "nid_test_id",
            nidRef = "nid_test_ref"
        )
        mockDoctorRepository.doctors["doctor_test_id"] = testDoctor
    }

    private fun Application.testModule() {
        install(ContentNegotiation) {
            gson()
        }

        install(Authentication) {
            jwt {
                verifier(
                    JWT.require(algorithm)
                        .withAudience("jwt-audience")
                        .withIssuer("https://jwt-provider-domain/")
                        .build()
                )
                validate { credential ->
                    if (credential.payload.audience.contains("jwt-audience")) {
                        JWTPrincipal(credential.payload)
                    } else null
                }
            }
        }

        routing {
            adminRoutes(doctorService, categoryService, productService, skuOfferService, momService, gson)
        }
    }


    private fun generateValidAdminToken(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "admin_main")
            .withClaim("userType", "ADMIN")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }

    private fun generateExpiredToken(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "admin_main")
            .withClaim("userType", "ADMIN")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() - 3600000)) // Expired 1 hour ago
            .sign(algorithm)
    }

    private fun generateTokenWithWrongSecret(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "admin_main")
            .withClaim("userType", "ADMIN")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(wrongAlgorithm)
    }

    private fun generateTokenWithWrongUserType(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "admin_main")
            .withClaim("userType", "INVALID_TYPE")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }

    private fun generateTokenWithMissingUserId(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userType", "ADMIN")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }

    private fun generateTokenWithMissingUserType(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "admin_main")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }

    private fun generateTokenWithWrongAudience(): String {
        return JWT.create()
            .withAudience("wrong-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "admin_main")
            .withClaim("userType", "ADMIN")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }

    private fun generateTokenWithWrongIssuer(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://wrong-issuer/")
            .withClaim("userId", "admin_main")
            .withClaim("userType", "ADMIN")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }


    @Test
    fun `PUT admin doctors authorize with expired token should return unauthorized`() = testApplication {
        application { testModule() }

        val expiredToken = generateExpiredToken()
        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "Bearer $expiredToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET admin doctors status with expired token should return unauthorized`() = testApplication {
        application { testModule() }

        val expiredToken = generateExpiredToken()
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Bearer $expiredToken")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }


    @Test
    fun `PUT admin doctors authorize with malformed token should return unauthorized`() = testApplication {
        application { testModule() }

        val malformedToken = "not.a.valid.jwt.token"
        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "Bearer $malformedToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET admin doctors status with malformed token should return unauthorized`() = testApplication {
        application { testModule() }

        val malformedToken = "invalid.jwt"
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Bearer $malformedToken")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT admin doctors authorize with empty token should return unauthorized`() = testApplication {
        application { testModule() }

        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "Bearer ")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }


    @Test
    fun `PUT admin doctors authorize with wrong secret token should return unauthorized`() = testApplication {
        application { testModule() }

        val wrongSecretToken = generateTokenWithWrongSecret()
        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "Bearer $wrongSecretToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET admin doctors status with wrong secret token should return unauthorized`() = testApplication {
        application { testModule() }

        val wrongSecretToken = generateTokenWithWrongSecret()
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Bearer $wrongSecretToken")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }


    @Test
    fun `PUT admin doctors authorize with wrong user type should return forbidden`() = testApplication {
        application { testModule() }

        val wrongUserTypeToken = generateTokenWithWrongUserType()
        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "Bearer $wrongUserTypeToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `GET admin doctors status with wrong user type should return forbidden`() = testApplication {
        application { testModule() }

        val wrongUserTypeToken = generateTokenWithWrongUserType()
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Bearer $wrongUserTypeToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }


    @Test
    fun `PUT admin doctors authorize with missing userId claim should return unauthorized`() = testApplication {
        application { testModule() }

        val missingUserIdToken = generateTokenWithMissingUserId()
        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "Bearer $missingUserIdToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET admin doctors status with missing userId claim should return unauthorized`() = testApplication {
        application { testModule() }

        val missingUserIdToken = generateTokenWithMissingUserId()
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Bearer $missingUserIdToken")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT admin doctors authorize with missing userType claim should return forbidden`() = testApplication {
        application { testModule() }

        val missingUserTypeToken = generateTokenWithMissingUserType()
        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "Bearer $missingUserTypeToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `GET admin doctors status with missing userType claim should return forbidden`() = testApplication {
        application { testModule() }

        val missingUserTypeToken = generateTokenWithMissingUserType()
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Bearer $missingUserTypeToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }


    @Test
    fun `PUT admin doctors authorize with wrong audience should return unauthorized`() = testApplication {
        application { testModule() }

        val wrongAudienceToken = generateTokenWithWrongAudience()
        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "Bearer $wrongAudienceToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET admin doctors status with wrong audience should return unauthorized`() = testApplication {
        application { testModule() }

        val wrongAudienceToken = generateTokenWithWrongAudience()
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Bearer $wrongAudienceToken")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT admin doctors authorize with wrong issuer should return unauthorized`() = testApplication {
        application { testModule() }

        val wrongIssuerToken = generateTokenWithWrongIssuer()
        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "Bearer $wrongIssuerToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET admin doctors status with wrong issuer should return unauthorized`() = testApplication {
        application { testModule() }

        val wrongIssuerToken = generateTokenWithWrongIssuer()
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Bearer $wrongIssuerToken")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }


    @Test
    fun `PUT admin doctors authorize with invalid authorization header format should return unauthorized`() = testApplication {
        application { testModule() }

        val validToken = generateValidAdminToken()
        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "InvalidFormat $validToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET admin doctors status with invalid authorization header format should return unauthorized`() = testApplication {
        application { testModule() }

        val validToken = generateValidAdminToken()
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Token $validToken")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }


    @Test
    fun `PUT admin doctors authorize with null authorization header should return unauthorized`() = testApplication {
        application { testModule() }

        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, null)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET admin doctors status with null authorization header should return unauthorized`() = testApplication {
        application { testModule() }
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, null)
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }


    @Test
    fun `PUT admin doctors authorize with valid token should succeed`() = testApplication {
        application { testModule() }

        val validToken = generateValidAdminToken()
        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "Bearer $validToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET admin doctors status with valid token should succeed`() = testApplication {
        application { testModule() }

        val validToken = generateValidAdminToken()
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Bearer $validToken")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }
}

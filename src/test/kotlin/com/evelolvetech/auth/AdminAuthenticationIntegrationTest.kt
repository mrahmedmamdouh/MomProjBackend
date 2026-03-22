package com.evelolvetech.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.evelolvetech.data.models.UserType
import com.evelolvetech.data.requests.AuthorizationRequest
import com.evelolvetech.data.requests.CreateCategoryRequest
import com.evelolvetech.data.requests.CreateProductRequest
import com.evelolvetech.data.requests.CreateSkuOfferRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.mocks.*
import com.evelolvetech.routes.admin.adminRoutes
import com.evelolvetech.service.auth.AuthService
import com.evelolvetech.service.doctor.DoctorService
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.service.mom.ecommerce.CategoryService
import com.evelolvetech.service.mom.ecommerce.ProductService
import com.evelolvetech.service.mom.ecommerce.SkuOfferService
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

class AdminAuthenticationIntegrationTest {

    private val gson = Gson()
    private val algorithm = Algorithm.HMAC256("test-secret")
    

    private val mockMomRepository = MockMomRepository()
    private val mockDoctorRepository = MockDoctorRepository()
    private val mockCategoryRepository = MockCategoryRepository()
    private val mockProductRepository = MockProductRepository()
    private val mockSkuOfferRepository = MockSkuOfferRepository()
    private val mockCartRepository = MockCartRepository()
    private val mockSellerRepository = MockSellerRepository()
    private val mockNidRepository = MockNidRepository()
    private val mockUserRepository = MockUserRepository()
    private val mockHashingService = MockHashingService()
    private val mockTransactionService = MockTransactionService()
    private val mockRefreshTokenRepository = MockRefreshTokenRepository()
    private val mockAuthConfig = MockAuthConfig.instance


    private val categoryService = CategoryService(mockCategoryRepository)
    private val productService = ProductService(mockProductRepository, mockSellerRepository, mockCategoryRepository, mockMomRepository, mockAuthConfig)
    private val skuOfferService = SkuOfferService(mockSkuOfferRepository, mockMomRepository, mockAuthConfig)
    private val doctorService = DoctorService(mockDoctorRepository, mockNidRepository, mockUserRepository, mockHashingService, mockTransactionService)
    private val momService = MomService(mockMomRepository, mockNidRepository, mockUserRepository, mockHashingService, mockTransactionService, mockAuthConfig)
    private val authService = AuthService(mockUserRepository, mockHashingService, mockRefreshTokenRepository, mockAuthConfig)

    init {

        mockDoctorRepository.doctors["doctor_integration_test"] = com.evelolvetech.data.models.Doctor(
            id = "doctor_integration_test",
            authUid = "auth_doctor_integration_test",
            name = "Dr. Integration Test",
            email = "dr.integration@example.com",
            phone = "+1-555-9999",
            specialization = "CLINICAL_PSYCHOLOGIST",
            isAuthorized = false,
            photo = "/uploads/profiles/dr_integration.jpg",
            nidId = "nid_dr_integration",
            nidRef = "nid_dr_integration_ref"
        )
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
            adminRoutes(doctorService, categoryService, productService, skuOfferService, momService, gson)
        }
    }

    private fun generateAdminToken(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "admin_integration_test")
            .withClaim("userType", "ADMIN")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }

    @Test
    fun `Complete admin workflow - authorize doctor, create category, create product, create sku offer`() = testApplication {
        application { testModule() }

        val adminToken = generateAdminToken()


        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        val authorizeResponse = client.put("/api/admin/doctors/doctor_integration_test/authorize") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }
        assertEquals(HttpStatusCode.OK, authorizeResponse.status)
        val authorizeResponseBody = gson.fromJson(authorizeResponse.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(true, authorizeResponseBody.success)


        val statusResponse = client.get("/api/admin/doctors/doctor_integration_test/status") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }
        assertEquals(HttpStatusCode.OK, statusResponse.status)
        val statusResponseBody = gson.fromJson(statusResponse.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(true, statusResponseBody.success)
        assertNotNull(statusResponseBody.data)


        val createCategoryRequest = CreateCategoryRequest(
            name = "Integration Test Category",
            slug = "integration-test-category"
        )
        val categoryResponse = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createCategoryRequest))
        }
        assertEquals(HttpStatusCode.Created, categoryResponse.status)
        val categoryResponseBody = gson.fromJson(categoryResponse.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(true, categoryResponseBody.success)
        assertNotNull(categoryResponseBody.data)


        val createProductRequest = CreateProductRequest(
            name = "Integration Test Product",
            slug = "integration-test-product",
            description = "A product created during integration testing",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"), // Use existing category
            minSessionsToPurchase = 5
        )
        val productResponse = client.post("/api/admin/products") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createProductRequest))
        }
        assertEquals(HttpStatusCode.OK, productResponse.status)
        val productResponseBody = gson.fromJson(productResponse.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(true, productResponseBody.success)


        val createSkuOfferRequest = CreateSkuOfferRequest(
            skuId = "sku_integration_test",
            sellerId = "seller_happy",
            listPrice = 150.0,
            salePrice = 120.0,
            currency = "USD",
            activeFrom = System.currentTimeMillis(),
            activeTo = System.currentTimeMillis() + 86400000 // 24 hours
        )
        val skuOfferResponse = client.post("/api/admin/sku-offers") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createSkuOfferRequest))
        }
        assertEquals(HttpStatusCode.OK, skuOfferResponse.status)
        val skuOfferResponseBody = gson.fromJson(skuOfferResponse.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(true, skuOfferResponseBody.success)
    }

    @Test
    fun `Admin workflow with unauthorized access attempts should fail`() = testApplication {
        application { testModule() }

        val momToken = generateMomToken()
        val doctorToken = generateDoctorToken()


        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        val momAuthorizeResponse = client.put("/api/admin/doctors/doctor_integration_test/authorize") {
            header(HttpHeaders.Authorization, "Bearer $momToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }
        assertEquals(HttpStatusCode.Forbidden, momAuthorizeResponse.status)


        val doctorAuthorizeResponse = client.put("/api/admin/doctors/doctor_integration_test/authorize") {
            header(HttpHeaders.Authorization, "Bearer $doctorToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }
        assertEquals(HttpStatusCode.Forbidden, doctorAuthorizeResponse.status)


        val createCategoryRequest = CreateCategoryRequest(
            name = "Unauthorized Category",
            slug = "unauthorized-category"
        )
        val momCategoryResponse = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $momToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createCategoryRequest))
        }
        assertEquals(HttpStatusCode.Forbidden, momCategoryResponse.status)


        val createProductRequest = CreateProductRequest(
            name = "Unauthorized Product",
            slug = "unauthorized-product",
            description = "A product created with unauthorized access",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 0
        )
        val doctorProductResponse = client.post("/api/admin/products") {
            header(HttpHeaders.Authorization, "Bearer $doctorToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createProductRequest))
        }
        assertEquals(HttpStatusCode.Forbidden, doctorProductResponse.status)
    }

    @Test
    fun `Admin workflow with invalid data should return appropriate errors`() = testApplication {
        application { testModule() }

        val adminToken = generateAdminToken()


        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        val invalidDoctorResponse = client.put("/api/admin/doctors/non_existent_doctor/authorize") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }
        assertEquals(HttpStatusCode.BadRequest, invalidDoctorResponse.status)


        val invalidCategoryRequest = CreateCategoryRequest(
            name = "",
            slug = "invalid-category"
        )
        val invalidCategoryResponse = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(invalidCategoryRequest))
        }
        assertEquals(HttpStatusCode.BadRequest, invalidCategoryResponse.status)


        val invalidProductRequest = CreateProductRequest(
            name = "Invalid Product",
            slug = "invalid-product",
            description = "A product with invalid data",
            defaultSellerId = "invalid_seller",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 0
        )
        val invalidProductResponse = client.post("/api/admin/products") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(invalidProductRequest))
        }
        assertEquals(HttpStatusCode.BadRequest, invalidProductResponse.status)


        val invalidSkuOfferRequest = CreateSkuOfferRequest(
            skuId = "sku_invalid",
            sellerId = "seller_happy",
            listPrice = -10.0, // Invalid negative price
            salePrice = 5.0,
            currency = "USD"
        )
        val invalidSkuOfferResponse = client.post("/api/admin/sku-offers") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(invalidSkuOfferRequest))
        }
        assertEquals(HttpStatusCode.BadRequest, invalidSkuOfferResponse.status)
    }

    @Test
    fun `Admin workflow with database operations and state persistence`() = testApplication {
        application { testModule() }

        val adminToken = generateAdminToken()


        val createCategoryRequest = CreateCategoryRequest(
            name = "Persistent Category",
            slug = "persistent-category"
        )
        val categoryResponse = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createCategoryRequest))
        }
        assertEquals(HttpStatusCode.Created, categoryResponse.status)


        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        val authorizeResponse = client.put("/api/admin/doctors/doctor_integration_test/authorize") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }
        assertEquals(HttpStatusCode.OK, authorizeResponse.status)


        val statusResponse = client.get("/api/admin/doctors/doctor_integration_test/status") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }
        assertEquals(HttpStatusCode.OK, statusResponse.status)
        val statusResponseBody = gson.fromJson(statusResponse.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(true, statusResponseBody.success)


        val createProductRequest = CreateProductRequest(
            name = "Persistent Product",
            slug = "persistent-product",
            description = "A product that depends on persistent data",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"), // Use existing category
            minSessionsToPurchase = 3
        )
        val productResponse = client.post("/api/admin/products") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createProductRequest))
        }
        assertEquals(HttpStatusCode.OK, productResponse.status)


        val createSkuOfferRequest = CreateSkuOfferRequest(
            skuId = "sku_persistent",
            sellerId = "seller_happy",
            listPrice = 200.0,
            salePrice = 180.0,
            currency = "USD"
        )
        val skuOfferResponse = client.post("/api/admin/sku-offers") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createSkuOfferRequest))
        }
        assertEquals(HttpStatusCode.OK, skuOfferResponse.status)
    }

    private fun generateMomToken(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "mom_integration_test")
            .withClaim("userType", "MOM")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }

    private fun generateDoctorToken(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "doctor_integration_test")
            .withClaim("userType", "DOCTOR")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }
}

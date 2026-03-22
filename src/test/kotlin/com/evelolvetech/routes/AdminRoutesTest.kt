package com.evelolvetech.routes

import com.evelolvetech.auth.adminRoute
import com.evelolvetech.data.models.UserType
import com.evelolvetech.data.requests.CreateCategoryRequest
import com.evelolvetech.data.requests.UpdateCategoryRequest
import com.evelolvetech.data.requests.CreateProductRequest
import com.evelolvetech.data.requests.CreateSkuOfferRequest
import com.evelolvetech.data.requests.UpdateProductRequest
import com.evelolvetech.data.requests.UpdateSkuOfferRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.mocks.*
import com.evelolvetech.routes.admin.adminRoutes
import com.evelolvetech.routes.admin.adminCategoryRoutes
import com.evelolvetech.routes.admin.adminDoctorRoutes
import com.evelolvetech.routes.admin.adminProductRoutes
import com.evelolvetech.routes.admin.adminSkuOfferRoutes
import com.evelolvetech.service.mom.ecommerce.CategoryService
import com.evelolvetech.service.doctor.DoctorService
import com.evelolvetech.service.mom.ecommerce.ProductService
import com.evelolvetech.service.mom.ecommerce.SkuOfferService
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.util.SHA256HashingService
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
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

class AdminRoutesTest {

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

        val testMom = com.evelolvetech.data.models.Mom(
            id = "mom_test_id",
            fullName = "Test Mom",
            email = "test@mom.com",
            phone = "+1234567890",
            maritalStatus = "MARRIED",
            numberOfSessions = 10,
            isAuthorized = true,
            authUid = "auth_test_mom",
            nidId = "nid_test_mom_id",
            nidRef = "nid_test_mom_ref",
            photoUrl = "test_photo.jpg"
        )
        mockMomRepository.moms["mom_test_id"] = testMom
    }

    private val categoryService = CategoryService(mockCategoryRepository)
    private val productService = ProductService(mockProductRepository, mockSellerRepository, mockCategoryRepository, mockMomRepository, MockAuthConfig.instance)
    private val skuOfferService = SkuOfferService(mockSkuOfferRepository, mockMomRepository, MockAuthConfig.instance)
    private val doctorService = DoctorService(mockDoctorRepository, mockNidRepository, mockUserRepository, mockHashingService, mockTransactionService)
    private val momService = MomService(mockMomRepository, mockNidRepository, mockUserRepository, mockHashingService, mockTransactionService, MockAuthConfig.instance)

    private val jwtSecret = "secret"
    private val algorithm = Algorithm.HMAC256(jwtSecret)
    private val gson = Gson()

    private fun generateAdminToken(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "admin_main")
            .withClaim("userType", "ADMIN")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }

    private fun generateMomToken(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "mom_alice")
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

    @Test
    fun `POST admin products with valid admin token should succeed`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val createRequest = CreateProductRequest(
            name = "Test Product",
            slug = "test-product",
            description = "Test Description",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 0
        )
        
        val response = client.post("/api/admin/products") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST admin products with mom token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateMomToken()
        val createRequest = CreateProductRequest(
            name = "Test Product",
            slug = "test-product",
            description = "Test Description",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 0
        )
        
        val response = client.post("/api/admin/products") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `POST admin products without token should return unauthorized`() = testApplication {
        application { testModule() }

        val createRequest = CreateProductRequest(
            name = "Test Product",
            slug = "test-product",
            description = "Test Description", 
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 0
        )

        val response = client.post("/api/admin/products") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST admin categories with valid admin token should succeed`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val createRequest = CreateCategoryRequest(
            name = "Test Category",
            slug = "test-category"
        )
        
        val response = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `POST admin categories with mom token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateMomToken()
        val createRequest = CreateCategoryRequest(
            name = "Test Category",
            slug = "test-category"
        )

        val response = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `POST admin sku-offers with valid admin token should succeed`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val createRequest = CreateSkuOfferRequest(
            skuId = "sku_test",
            sellerId = "seller_test",
            listPrice = 100.0,
            salePrice = 90.0,
            currency = "USD"
        )

        val response = client.post("/api/admin/sku-offers") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST admin sku-offers with mom token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateMomToken()
        val createRequest = CreateSkuOfferRequest(
            skuId = "sku_test",
            sellerId = "seller_test",
            listPrice = 100.0,
            salePrice = 90.0,
            currency = "USD"
        )

        val response = client.post("/api/admin/sku-offers") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }


    @Test
    fun `PUT admin sku-offers with valid admin token should return success false for non-existent offer`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val updateRequest = UpdateSkuOfferRequest(
            listPrice = 120.0,
            salePrice = 100.0,
            currency = "USD",
            isActive = null,
            activeFrom = null,
            activeTo = null
        )
        
        val response = client.put("/api/admin/sku-offers/test_offer_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT admin sku-offers with mom token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateMomToken()
        val updateRequest = UpdateSkuOfferRequest(
            listPrice = 120.0,
            salePrice = 100.0,
            currency = "USD",
            isActive = true,
            activeFrom = System.currentTimeMillis(),
            activeTo = System.currentTimeMillis() + 86400000
        )
        
        val response = client.put("/api/admin/sku-offers/test_offer_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `PUT admin sku-offers with doctor token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateDoctorToken()
        val updateRequest = UpdateSkuOfferRequest(
            listPrice = 120.0,
            salePrice = 100.0,
            currency = "USD",
            isActive = true,
            activeFrom = System.currentTimeMillis(),
            activeTo = System.currentTimeMillis() + 86400000
        )
        
        val response = client.put("/api/admin/sku-offers/test_offer_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `PUT admin sku-offers without token should return unauthorized`() = testApplication {
        application { testModule() }

        val updateRequest = UpdateSkuOfferRequest(
            listPrice = 120.0,
            salePrice = 100.0,
            currency = "USD",
            isActive = true,
            activeFrom = System.currentTimeMillis(),
            activeTo = System.currentTimeMillis() + 86400000
        )
        
        val response = client.put("/api/admin/sku-offers/test_offer_id") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT admin sku-offers with invalid JSON should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()

        val response = client.put("/api/admin/sku-offers/test_offer_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("invalid json")
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT admin sku-offers with missing ID should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val updateRequest = UpdateSkuOfferRequest(
            listPrice = 120.0,
            salePrice = 100.0,
            currency = "USD",
            isActive = true,
            activeFrom = System.currentTimeMillis(),
            activeTo = System.currentTimeMillis() + 86400000
        )
        
        val response = client.put("/api/admin/sku-offers/") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT admin sku-offers with non-existent ID should return success false`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val updateRequest = UpdateSkuOfferRequest(
            listPrice = 120.0,
            salePrice = 100.0,
            currency = "USD",
            isActive = true,
            activeFrom = System.currentTimeMillis(),
            activeTo = System.currentTimeMillis() + 86400000
        )
        
        val response = client.put("/api/admin/sku-offers/non_existent_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }


    @Test
    fun `DELETE admin sku-offers with valid admin token should return success false for non-existent offer`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        
        val response = client.delete("/api/admin/sku-offers/test_offer_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `DELETE admin sku-offers with mom token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateMomToken()
        
        val response = client.delete("/api/admin/sku-offers/test_offer_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `DELETE admin sku-offers with doctor token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateDoctorToken()
        
        val response = client.delete("/api/admin/sku-offers/test_offer_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `DELETE admin sku-offers without token should return unauthorized`() = testApplication {
        application { testModule() }
        
        val response = client.delete("/api/admin/sku-offers/test_offer_id")
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `DELETE admin sku-offers with missing ID should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        
        val response = client.delete("/api/admin/sku-offers/") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `DELETE admin sku-offers with non-existent ID should return success false`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        
        val response = client.delete("/api/admin/sku-offers/non_existent_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }


    @Test
    fun `PUT admin products with valid admin token should succeed`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val updateRequest = UpdateProductRequest(
            name = "Updated Product",
            slug = "updated-product",
            description = "Updated Description",
            status = "ACTIVE",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 1
        )
        
        val response = client.put("/api/admin/products/test_product_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT admin products with mom token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateMomToken()
        val updateRequest = UpdateProductRequest(
            name = "Updated Product",
            slug = "updated-product",
            description = "Updated Description",
            status = "ACTIVE",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 1
        )
        
        val response = client.put("/api/admin/products/test_product_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `PUT admin products with doctor token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateDoctorToken()
        val updateRequest = UpdateProductRequest(
            name = "Updated Product",
            slug = "updated-product",
            description = "Updated Description",
            status = "ACTIVE",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 1
        )
        
        val response = client.put("/api/admin/products/test_product_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `PUT admin products without token should return unauthorized`() = testApplication {
        application { testModule() }

        val updateRequest = UpdateProductRequest(
            name = "Updated Product",
            slug = "updated-product",
            description = "Updated Description",
            status = "ACTIVE",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 1
        )
        
        val response = client.put("/api/admin/products/test_product_id") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT admin products with invalid JSON should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()

        val response = client.put("/api/admin/products/test_product_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("invalid json")
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT admin products with missing ID should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val updateRequest = UpdateProductRequest(
            name = "Updated Product",
            slug = "updated-product",
            description = "Updated Description",
            status = "ACTIVE",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 1
        )
        
        val response = client.put("/api/admin/products/") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT admin products with non-existent ID should return success false`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val updateRequest = UpdateProductRequest(
            name = "Updated Product",
            slug = "updated-product",
            description = "Updated Description",
            status = "ACTIVE",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 1
        )
        
        val response = client.put("/api/admin/products/non_existent_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(false, responseBody.success)
    }

    @Test
    fun `PUT admin products with empty name should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val updateRequest = UpdateProductRequest(
            name = "",
            slug = "updated-product",
            description = "Updated Description",
            status = "ACTIVE",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 1
        )
        
        val response = client.put("/api/admin/products/test_product_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT admin products with invalid seller ID should return success false`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val updateRequest = UpdateProductRequest(
            name = "Updated Product",
            slug = "updated-product",
            description = "Updated Description",
            status = "ACTIVE",
            defaultSellerId = "invalid_seller",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 1
        )
        
        val response = client.put("/api/admin/products/test_product_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = gson.fromJson(response.bodyAsText(), BasicApiResponse::class.java)
        assertEquals(false, responseBody.success)
    }

    @Test
    fun `PUT admin products with negative sessions should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val updateRequest = UpdateProductRequest(
            name = "Updated Product",
            slug = "updated-product",
            description = "Updated Description",
            status = "ACTIVE",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = -1
        )
        
        val response = client.put("/api/admin/products/test_product_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `DELETE admin products with valid admin token should succeed`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()

        val response = client.delete("/api/admin/products/test_product_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `DELETE admin products with mom token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateMomToken()

        val response = client.delete("/api/admin/products/test_product_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
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
        val updateRequest = CreateCategoryRequest(
            name = "Updated Category",
            slug = "updated-category"
        )
        
        val response = client.put("/api/admin/categories/test_category_id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `PUT admin doctors authorize with valid admin token should succeed`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val authorizeRequest = com.evelolvetech.data.requests.AuthorizationRequest(
            isAuthorized = true
        )
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT admin doctors authorize with mom token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateMomToken()
        val authorizeRequest = com.evelolvetech.data.requests.AuthorizationRequest(
            isAuthorized = true
        )
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `PUT admin doctors authorize without token should return unauthorized`() = testApplication {
        application { testModule() }

        val authorizeRequest = com.evelolvetech.data.requests.AuthorizationRequest(
            isAuthorized = true
        )
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT admin doctors authorize with invalid doctor ID should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        val authorizeRequest = com.evelolvetech.data.requests.AuthorizationRequest(
            isAuthorized = true
        )
        
        val response = client.put("/api/admin/doctors/ /authorize") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET admin doctors status with valid admin token should succeed`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET admin doctors status with mom token should return unauthorized`() = testApplication {
        application { testModule() }

        val token = generateMomToken()
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `GET admin doctors status without token should return unauthorized`() = testApplication {
        application { testModule() }
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET admin doctors status with invalid doctor ID should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        
        val response = client.get("/api/admin/doctors/ /status") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET admin moms sessions with valid admin token should return mom session data`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        
        val response = client.get("/api/admin/moms/mom_test_id/sessions") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = response.bodyAsText()
        val apiResponse = gson.fromJson(responseBody, BasicApiResponse::class.java)
        
        assertTrue(apiResponse.success)
        assertNotNull(apiResponse.data)
        
        val data = apiResponse.data as Map<*, *>
        assertEquals("mom_test_id", data["momId"])
        assertTrue(data.containsKey("sessionCount"))
        assertTrue(data.containsKey("lastActive"))
        assertTrue(data.containsKey("isAuthorized"))
    }

    @Test
    fun `GET admin moms sessions with mom token should return forbidden`() = testApplication {
        application { testModule() }

        val token = generateMomToken()
        
        val response = client.get("/api/admin/moms/mom_test_id/sessions") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `GET admin moms sessions with doctor token should return forbidden`() = testApplication {
        application { testModule() }

        val token = generateDoctorToken()
        
        val response = client.get("/api/admin/moms/mom_test_id/sessions") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `GET admin moms sessions without token should return unauthorized`() = testApplication {
        application { testModule() }
        
        val response = client.get("/api/admin/moms/mom_test_id/sessions")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET admin moms sessions with invalid mom ID should return bad request`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        
        val response = client.get("/api/admin/moms/ /sessions") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET admin moms sessions with non-existent mom ID should return not found`() = testApplication {
        application { testModule() }

        val token = generateAdminToken()
        
        val response = client.get("/api/admin/moms/non_existent_mom/sessions") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        
        val responseBody = response.bodyAsText()
        val apiResponse = gson.fromJson(responseBody, BasicApiResponse::class.java)
        
        assertFalse(apiResponse.success)
        assertEquals("Mom not found", apiResponse.message)
    }
}

package com.evelolvetech.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.evelolvetech.data.requests.AuthorizationRequest
import com.evelolvetech.data.requests.CreateCategoryRequest
import com.evelolvetech.data.requests.CreateProductRequest
import com.evelolvetech.data.requests.CreateSkuOfferRequest
import com.evelolvetech.mocks.*
import com.evelolvetech.routes.admin.adminRoutes
import com.evelolvetech.routes.doctor.doctorRoutes
import com.evelolvetech.routes.mom.momRoutes
import com.evelolvetech.routes.mom.ecommerce.productRoutes
import com.evelolvetech.routes.mom.ecommerce.skuOfferRoutes
import com.evelolvetech.routes.mom.ecommerce.cartRoutes
import com.evelolvetech.routes.mom.ecommerce.categoryRoutes
import com.evelolvetech.service.mom.ecommerce.CartService
import com.evelolvetech.service.mom.ecommerce.CategoryService
import com.evelolvetech.service.doctor.DoctorService
import com.evelolvetech.service.mom.MomService
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
import kotlin.test.*

class RoleBasedAccessControlTest {

    private val mockCategoryRepository = MockCategoryRepository()
    private val mockProductRepository = MockProductRepository()
    private val mockSkuOfferRepository = MockSkuOfferRepository()
    private val mockSkuRepository = MockSkuRepository()
    private val mockSellerRepository = MockSellerRepository()
    private val mockCartRepository = MockCartRepository()
    private val mockDoctorRepository = MockDoctorRepository()
    private val mockNidRepository = MockNidRepository()
    private val mockHashingService = MockHashingService()
    private val mockMomRepository = MockMomRepository()
    private val mockUserRepository = MockUserRepository()
    private val mockTransactionService = MockTransactionService()

    private val categoryService = CategoryService(mockCategoryRepository)
    private val productService = ProductService(mockProductRepository, mockSellerRepository, mockCategoryRepository, mockMomRepository, MockAuthConfig.instance)
    private val skuOfferService = SkuOfferService(mockSkuOfferRepository, mockMomRepository, MockAuthConfig.instance)
    private val cartService = CartService(mockCartRepository, mockMomRepository, mockSkuOfferRepository, MockAuthConfig.instance)
    private val doctorService = DoctorService(mockDoctorRepository, mockNidRepository, mockUserRepository, mockHashingService, mockTransactionService)
    private val momService = MomService(mockMomRepository, mockNidRepository, mockUserRepository, mockHashingService, mockTransactionService, MockAuthConfig.instance)

    private val jwtSecret = "secret"
    private val algorithm = Algorithm.HMAC256(jwtSecret)
    private val gson = Gson()

    init {

        val testDoctor = com.evelolvetech.data.models.Doctor(
            id = "doctor_test_id",
            name = "Test Doctor",
            email = "test@doctor.com",
            phone = "+1234567890",
            specialization = "CLINICAL_PSYCHOLOGIST",
            isAuthorized = true,
            authUid = "auth_test_doctor",
            nidId = "nid_test_id",
            nidRef = "nid_test_ref"
        )
        mockDoctorRepository.doctors["doctor_test_id"] = testDoctor


        val testMom = com.evelolvetech.data.models.Mom(
            id = "mom_test_id",
            email = "test@mom.com",
            fullName = "Test Mom",
            phone = "+1234567890",
            maritalStatus = "MARRIED",
            authUid = "auth_test_mom",
            photoUrl = "https://example.com/photo.jpg",
            numberOfSessions = 10,
            isAuthorized = true,
            nidId = "nid_test_mom",
            nidRef = "nid_test_mom_ref"
        )
        mockMomRepository.moms["mom_test_id"] = testMom
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
            categoryRoutes(categoryService)
            momRoutes(momService, gson)
            doctorRoutes(doctorService, gson)
            productRoutes(productService, momService)
            skuOfferRoutes(skuOfferService, momService)
            cartRoutes(cartService, momService, gson)
            adminRoutes(doctorService, categoryService, productService, skuOfferService, momService, gson)
        }
    }


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
            .withClaim("userId", "mom_test_id")
            .withClaim("userType", "MOM")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }

    private fun generateDoctorToken(): String {
        return JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "doctor_test_id")
            .withClaim("userType", "DOCTOR")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)
    }



    @Test
    fun `Mom token accessing admin doctor authorize endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val momToken = generateMomToken()
        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "Bearer $momToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Doctor token accessing admin doctor authorize endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val doctorToken = generateDoctorToken()
        val authorizeRequest = AuthorizationRequest(isAuthorized = true)
        
        val response = client.put("/api/admin/doctors/doctor_test_id/authorize") {
            header(HttpHeaders.Authorization, "Bearer $doctorToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(authorizeRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Mom token accessing admin doctor status endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val momToken = generateMomToken()
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Bearer $momToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Doctor token accessing admin doctor status endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val doctorToken = generateDoctorToken()
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Bearer $doctorToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Mom token accessing admin categories endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val momToken = generateMomToken()
        val createRequest = CreateCategoryRequest(
            name = "Test Category",
            slug = "test-category"
        )
        
        val response = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $momToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Doctor token accessing admin categories endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val doctorToken = generateDoctorToken()
        val createRequest = CreateCategoryRequest(
            name = "Test Category",
            slug = "test-category"
        )
        
        val response = client.post("/api/admin/categories") {
            header(HttpHeaders.Authorization, "Bearer $doctorToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Mom token accessing admin products endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val momToken = generateMomToken()
        val createRequest = CreateProductRequest(
            name = "Test Product",
            slug = "test-product",
            description = "Test Description",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 0
        )
        
        val response = client.post("/api/admin/products") {
            header(HttpHeaders.Authorization, "Bearer $momToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Doctor token accessing admin products endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val doctorToken = generateDoctorToken()
        val createRequest = CreateProductRequest(
            name = "Test Product",
            slug = "test-product",
            description = "Test Description",
            defaultSellerId = "seller_happy",
            categoryIds = listOf("cat_fitness"),
            minSessionsToPurchase = 0
        )
        
        val response = client.post("/api/admin/products") {
            header(HttpHeaders.Authorization, "Bearer $doctorToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Mom token accessing admin sku-offers endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val momToken = generateMomToken()
        val createRequest = CreateSkuOfferRequest(
            skuId = "sku_test",
            sellerId = "seller_test",
            listPrice = 100.0,
            salePrice = 90.0,
            currency = "USD"
        )
        
        val response = client.post("/api/admin/sku-offers") {
            header(HttpHeaders.Authorization, "Bearer $momToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Doctor token accessing admin sku-offers endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val doctorToken = generateDoctorToken()
        val createRequest = CreateSkuOfferRequest(
            skuId = "sku_test",
            sellerId = "seller_test",
            listPrice = 100.0,
            salePrice = 90.0,
            currency = "USD"
        )
        
        val response = client.post("/api/admin/sku-offers") {
            header(HttpHeaders.Authorization, "Bearer $doctorToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(createRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }



    @Test
    fun `Admin token accessing mom profile endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val adminToken = generateAdminToken()
        
        val response = client.get("/api/moms/profile") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Doctor token accessing mom profile endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val doctorToken = generateDoctorToken()
        
        val response = client.get("/api/moms/profile") {
            header(HttpHeaders.Authorization, "Bearer $doctorToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Admin token accessing mom products endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val adminToken = generateAdminToken()
        
        val response = client.get("/api/products") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Doctor token accessing mom products endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val doctorToken = generateDoctorToken()
        
        val response = client.get("/api/products") {
            header(HttpHeaders.Authorization, "Bearer $doctorToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Admin token accessing mom sku-offers endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val adminToken = generateAdminToken()
        
        val response = client.get("/api/sku-offers") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Doctor token accessing mom sku-offers endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val doctorToken = generateDoctorToken()
        
        val response = client.get("/api/sku-offers") {
            header(HttpHeaders.Authorization, "Bearer $doctorToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Admin token accessing mom cart endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val adminToken = generateAdminToken()
        
        val response = client.get("/api/cart") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Doctor token accessing mom cart endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val doctorToken = generateDoctorToken()
        
        val response = client.get("/api/cart") {
            header(HttpHeaders.Authorization, "Bearer $doctorToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }



    @Test
    fun `Admin token accessing doctor profile endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val adminToken = generateAdminToken()
        
        val response = client.get("/api/doctors/profile") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Mom token accessing doctor profile endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val momToken = generateMomToken()
        
        val response = client.get("/api/doctors/profile") {
            header(HttpHeaders.Authorization, "Bearer $momToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Admin token accessing doctor profile update endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val adminToken = generateAdminToken()
        val updateRequest = mapOf(
            "name" to "Updated Doctor Name",
            "phone" to "+1234567890"
        )
        
        val response = client.put("/api/doctors/profile") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Mom token accessing doctor profile update endpoint should return forbidden`() = testApplication {
        application { testModule() }

        val momToken = generateMomToken()
        val updateRequest = mapOf(
            "name" to "Updated Doctor Name",
            "phone" to "+1234567890"
        )
        
        val response = client.put("/api/doctors/profile") {
            header(HttpHeaders.Authorization, "Bearer $momToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(gson.toJson(updateRequest))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }



    @Test
    fun `Admin token accessing admin endpoints should succeed`() = testApplication {
        application { testModule() }

        val adminToken = generateAdminToken()
        
        val response = client.get("/api/admin/doctors/doctor_test_id/status") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `Mom token accessing mom endpoints should succeed`() = testApplication {
        application { testModule() }

        val momToken = generateMomToken()
        
        val response = client.get("/api/moms/profile") {
            header(HttpHeaders.Authorization, "Bearer $momToken")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `Doctor token accessing doctor endpoints should succeed`() = testApplication {
        application { testModule() }

        val doctorToken = generateDoctorToken()
        
        val response = client.get("/api/doctors/profile") {
            header(HttpHeaders.Authorization, "Bearer $doctorToken")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }



    @Test
    fun `Unauthorized mom token accessing mom e-commerce endpoints should return forbidden`() = testApplication {
        application { testModule() }


        val unauthorizedMomToken = JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "mom_unauthorized")
            .withClaim("userType", "MOM")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)


        val unauthorizedMom = com.evelolvetech.data.models.Mom(
            id = "mom_unauthorized",
            email = "unauthorized@mom.com",
            fullName = "Unauthorized Mom",
            phone = "+1234567890",
            maritalStatus = "SINGLE",
            authUid = "auth_unauthorized_mom",
            photoUrl = "https://example.com/photo.jpg",
            numberOfSessions = 3,
            isAuthorized = false,
            nidId = "nid_unauthorized",
            nidRef = "nid_unauthorized_ref"
        )
        mockMomRepository.moms["mom_unauthorized"] = unauthorizedMom
        
        val response = client.get("/api/products") {
            header(HttpHeaders.Authorization, "Bearer $unauthorizedMomToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `Unauthorized mom token accessing mom cart endpoint should return forbidden`() = testApplication {
        application { testModule() }


        val unauthorizedMomToken = JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "mom_unauthorized")
            .withClaim("userType", "MOM")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)


        val unauthorizedMom = com.evelolvetech.data.models.Mom(
            id = "mom_unauthorized",
            email = "unauthorized@mom.com",
            fullName = "Unauthorized Mom",
            phone = "+1234567890",
            maritalStatus = "SINGLE",
            authUid = "auth_unauthorized_mom",
            photoUrl = "https://example.com/photo.jpg",
            numberOfSessions = 3,
            isAuthorized = false,
            nidId = "nid_unauthorized",
            nidRef = "nid_unauthorized_ref"
        )
        mockMomRepository.moms["mom_unauthorized"] = unauthorizedMom
        
        val response = client.get("/api/cart") {
            header(HttpHeaders.Authorization, "Bearer $unauthorizedMomToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }



    @Test
    fun `Unauthorized doctor token accessing doctor endpoints should return forbidden`() = testApplication {
        application { testModule() }


        val unauthorizedDoctorToken = JWT.create()
            .withAudience("jwt-audience")
            .withIssuer("https://jwt-provider-domain/")
            .withClaim("userId", "doctor_unauthorized")
            .withClaim("userType", "DOCTOR")
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 3600000))
            .sign(algorithm)


        val unauthorizedDoctor = com.evelolvetech.data.models.Doctor(
            id = "doctor_unauthorized",
            name = "Unauthorized Doctor",
            email = "unauthorized@doctor.com",
            phone = "+1234567890",
            specialization = "PSYCHIATRIST",
            isAuthorized = false,
            authUid = "auth_unauthorized_doctor",
            nidId = "nid_unauthorized_doctor",
            nidRef = "nid_unauthorized_doctor_ref"
        )
        mockDoctorRepository.doctors["doctor_unauthorized"] = unauthorizedDoctor
        
        val response = client.get("/api/doctors/profile") {
            header(HttpHeaders.Authorization, "Bearer $unauthorizedDoctorToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}

package com.evelolvetech

import com.evelolvetech.routes.auth.authRoutes
import com.evelolvetech.routes.mom.ecommerce.categoryRoutes
import com.evelolvetech.routes.mom.momRoutes
import com.evelolvetech.routes.doctor.doctorRoutes
import com.evelolvetech.routes.mom.ecommerce.productRoutes
import com.evelolvetech.routes.mom.ecommerce.skuOfferRoutes
import com.evelolvetech.routes.mom.ecommerce.cartRoutes
import com.evelolvetech.routes.mom.ecommerce.orderRoutes
import com.evelolvetech.routes.mom.ecommerce.paymentRoutes
import com.evelolvetech.routes.mom.ecommerce.ratingRoutes
import com.evelolvetech.routes.admin.adminRoutes
import com.evelolvetech.routes.admin.adminInventoryRoutes
import com.evelolvetech.routes.session.momSessionRoutes
import com.evelolvetech.routes.session.doctorSessionRoutes
import com.evelolvetech.routes.persona.momPersonaRoutes
import com.evelolvetech.routes.persona.adminCircleRoutes
import com.evelolvetech.routes.streaming.doctorStreamingRoutes
import com.evelolvetech.routes.streaming.momStreamingRoutes
import com.evelolvetech.routes.admin.adminPortalRoutes
import com.evelolvetech.routes.healthRoutes
import com.evelolvetech.service.auth.AuthService
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.service.doctor.DoctorService
import com.evelolvetech.service.mom.ecommerce.CategoryService
import com.evelolvetech.service.mom.ecommerce.ProductService
import com.evelolvetech.service.mom.ecommerce.SkuOfferService
import com.evelolvetech.service.mom.ecommerce.CartService
import com.evelolvetech.service.mom.ecommerce.PaymentService
import com.evelolvetech.service.mom.ecommerce.ProductRatingService
import com.evelolvetech.service.mom.ecommerce.InventoryService
import com.evelolvetech.service.OrderService
import com.evelolvetech.service.session.GroupSessionService
import com.evelolvetech.service.persona.PersonaService
import com.evelolvetech.service.persona.ClusteringService
import com.evelolvetech.service.persona.CircleFormationService
import com.evelolvetech.service.streaming.StreamingService
import com.evelolvetech.service.admin.AdminPortalService
import com.evelolvetech.service.admin.VenueService
import com.evelolvetech.service.admin.AdminAnalyticsService
import com.evelolvetech.util.FileUploadUtil
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.io.File
import com.google.gson.Gson

fun Application.configureRouting() {
    install(RequestValidation) {
        validate<String> { bodyText ->
            if (!bodyText.startsWith("Hello"))
                ValidationResult.Invalid("Body text should start with 'Hello'")
            else ValidationResult.Valid
        }
    }

    FileUploadUtil.init()

    val authService: AuthService by inject()
    val momService: MomService by inject()
    val doctorService: DoctorService by inject()
    val categoryService: CategoryService by inject()
    val productService: ProductService by inject()
    val skuOfferService: SkuOfferService by inject()
    val cartService: CartService by inject()
    val orderService: OrderService by inject()
    val paymentService: PaymentService by inject()
    val ratingService: ProductRatingService by inject()
    val inventoryService: InventoryService by inject()
    val sessionService: GroupSessionService by inject()
    val personaService: PersonaService by inject()
    val clusteringService: ClusteringService by inject()
    val circleFormationService: CircleFormationService by inject()
    val streamingService: StreamingService by inject()
    val adminPortalService: AdminPortalService by inject()
    val venueService: VenueService by inject()
    val analyticsService: AdminAnalyticsService by inject()
    val gson: Gson by inject()

    val jwtIssuer = System.getenv("JWT_ISSUER") ?: environment.config.property("jwt.domain").getString()
    val jwtAudience = System.getenv("JWT_AUDIENCE") ?: environment.config.property("jwt.audience").getString()
    val jwtSecret = System.getenv("JWT_SECRET") ?: environment.config.property("jwt.secret").getString()

    routing {
        get("/") {
            call.respondText("Mom Project API - Welcome!")
        }

        staticFiles("/uploads", File("uploads")) {
            default("index.html")
        }

        healthRoutes()
        authRoutes(authService, momService, doctorService, jwtIssuer, jwtAudience, jwtSecret, gson)
        categoryRoutes(categoryService)
        momRoutes(momService, gson)
        doctorRoutes(doctorService, gson)
        productRoutes(productService, momService)
        skuOfferRoutes(skuOfferService, momService)
        cartRoutes(cartService, momService, gson)
        orderRoutes(orderService, momService)
        paymentRoutes(paymentService, momService)
        ratingRoutes(ratingService, momService)
        adminRoutes(doctorService, categoryService, productService, skuOfferService, momService, gson)
        adminInventoryRoutes(inventoryService)
        momSessionRoutes(sessionService, momService)
        doctorSessionRoutes(sessionService, doctorService)
        momPersonaRoutes(personaService, clusteringService, circleFormationService, momService)
        adminCircleRoutes(clusteringService, circleFormationService)
        doctorStreamingRoutes(streamingService, doctorService)
        momStreamingRoutes(streamingService, momService)
        adminPortalRoutes(adminPortalService, venueService, analyticsService)
    }
}

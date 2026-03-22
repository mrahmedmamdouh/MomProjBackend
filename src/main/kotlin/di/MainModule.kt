package com.evelolvetech.di

import com.evelolvetech.data.repository.api.auth.UserRepository
import com.evelolvetech.data.repository.api.auth.RefreshTokenRepository
import com.evelolvetech.data.repository.api.mom.MomRepository
import com.evelolvetech.data.repository.api.mom.NidRepository
import com.evelolvetech.data.repository.api.doctor.DoctorRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.CategoryRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.SellerRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.ProductRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.SkuRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.SkuOfferRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.CartRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.OrderRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.PaymentRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.ProductRatingRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.InventoryRepository
import com.evelolvetech.data.repository.api.session.GroupSessionRepository
import com.evelolvetech.data.repository.api.session.SessionBookingRepository
import com.evelolvetech.data.repository.impl.auth.UserRepositoryImpl
import com.evelolvetech.data.repository.impl.auth.RefreshTokenRepositoryImpl
import com.evelolvetech.data.repository.impl.mom.MomRepositoryImpl
import com.evelolvetech.data.repository.impl.mom.NidRepositoryImpl
import com.evelolvetech.data.repository.impl.doctor.DoctorRepositoryImpl
import com.evelolvetech.data.repository.impl.mom.ecommerce.CategoryRepositoryImpl
import com.evelolvetech.data.repository.impl.mom.ecommerce.SellerRepositoryImpl
import com.evelolvetech.data.repository.impl.mom.ecommerce.ProductRepositoryImpl
import com.evelolvetech.data.repository.impl.mom.ecommerce.SkuRepositoryImpl
import com.evelolvetech.data.repository.impl.mom.ecommerce.SkuOfferRepositoryImpl
import com.evelolvetech.data.repository.impl.mom.ecommerce.CartRepositoryImpl
import com.evelolvetech.data.repository.impl.mom.ecommerce.OrderRepositoryImpl
import com.evelolvetech.data.repository.impl.mom.ecommerce.PaymentRepositoryImpl
import com.evelolvetech.data.repository.impl.mom.ecommerce.ProductRatingRepositoryImpl
import com.evelolvetech.data.repository.impl.mom.ecommerce.InventoryRepositoryImpl
import com.evelolvetech.data.repository.impl.session.GroupSessionRepositoryImpl
import com.evelolvetech.data.repository.impl.session.SessionBookingRepositoryImpl
import com.evelolvetech.service.auth.AuthService
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.service.doctor.DoctorService
import com.evelolvetech.service.mom.ecommerce.CategoryService
import com.evelolvetech.service.mom.ecommerce.SellerService
import com.evelolvetech.service.mom.ecommerce.ProductService
import com.evelolvetech.service.mom.ecommerce.SkuOfferService
import com.evelolvetech.service.mom.ecommerce.CartService
import com.evelolvetech.service.OrderService
import com.evelolvetech.service.mom.ecommerce.PaymentService
import com.evelolvetech.service.mom.ecommerce.ProductRatingService
import com.evelolvetech.service.mom.ecommerce.InventoryService
import com.evelolvetech.service.TransactionServiceInterface
import com.evelolvetech.service.TransactionService
import com.evelolvetech.service.session.GroupSessionService
import com.evelolvetech.service.persona.PersonaService
import com.evelolvetech.service.persona.ClusteringService
import com.evelolvetech.service.persona.CircleFormationService
import com.evelolvetech.data.repository.api.circle.SupportCircleRepository
import com.evelolvetech.data.repository.impl.circle.SupportCircleRepositoryImpl
import com.evelolvetech.service.streaming.LiveKitConfig
import com.evelolvetech.service.streaming.StreamingService
import com.evelolvetech.service.admin.AdminPortalService
import com.evelolvetech.service.admin.VenueService
import com.evelolvetech.service.admin.AdminAnalyticsService
import com.evelolvetech.data.repository.api.admin.*
import com.evelolvetech.data.repository.impl.admin.*
import com.evelolvetech.util.Constants
import com.evelolvetech.util.HashingService
import com.evelolvetech.util.AuthConfig
import com.evelolvetech.util.SHA256HashingService
import io.ktor.server.application.*
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import org.koin.dsl.module
import org.litote.kmongo.KMongo
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness

val mainModule = module {

    single<MongoClient> {
        val mongoUri = System.getenv("MONGODB_URI") ?: "mongodb://localhost:27017/?replicaSet=rs0&maxPoolSize=20&w=majority"
        KMongo.createClient(mongoUri)
    }

    single<MongoDatabase> {
        get<MongoClient>().getDatabase(Constants.DATABASE_NAME)
    }

    single<HashingService> {
        SHA256HashingService()
    }

    single<AuthConfig> {
        AuthConfig()
    }

    single<Gson> {
        GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .create()
    }

    single<TransactionServiceInterface> {
        TransactionService(get(), get())
    }

    single<UserRepository> {
        UserRepositoryImpl(get())
    }

    single<CategoryRepository> {
        CategoryRepositoryImpl(get())
    }

    single<SellerRepository> {
        SellerRepositoryImpl(get())
    }

    single<MomRepository> {
        MomRepositoryImpl(get())
    }

    single<DoctorRepository> {
        DoctorRepositoryImpl(get())
    }

    single<ProductRepository> {
        ProductRepositoryImpl(get())
    }

    single<SkuRepository> {
        SkuRepositoryImpl(get())
    }

    single<SkuOfferRepository> {
        SkuOfferRepositoryImpl(get())
    }

    single<CartRepository> {
        CartRepositoryImpl(get())
    }

    single<OrderRepository> {
        OrderRepositoryImpl(get())
    }

    single<PaymentRepository> {
        PaymentRepositoryImpl(get())
    }

    single<ProductRatingRepository> {
        ProductRatingRepositoryImpl(get())
    }

    single<InventoryRepository> {
        InventoryRepositoryImpl(get())
    }

    single<NidRepository> {
        NidRepositoryImpl(get())
    }

    single<RefreshTokenRepository> {
        RefreshTokenRepositoryImpl(get())
    }

    single<CategoryService> {
        CategoryService(get<CategoryRepository>())
    }

    single<SellerService> {
        SellerService(get<SellerRepository>())
    }

    single<MomService> {
        MomService(get<MomRepository>(), get<NidRepository>(), get<UserRepository>(), get<HashingService>(), get<TransactionServiceInterface>(), get<AuthConfig>())
    }

    single<DoctorService> {
        DoctorService(get<DoctorRepository>(), get<NidRepository>(), get<UserRepository>(), get<HashingService>(), get<TransactionServiceInterface>())
    }

    single<AuthService> {
        AuthService(get<UserRepository>(), get<HashingService>(), get<RefreshTokenRepository>(), get<AuthConfig>())
    }

    single<ProductService> {
        ProductService(get<ProductRepository>(), get<SellerRepository>(), get<CategoryRepository>(), get<MomRepository>(), get<AuthConfig>())
    }

    single<SkuOfferService> {
        SkuOfferService(get<SkuOfferRepository>(), get<MomRepository>(), get<AuthConfig>())
    }

    single<CartService> {
        CartService(get<CartRepository>(), get<MomRepository>(), get<SkuOfferRepository>(), get<AuthConfig>())
    }

    single<OrderService> {
        OrderService(get<OrderRepository>(), get<CartRepository>(), get<SkuOfferRepository>(), get<SkuRepository>())
    }

    single<PaymentService> {
        PaymentService(get<PaymentRepository>(), get<OrderRepository>())
    }

    single<ProductRatingService> {
        ProductRatingService(get<ProductRatingRepository>(), get<ProductRepository>())
    }

    single<InventoryService> {
        InventoryService(get<InventoryRepository>())
    }

    single<GroupSessionRepository> {
        GroupSessionRepositoryImpl(get())
    }

    single<SessionBookingRepository> {
        SessionBookingRepositoryImpl(get())
    }

    single<GroupSessionService> {
        GroupSessionService(
            get<GroupSessionRepository>(),
            get<SessionBookingRepository>(),
            get<MomRepository>(),
            get<DoctorRepository>(),
            get<AuthConfig>()
        )
    }

    single<SupportCircleRepository> {
        SupportCircleRepositoryImpl(get())
    }

    single<PersonaService> {
        PersonaService(get<MomRepository>())
    }

    single<ClusteringService> {
        ClusteringService(get<MomRepository>())
    }

    single<CircleFormationService> {
        CircleFormationService(get<SupportCircleRepository>(), get<MomRepository>(), get<ClusteringService>())
    }

    single<LiveKitConfig> { LiveKitConfig() }

    single<StreamingService> {
        StreamingService(
            get<LiveKitConfig>(),
            get<GroupSessionRepository>(),
            get<SessionBookingRepository>(),
            get<DoctorRepository>(),
            get<MomRepository>()
        )
    }

    // Admin Repositories
    single<VenueRepository> { VenueRepositoryImpl(get()) }
    single<AuditLogRepository> { AuditLogRepositoryImpl(get()) }
    single<SystemConfigRepository> { SystemConfigRepositoryImpl(get()) }
    single<AdminAlertRepository> { AdminAlertRepositoryImpl(get()) }
    single<EmergencyResourceRepository> { EmergencyResourceRepositoryImpl(get()) }
    single<ContentReportRepository> { ContentReportRepositoryImpl(get()) }

    // Admin Services
    single<AdminPortalService> {
        AdminPortalService(
            get<AuditLogRepository>(), get<SystemConfigRepository>(),
            get<AdminAlertRepository>(), get<EmergencyResourceRepository>(),
            get<ContentReportRepository>(), get<DoctorRepository>(),
            get<MomRepository>(), get<GroupSessionRepository>(),
            get<SessionBookingRepository>()
        )
    }

    single<VenueService> {
        VenueService(get<VenueRepository>(), get<AuditLogRepository>())
    }

    single<AdminAnalyticsService> {
        AdminAnalyticsService(
            get<MomRepository>(), get<DoctorRepository>(),
            get<GroupSessionRepository>(), get<SessionBookingRepository>(),
            get<VenueRepository>()
        )
    }
}

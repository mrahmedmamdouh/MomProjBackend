package com.evelolvetech.scripts

import com.evelolvetech.data.repository.api.mom.ecommerce.CategoryRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.SellerRepository
import com.evelolvetech.data.repository.api.mom.MomRepository
import com.evelolvetech.data.repository.api.doctor.DoctorRepository
import com.evelolvetech.data.repository.api.mom.NidRepository
import com.evelolvetech.data.repository.api.auth.UserRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.ProductRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.SkuRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.SkuOfferRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.InventoryRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.CartRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.ProductRatingRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.OrderRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.PaymentRepository
import com.evelolvetech.data.repository.api.session.GroupSessionRepository
import com.evelolvetech.data.repository.api.session.SessionBookingRepository
import com.evelolvetech.data.repository.api.admin.EmergencyResourceRepository
import com.evelolvetech.service.admin.VenueService
import com.evelolvetech.di.mainModule
import com.evelolvetech.util.DataSeeder
import com.evelolvetech.util.HashingService
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin

fun main() = runBlocking {

    try {
        val koin = startKoin {
            modules(mainModule)
        }.koin

        val categoryRepository = koin.get<CategoryRepository>()
        val sellerRepository = koin.get<SellerRepository>()
        val momRepository = koin.get<MomRepository>()
        val doctorRepository = koin.get<DoctorRepository>()
        val nidRepository = koin.get<NidRepository>()
        val userRepository = koin.get<UserRepository>()
        val productRepository = koin.get<ProductRepository>()
        val skuRepository = koin.get<SkuRepository>()
        val skuOfferRepository = koin.get<SkuOfferRepository>()
        val inventoryRepository = koin.get<InventoryRepository>()
        val cartRepository = koin.get<CartRepository>()
        val productRatingRepository = koin.get<ProductRatingRepository>()
        val orderRepository = koin.get<OrderRepository>()
        val paymentRepository = koin.get<PaymentRepository>()
        val sessionRepository = koin.get<GroupSessionRepository>()
        val bookingRepository = koin.get<SessionBookingRepository>()
        val venueService = koin.get<VenueService>()
        val emergencyResourceRepo = koin.get<EmergencyResourceRepository>()
        val hashingService = koin.get<HashingService>()

        val seeder = DataSeeder(
            categoryRepository = categoryRepository,
            sellerRepository = sellerRepository,
            momRepository = momRepository,
            doctorRepository = doctorRepository,
            nidRepository = nidRepository,
            userRepository = userRepository,
            productRepository = productRepository,
            skuRepository = skuRepository,
            skuOfferRepository = skuOfferRepository,
            inventoryRepository = inventoryRepository,
            cartRepository = cartRepository,
            productRatingRepository = productRatingRepository,
            orderRepository = orderRepository,
            paymentRepository = paymentRepository,
            sessionRepository = sessionRepository,
            bookingRepository = bookingRepository,
            venueService = venueService,
            emergencyResourceRepo = emergencyResourceRepo,
            hashingService = hashingService
        )

        seeder.seedAll()


    } catch (e: Exception) {
        e.printStackTrace()
    }
}

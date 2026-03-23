package com.evelolvetech.util

import com.evelolvetech.data.repository.api.mom.MomRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.*
import com.evelolvetech.data.repository.api.doctor.DoctorRepository
import com.evelolvetech.data.repository.api.mom.NidRepository
import com.evelolvetech.data.repository.api.auth.UserRepository
import com.evelolvetech.data.repository.api.session.GroupSessionRepository
import com.evelolvetech.data.repository.api.session.SessionBookingRepository
import com.evelolvetech.data.repository.api.admin.EmergencyResourceRepository
import com.evelolvetech.service.admin.VenueService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StartupSeeder : KoinComponent {

    private val categoryRepo: CategoryRepository by inject()
    private val sellerRepo: SellerRepository by inject()
    private val momRepo: MomRepository by inject()
    private val doctorRepo: DoctorRepository by inject()
    private val nidRepo: NidRepository by inject()
    private val userRepo: UserRepository by inject()
    private val productRepo: ProductRepository by inject()
    private val skuRepo: SkuRepository by inject()
    private val skuOfferRepo: SkuOfferRepository by inject()
    private val inventoryRepo: InventoryRepository by inject()
    private val cartRepo: CartRepository by inject()
    private val ratingRepo: ProductRatingRepository by inject()
    private val orderRepo: OrderRepository by inject()
    private val paymentRepo: PaymentRepository by inject()
    private val sessionRepo: GroupSessionRepository by inject()
    private val bookingRepo: SessionBookingRepository by inject()
    private val venueService: VenueService by inject()
    private val emergencyRepo: EmergencyResourceRepository by inject()
    private val hashingService: HashingService by inject()

    suspend fun seedIfEmpty() {
        try {
            val existingUsers = userRepo.getUserByEmail("admin@momcare.com")
            if (existingUsers != null) {
                println("Database already seeded — skipping")
                return
            }

            println("Database empty — starting seed...")

            val seeder = DataSeeder(
                categoryRepository = categoryRepo,
                sellerRepository = sellerRepo,
                momRepository = momRepo,
                doctorRepository = doctorRepo,
                nidRepository = nidRepo,
                userRepository = userRepo,
                productRepository = productRepo,
                skuRepository = skuRepo,
                skuOfferRepository = skuOfferRepo,
                inventoryRepository = inventoryRepo,
                cartRepository = cartRepo,
                productRatingRepository = ratingRepo,
                orderRepository = orderRepo,
                paymentRepository = paymentRepo,
                sessionRepository = sessionRepo,
                bookingRepository = bookingRepo,
                venueService = venueService,
                emergencyResourceRepo = emergencyRepo,
                hashingService = hashingService
            )

            seeder.seedAll()
            println("Database seeded successfully!")
        } catch (e: Exception) {
            println("Seed error (non-fatal): ${e.message}")
        }
    }
}

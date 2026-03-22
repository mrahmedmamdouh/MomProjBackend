package com.evelolvetech.util

import com.evelolvetech.data.models.*
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
import com.evelolvetech.data.models.admin.EmergencyResource
import com.evelolvetech.service.admin.VenueService
import org.bson.types.ObjectId

class DataSeeder(
    private val categoryRepository: CategoryRepository,
    private val sellerRepository: SellerRepository,
    private val momRepository: MomRepository,
    private val doctorRepository: DoctorRepository,
    private val nidRepository: NidRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val skuRepository: SkuRepository,
    private val skuOfferRepository: SkuOfferRepository,
    private val inventoryRepository: InventoryRepository,
    private val cartRepository: CartRepository,
    private val productRatingRepository: ProductRatingRepository,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val sessionRepository: GroupSessionRepository,
    private val bookingRepository: SessionBookingRepository,
    private val venueService: VenueService,
    private val emergencyResourceRepo: EmergencyResourceRepository,
    private val hashingService: HashingService
) {

    companion object {
        const val SEL_ACME = "seller_acme"
        const val SEL_HAPPY = "seller_happy"

        const val CAT_MOTHER_CARE = "cat_mother_care"
        const val CAT_FITNESS = "cat_fitness"
        const val CAT_NUTRITION = "cat_nutrition"

        const val MOM_ALICE = "mom_alice"
        const val MOM_BETH = "mom_beth"

        const val PROD_ESSENTIALS = "prod_baby_essentials"
        const val PROD_PRENATAL = "prod_prenatal_yoga"
        const val PROD_ADVANCED = "prod_advanced_fitness"

        const val SKU_ESS_BLACK = "sku_essentials_black"
        const val SKU_PRENATAL_A = "sku_prenatal_batchA"
        const val SKU_ADVANCED_A = "sku_advanced_planA"

        const val DOC_SMITH = "doc_smith"
        const val DOC_JOHNSON = "doc_johnson"
        const val DOC_WILLIAMS = "doc_williams"
        const val DOC_BROWN = "doc_brown"
        const val DOC_DAVIS = "doc_davis"

        const val ADMIN_MAIN = "admin_main"
        const val ADMIN_SUPPORT = "admin_support"
    }

    suspend fun seedAll() {
        try {
            seedCategories()
            seedSellers()
            seedMomsAndNids()
            seedDoctorsAndNids()
            seedAdmins()
            seedProductsSkusOffersInventory()
            seedCartsAndRatings()
            seedOrdersAndPayments()
            seedGroupSessions()
            seedVenuesAndEmergencyResources()
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun seedCategories() {
        val categories = listOf(
            Category(id = CAT_MOTHER_CARE, name = "Mother Care", slug = "mother-care"),
            Category(id = CAT_FITNESS, name = "Fitness", slug = "fitness"),
            Category(id = CAT_NUTRITION, name = "Nutrition", slug = "nutrition")
        )

        categories.forEach { category ->
            if (categoryRepository.getCategoryById(category.id) == null) {
                categoryRepository.createCategory(category)
            } else {
            }
        }
    }

    private suspend fun seedSellers() {
        val sellers = listOf(
            Seller(id = SEL_ACME, name = "Acme Health", status = "ACTIVE"),
            Seller(id = SEL_HAPPY, name = "Happy Moms Co.", status = "ACTIVE")
        )

        sellers.forEach { seller ->
            if (sellerRepository.getSellerById(seller.id) == null) {
                sellerRepository.createSeller(seller)
            } else {
            }
        }
    }

    private suspend fun seedMomsAndNids() {
        val momsData = listOf(
            Triple(
                "alice@example.com",
                "password123",
                Mom(
                    id = MOM_ALICE,
                    authUid = ObjectId().toString(),
                    fullName = "Alice Mom",
                    email = "alice@example.com",
                    phone = "+1-555-0001",
                    maritalStatus = "MARRIED",
                    photoUrl = "https://example.com/alice_photo.jpg",
                    numberOfSessions = 3,
                    nidId = "nid_alice",
                    nidRef = "/nids/nid_alice"
                )
            ),
            Triple(
                "beth@example.com",
                "password123",
                Mom(
                    id = MOM_BETH,
                    authUid = ObjectId().toString(),
                    fullName = "Beth Mom",
                    email = "beth@example.com",
                    phone = "+1-555-0002",
                    maritalStatus = "SINGLE",
                    photoUrl = "https://example.com/beth_photo.jpg",
                    numberOfSessions = 8,
                    isAuthorized = true,
                    nidId = "nid_beth",
                    nidRef = "/nids/nid_beth"
                )
            )
        )

        momsData.forEach { (email, password, mom) ->
            if (momRepository.getMomByEmail(email) == null) {
                momRepository.createMom(mom)

                val nid = Nid(
                    id = mom.nidId,
                    imageFront = "https://example.com/${mom.nidId}_front.jpg",
                    imageBack = "https://example.com/${mom.nidId}_back.jpg"
                )
                nidRepository.createNid(nid)
                val authMapping = MomAuth(uid = mom.authUid, momId = mom.id)
                momRepository.createMomAuth(authMapping)
                val hashedPassword = hashingService.generateSaltedHash(password)
                val user = User(
                    email = email,
                    password = "${hashedPassword.hash}:${hashedPassword.salt}",
                    userType = UserType.MOM,
                    momId = mom.id
                )
                userRepository.createUserEntry(user)

            } else {
            }
        }
    }

    private suspend fun seedDoctorsAndNids() {
        val doctorsData = listOf(
            Triple(
                "dr.smith@example.com",
                "password123",
                Doctor(
                    id = DOC_SMITH,
                    authUid = ObjectId().toString(),
                    name = "Dr. Sarah Smith",
                    email = "dr.smith@example.com",
                    phone = "+1-555-0101",
                    specialization = "PSYCHIATRIST",
                    isAuthorized = true,
                    photo = "",
                    nidId = "nid_dr_smith",
                    nidRef = "/nids/nid_dr_smith"
                )
            ),
            Triple(
                "dr.johnson@example.com",
                "password123",
                Doctor(
                    id = DOC_JOHNSON,
                    authUid = ObjectId().toString(),
                    name = "Dr. Michael Johnson",
                    email = "dr.johnson@example.com",
                    phone = "+1-555-0102",
                    specialization = "CLINICAL_PSYCHOLOGIST",
                    isAuthorized = true,
                    photo = "",
                    nidId = "nid_dr_johnson",
                    nidRef = "/nids/nid_dr_johnson"
                )
            ),
            Triple(
                "dr.williams@example.com",
                "password123",
                Doctor(
                    id = DOC_WILLIAMS,
                    authUid = ObjectId().toString(),
                    name = "Dr. Emily Williams",
                    email = "dr.williams@example.com",
                    phone = "+1-555-0103",
                    specialization = "PERINATAL_MENTAL_HEALTH",
                    isAuthorized = true,
                    photo = "",
                    nidId = "nid_dr_williams",
                    nidRef = "/nids/nid_dr_williams"
                )
            ),
            Triple(
                "dr.brown@example.com",
                "password123",
                Doctor(
                    id = DOC_BROWN,
                    authUid = ObjectId().toString(),
                    name = "Dr. David Brown",
                    email = "dr.brown@example.com",
                    phone = "+1-555-0104",
                    specialization = "FAMILY_THERAPIST",
                    isAuthorized = true,
                    photo = "",
                    nidId = "nid_dr_brown",
                    nidRef = "/nids/nid_dr_brown"
                )
            ),
            Triple(
                "dr.davis@example.com",
                "password123",
                Doctor(
                    id = DOC_DAVIS,
                    authUid = ObjectId().toString(),
                    name = "Dr. Jennifer Davis",
                    email = "dr.davis@example.com",
                    phone = "+1-555-0105",
                    specialization = "GROUP_THERAPIST",
                    isAuthorized = true,
                    photo = "",
                    nidId = "nid_dr_davis",
                    nidRef = "/nids/nid_dr_davis"
                )
            )
        )

        doctorsData.forEach { (email, password, doctor) ->
            if (doctorRepository.getDoctorByEmail(email) == null) {
                doctorRepository.createDoctor(doctor)

                val nid = Nid(
                    id = doctor.nidId,
                    imageFront = "",
                    imageBack = "",
                    createdAt = System.currentTimeMillis()
                )
                nidRepository.createNid(nid)
                val authMapping = DoctorAuth(uid = doctor.authUid, doctorId = doctor.id)
                doctorRepository.createDoctorAuth(authMapping)
                val hashedPassword = hashingService.generateSaltedHash(password)
                val user = User(
                    email = email,
                    password = "${hashedPassword.hash}:${hashedPassword.salt}",
                    userType = UserType.DOCTOR,
                    doctorId = doctor.id
                )
                userRepository.createUserEntry(user)

            } else {
            }
        }
    }

    private suspend fun seedAdmins() {
        val adminsData = listOf(
            Pair(
                "admin@momcare.com",
                "admin123"
            ),
            Pair(
                "support@momcare.com", 
                "support123"
            )
        )

        adminsData.forEachIndexed { index, (email, password) ->
            if (userRepository.getUserByEmail(email) == null) {
                val hashedPassword = hashingService.generateSaltedHash(password)
                val adminId = if (index == 0) ADMIN_MAIN else ADMIN_SUPPORT
                val user = User(
                    id = adminId,
                    email = email,
                    password = "${hashedPassword.hash}:${hashedPassword.salt}",
                    userType = UserType.ADMIN
                )
                userRepository.createUserEntry(user)
                println("✅ Created admin user: $email")
            } else {
                println("ℹ️ Admin user already exists: $email")
            }
        }
    }

    private suspend fun seedProductsSkusOffersInventory() {
        val products = listOf(
            Product(
                id = PROD_ESSENTIALS,
                name = "Baby Essentials Pack",
                slug = "baby-essentials-pack",
                description = "Starter essentials for new moms.",
                status = "ACTIVE",
                defaultSellerId = SEL_ACME,
                defaultSellerRef = "/sellers/$SEL_ACME",
                categoryIds = listOf(CAT_MOTHER_CARE),
                minSessionsToPurchase = 0
            ),
            Product(
                id = PROD_PRENATAL,
                name = "Prenatal Yoga Class",
                slug = "prenatal-yoga",
                description = "Guided prenatal yoga session.",
                status = "ACTIVE",
                defaultSellerId = SEL_HAPPY,
                defaultSellerRef = "/sellers/$SEL_HAPPY",
                categoryIds = listOf(CAT_FITNESS),
                minSessionsToPurchase = 5
            ),
            Product(
                id = PROD_ADVANCED,
                name = "Advanced Fitness Plan",
                slug = "advanced-fitness-plan",
                description = "Advanced fitness program.",
                status = "ACTIVE",
                defaultSellerId = SEL_HAPPY,
                defaultSellerRef = "/sellers/$SEL_HAPPY",
                categoryIds = listOf(CAT_FITNESS),
                minSessionsToPurchase = 10
            )
        )

        products.forEach { product ->
            if (productRepository.getProductById(product.id) == null) {
                productRepository.createProduct(product)
            } else {
            }
        }
        val skus = listOf(
            Sku(
                id = SKU_ESS_BLACK,
                productId = PROD_ESSENTIALS,
                productRef = "/products/$PROD_ESSENTIALS",
                skuCode = "ESS-BLK",
                title = "Black",
                taxClass = "STANDARD",
                isActive = true
            ),
            Sku(
                id = SKU_PRENATAL_A,
                productId = PROD_PRENATAL,
                productRef = "/products/$PROD_PRENATAL",
                skuCode = "PRN-A",
                title = "Batch A",
                taxClass = "SERVICE",
                isActive = true
            ),
            Sku(
                id = SKU_ADVANCED_A,
                productId = PROD_ADVANCED,
                productRef = "/products/$PROD_ADVANCED",
                skuCode = "ADV-A",
                title = "Plan A",
                taxClass = "SERVICE",
                isActive = true
            )
        )

        skus.forEach { sku ->
            if (skuRepository.getSkuById(sku.id) == null) {
                skuRepository.createSku(sku)
            } else {
            }
        }
        val skuOffers = listOf(
            SkuOffer(
                id = "offer_ess_acme",
                skuId = SKU_ESS_BLACK,
                skuRef = "/skus/$SKU_ESS_BLACK",
                sellerId = SEL_ACME,
                sellerRef = "/sellers/$SEL_ACME",
                listPrice = 120.0,
                salePrice = 99.0,
                currency = "USD",
                isActive = true,
                activeFrom = System.currentTimeMillis()
            ),
            SkuOffer(
                id = "offer_prn_happy",
                skuId = SKU_PRENATAL_A,
                skuRef = "/skus/$SKU_PRENATAL_A",
                sellerId = SEL_HAPPY,
                sellerRef = "/sellers/$SEL_HAPPY",
                listPrice = 30.0,
                salePrice = 25.0,
                currency = "USD",
                isActive = true,
                activeFrom = System.currentTimeMillis()
            ),
            SkuOffer(
                id = "offer_adv_happy",
                skuId = SKU_ADVANCED_A,
                skuRef = "/skus/$SKU_ADVANCED_A",
                sellerId = SEL_HAPPY,
                sellerRef = "/sellers/$SEL_HAPPY",
                listPrice = 99.0,
                salePrice = 89.0,
                currency = "USD",
                isActive = true,
                activeFrom = System.currentTimeMillis()
            )
        )

        skuOffers.forEach { offer ->
            if (skuOfferRepository.getSkuOfferById(offer.id) == null) {
                skuOfferRepository.createSkuOffer(offer)
            } else {
            }
        }
        val inventoryItems = listOf(
            Inventory(skuId = SKU_ESS_BLACK, onHand = 100, reserved = 0),
            Inventory(skuId = SKU_PRENATAL_A, onHand = 25, reserved = 0),
            Inventory(skuId = SKU_ADVANCED_A, onHand = 10, reserved = 0)
        )

        inventoryItems.forEach { inventory ->
            if (inventoryRepository.getInventoryBySkuId(inventory.skuId) == null) {
                inventoryRepository.createInventory(inventory)
            } else {
            }
        }
    }

    private suspend fun seedCartsAndRatings() {
        val bethCart = Cart(momId = MOM_BETH)
        val bethCartItem = CartItem(
            skuId = SKU_PRENATAL_A,
            qty = 2,
            priceSnapshot = 25.0,
            offerId = "offer_prn_happy",
            skuRef = "/skus/$SKU_PRENATAL_A",
            offerRef = "/skuOffers/offer_prn_happy"
        )

        if (cartRepository.getCartByMomId(MOM_BETH) == null) {
            cartRepository.createCart(bethCart)
            cartRepository.addItemToCart(MOM_BETH, bethCartItem)
        } else {
        }
        val aliceCart = Cart(momId = MOM_ALICE)
        val aliceCartItem = CartItem(
            skuId = SKU_ESS_BLACK,
            qty = 1,
            priceSnapshot = 99.0,
            offerId = "offer_ess_acme",
            skuRef = "/skus/$SKU_ESS_BLACK",
            offerRef = "/skuOffers/offer_ess_acme"
        )

        if (cartRepository.getCartByMomId(MOM_ALICE) == null) {
            cartRepository.createCart(aliceCart)
            cartRepository.addItemToCart(MOM_ALICE, aliceCartItem)
        } else {
        }
        val aliceRating = ProductRating(
            id = "${PROD_ESSENTIALS}_${MOM_ALICE}",
            productId = PROD_ESSENTIALS,
            uid = MOM_ALICE,
            rating = 5,
            title = "Great starter",
            comment = "Loved it!"
        )

        if (productRatingRepository.getRatingById(aliceRating.id) == null) {
            productRatingRepository.createRating(aliceRating)
        } else {
        }
    }

    private suspend fun seedOrdersAndPayments() {
        val orderBeth = Order(
            id = "order_beth_prenatal",
            orderNo = "ORD-BETH-001",
            momId = MOM_BETH,
            momRef = "/moms/$MOM_BETH",
            uid = MOM_BETH,
            status = "PENDING",
            currency = "USD",
            subtotal = 50.0,
            discountTotal = 0.0,
            taxTotal = 0.0,
            shippingTotal = 0.0,
            grandTotal = 50.0,
            items = listOf(
                OrderItem(
                    skuId = SKU_PRENATAL_A,
                    skuRef = "/skus/$SKU_PRENATAL_A",
                    productId = PROD_PRENATAL,
                    productRef = "/products/$PROD_PRENATAL",
                    sellerId = SEL_HAPPY,
                    sellerRef = "/sellers/$SEL_HAPPY",
                    qty = 2,
                    unitPrice = 25.0,
                    lineTotal = 50.0,
                    productName = "Prenatal Yoga Class"
                )
            )
        )

        if (orderRepository.getOrderById(orderBeth.id) == null) {
            orderRepository.createOrder(orderBeth)
            val paymentBeth = Payment(
                id = "pay_beth_order001",
                orderId = orderBeth.id,
                orderRef = "/orders/${orderBeth.id}",
                uid = MOM_BETH,
                provider = "stripe",
                method = "card",
                amount = 50.0,
                currency = "USD",
                status = "AUTHORIZED",
                transactionRef = "txn_beth_001"
            )

            paymentRepository.createPayment(paymentBeth)
        } else {
        }
        val orderAlice = Order(
            id = "order_alice_essentials",
            orderNo = "ORD-ALICE-001",
            momId = MOM_ALICE,
            momRef = "/moms/$MOM_ALICE",
            uid = MOM_ALICE,
            status = "PENDING",
            currency = "USD",
            subtotal = 99.0,
            discountTotal = 0.0,
            taxTotal = 0.0,
            shippingTotal = 0.0,
            grandTotal = 99.0,
            items = listOf(
                OrderItem(
                    skuId = SKU_ESS_BLACK,
                    skuRef = "/skus/$SKU_ESS_BLACK",
                    productId = PROD_ESSENTIALS,
                    productRef = "/products/$PROD_ESSENTIALS",
                    sellerId = SEL_ACME,
                    sellerRef = "/sellers/$SEL_ACME",
                    qty = 1,
                    unitPrice = 99.0,
                    lineTotal = 99.0,
                    productName = "Baby Essentials Pack"
                )
            )
        )

        if (orderRepository.getOrderById(orderAlice.id) == null) {
            orderRepository.createOrder(orderAlice)
            val paymentAlice = Payment(
                id = "pay_alice_order001",
                orderId = orderAlice.id,
                orderRef = "/orders/${orderAlice.id}",
                uid = MOM_ALICE,
                provider = "paypal",
                method = "wallet",
                amount = 99.0,
                currency = "USD",
                status = "AUTHORIZED",
                transactionRef = "txn_alice_001"
            )

            paymentRepository.createPayment(paymentAlice)
        } else {
        }
    }

    private suspend fun seedGroupSessions() {
        val nextWeek = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
        val nextTwoWeeks = System.currentTimeMillis() + (14 * 24 * 60 * 60 * 1000L)
        val nextThreeWeeks = System.currentTimeMillis() + (21 * 24 * 60 * 60 * 1000L)

        val onlineSession = GroupSession(
            id = "session_online_001",
            doctorId = DOC_SMITH,
            doctorRef = "/doctors/$DOC_SMITH",
            doctorName = "Dr. Sarah Smith",
            title = "Coping with Anxiety During Pregnancy",
            description = "A live group therapy call for mothers experiencing anxiety during pregnancy. We'll explore breathing techniques, cognitive reframing, and peer support strategies together in real-time.",
            sessionType = "ONLINE_VIDEO",
            maxCapacity = 7,
            currentBookings = 0,
            scheduledAt = nextWeek,
            durationMinutes = 60,
            status = "OPEN_FOR_BOOKING",
            meetingUrl = "https://meet.momcare.com/session-001",
            targetInterests = listOf("mental_health", "anxiety", "wellness"),
            targetPregnancyStages = listOf("FIRST_TRIMESTER", "SECOND_TRIMESTER", "THIRD_TRIMESTER"),
            language = "ar"
        )

        val physicalSession = GroupSession(
            id = "session_physical_001",
            doctorId = DOC_WILLIAMS,
            doctorRef = "/doctors/$DOC_WILLIAMS",
            doctorName = "Dr. Emily Williams",
            title = "Postpartum Depression Support Circle",
            description = "A warm, safe meetup for new mothers at a family-friendly café in Cairo. Share your experiences and learn coping strategies in a relaxed atmosphere with tea and light refreshments.",
            sessionType = "PHYSICAL_MEETING",
            maxCapacity = 6,
            currentBookings = 0,
            scheduledAt = nextTwoWeeks,
            durationMinutes = 90,
            status = "OPEN_FOR_BOOKING",
            venue = SessionVenue(
                name = "Café Corniche — Family Area",
                address = "26th of July Corridor, Zamalek",
                city = "Cairo",
                state = "Cairo Governorate",
                country = "EG",
                latitude = 30.0594,
                longitude = 31.2234,
                venueType = "CAFE",
                notes = "Reserved private corner for the group. Baby-friendly, stroller access, free parking nearby."
            ),
            targetInterests = listOf("postpartum", "depression", "new_mother"),
            targetPregnancyStages = listOf("POSTPARTUM"),
            language = "ar"
        )

        val traumaSession = GroupSession(
            id = "session_online_002",
            doctorId = DOC_BROWN,
            doctorRef = "/doctors/$DOC_BROWN",
            doctorName = "Dr. David Brown",
            title = "Healing Together: Single Mothers Support",
            description = "A confidential live group therapy call for single mothers navigating the challenges of solo parenting. We meet in real-time to build resilience, share strategies, and support each other.",
            sessionType = "ONLINE_VIDEO",
            maxCapacity = 5,
            currentBookings = 0,
            scheduledAt = nextThreeWeeks,
            durationMinutes = 75,
            status = "OPEN_FOR_BOOKING",
            meetingUrl = "https://meet.momcare.com/session-002",
            targetInterests = listOf("single_parenting", "resilience", "community"),
            targetPregnancyStages = listOf("SECOND_TRIMESTER", "THIRD_TRIMESTER", "POSTPARTUM"),
            language = "ar"
        )

        val parkSession = GroupSession(
            id = "session_physical_002",
            doctorId = DOC_SMITH,
            doctorRef = "/doctors/$DOC_SMITH",
            doctorName = "Dr. Sarah Smith",
            title = "Mindful Mothers: Yoga & Talk in the Park",
            description = "A gentle yoga and group conversation session at Al-Azhar Park. We'll start with 30 minutes of pregnancy-safe yoga, then sit together for an open discussion about what you're going through.",
            sessionType = "PHYSICAL_MEETING",
            maxCapacity = 7,
            currentBookings = 0,
            scheduledAt = nextWeek + (3 * 24 * 60 * 60 * 1000L),
            durationMinutes = 90,
            status = "OPEN_FOR_BOOKING",
            venue = SessionVenue(
                name = "Al-Azhar Park — Lakeside Garden",
                address = "Al-Azhar Park, Salah Salem St, El-Darb El-Ahmar",
                city = "Cairo",
                state = "Cairo Governorate",
                country = "EG",
                latitude = 30.0394,
                longitude = 31.2661,
                venueType = "PARK",
                notes = "Bring a yoga mat or blanket. Shaded area near the lake. Entry ticket included."
            ),
            targetInterests = listOf("yoga", "meditation", "wellness", "exercise"),
            targetPregnancyStages = listOf("FIRST_TRIMESTER", "SECOND_TRIMESTER"),
            language = "ar"
        )

        val clubSession = GroupSession(
            id = "session_physical_003",
            doctorId = DOC_WILLIAMS,
            doctorRef = "/doctors/$DOC_WILLIAMS",
            doctorName = "Dr. Emily Williams",
            title = "New Moms Brunch & Talk",
            description = "A relaxed brunch meetup at a private dining room. Share your journey with other new mothers over food and conversation. Babies welcome — highchairs and a play corner are available.",
            sessionType = "PHYSICAL_MEETING",
            maxCapacity = 6,
            currentBookings = 0,
            scheduledAt = nextTwoWeeks + (2 * 24 * 60 * 60 * 1000L),
            durationMinutes = 120,
            status = "OPEN_FOR_BOOKING",
            venue = SessionVenue(
                name = "The Garden Restaurant — Private Room",
                address = "8 Ahmed Nessim St, Giza",
                city = "Giza",
                state = "Giza Governorate",
                country = "EG",
                latitude = 30.0131,
                longitude = 31.2089,
                venueType = "RESTAURANT",
                notes = "Private dining room reserved. Baby-friendly, highchairs available. Brunch included in session."
            ),
            targetInterests = listOf("nutrition", "new_mother", "community"),
            targetPregnancyStages = listOf("POSTPARTUM"),
            language = "ar"
        )

        val sessions = listOf(onlineSession, physicalSession, traumaSession, parkSession, clubSession)

        sessions.forEach { session ->
            if (sessionRepository.getSessionById(session.id) == null) {
                sessionRepository.createSession(session)
            }
        }

        if (bookingRepository.getBookingBySessionAndMom(onlineSession.id, MOM_BETH) == null) {
            val bethBooking = SessionBooking(
                id = "booking_beth_session001",
                sessionId = onlineSession.id,
                sessionRef = "/sessions/${onlineSession.id}",
                momId = MOM_BETH,
                momRef = "/moms/$MOM_BETH",
                momName = "Beth Mom",
                status = "CONFIRMED"
            )
            bookingRepository.createBooking(bethBooking)
            sessionRepository.incrementBookingCount(onlineSession.id)
        }
    }

    private suspend fun seedVenuesAndEmergencyResources() {
        venueService.seedDefaultVenues()

        val resources = listOf(
            EmergencyResource(id = "er_mental_health", name = "Mental Health Hotline", nameAr = "خط نجدة الصحة النفسية", phone = "08008880700", description = "Free 24/7 mental health support line", descriptionAr = "خط دعم الصحة النفسية المجاني على مدار الساعة", country = "EG", category = "MENTAL_HEALTH", displayOrder = 1),
            EmergencyResource(id = "er_child_helpline", name = "Child Helpline", nameAr = "خط نجدة الطفل", phone = "16000", description = "National child protection and family support", descriptionAr = "الخط القومي لحماية الطفل ودعم الأسرة", country = "EG", category = "CHILD_PROTECTION", displayOrder = 2),
            EmergencyResource(id = "er_domestic_violence", name = "Violence Against Women", nameAr = "العنف ضد المرأة", phone = "15115", description = "National hotline for women experiencing violence", descriptionAr = "الخط الساخن للنساء اللواتي يتعرضن للعنف", country = "EG", category = "DOMESTIC_VIOLENCE", displayOrder = 3),
            EmergencyResource(id = "er_ambulance", name = "Ambulance", nameAr = "الإسعاف", phone = "123", description = "Emergency medical services", descriptionAr = "خدمات الطوارئ الطبية", country = "EG", category = "EMERGENCY", displayOrder = 4),
            EmergencyResource(id = "er_police", name = "Police", nameAr = "الشرطة", phone = "122", description = "Emergency police services", descriptionAr = "خدمات الشرطة الطارئة", country = "EG", category = "EMERGENCY", displayOrder = 5)
        )

        resources.forEach { resource ->
            if (emergencyResourceRepo.getById(resource.id) == null) {
                emergencyResourceRepo.create(resource)
            }
        }
    }
}

package com.evelolvetech.service.admin

import com.evelolvetech.data.models.admin.AuditLog
import com.evelolvetech.data.models.venue.ApprovedVenue
import com.evelolvetech.data.models.venue.VenueReview
import com.evelolvetech.data.models.venue.VenueStatus
import com.evelolvetech.data.repository.api.admin.AuditLogRepository
import com.evelolvetech.data.repository.api.admin.VenueRepository
import com.evelolvetech.data.responses.BasicApiResponse

class VenueService(
    private val venueRepo: VenueRepository,
    private val auditLogRepo: AuditLogRepository
) {

    suspend fun createVenue(venue: ApprovedVenue, adminId: String): BasicApiResponse<ApprovedVenue> {
        if (venue.name.isBlank() || venue.city.isBlank() || venue.venueType.isBlank()) {
            return BasicApiResponse(success = false, message = "Name, city, and venue type are required")
        }
        val created = venue.copy(addedBy = adminId, status = VenueStatus.ACTIVE.name)
        venueRepo.createVenue(created)
        auditLogRepo.log(AuditLog(adminId = adminId, adminEmail = "", action = "CREATE_VENUE", targetType = "VENUE", targetId = created.id, details = "Created venue: ${created.name} in ${created.city}"))
        return BasicApiResponse(success = true, data = created, message = "Venue created")
    }

    suspend fun updateVenue(id: String, venue: ApprovedVenue, adminId: String): BasicApiResponse<ApprovedVenue> {
        val existing = venueRepo.getVenueById(id) ?: return BasicApiResponse(success = false, message = "Venue not found")
        val updated = venue.copy(id = id, updatedAt = System.currentTimeMillis())
        venueRepo.updateVenue(id, updated)
        auditLogRepo.log(AuditLog(adminId = adminId, adminEmail = "", action = "UPDATE_VENUE", targetType = "VENUE", targetId = id, details = "Updated: ${existing.name}"))
        return BasicApiResponse(success = true, data = updated, message = "Venue updated")
    }

    suspend fun activateVenue(id: String, adminId: String): BasicApiResponse<Unit> {
        venueRepo.updateVenueStatus(id, VenueStatus.ACTIVE.name)
        auditLogRepo.log(AuditLog(adminId = adminId, adminEmail = "", action = "ACTIVATE_VENUE", targetType = "VENUE", targetId = id))
        return BasicApiResponse(success = true, message = "Venue activated")
    }

    suspend fun deactivateVenue(id: String, adminId: String): BasicApiResponse<Unit> {
        venueRepo.updateVenueStatus(id, VenueStatus.INACTIVE.name)
        auditLogRepo.log(AuditLog(adminId = adminId, adminEmail = "", action = "DEACTIVATE_VENUE", targetType = "VENUE", targetId = id))
        return BasicApiResponse(success = true, message = "Venue deactivated")
    }

    suspend fun getVenueById(id: String) = BasicApiResponse(success = true, data = venueRepo.getVenueById(id))
    suspend fun getActiveVenues(page: Int = 0) = BasicApiResponse(success = true, data = venueRepo.getActiveVenues(page))
    suspend fun getAllVenues(page: Int = 0) = BasicApiResponse(success = true, data = venueRepo.getAllVenues(page))
    suspend fun getVenuesByCity(city: String) = BasicApiResponse(success = true, data = venueRepo.getVenuesByCity(city))
    suspend fun getVenuesByType(venueType: String) = BasicApiResponse(success = true, data = venueRepo.getVenuesByType(venueType))
    suspend fun getVenuesByCityAndType(city: String, venueType: String) = BasicApiResponse(success = true, data = venueRepo.getVenuesByCityAndType(city, venueType))
    suspend fun searchVenues(query: String) = BasicApiResponse(success = true, data = venueRepo.searchVenues(query))

    suspend fun addVenueReview(review: VenueReview): BasicApiResponse<Unit> {
        val venue = venueRepo.getVenueById(review.venueId) ?: return BasicApiResponse(success = false, message = "Venue not found")
        venueRepo.createReview(review)
        val reviews = venueRepo.getReviewsByVenueId(review.venueId)
        val avg = reviews.map { it.rating }.average()
        venueRepo.updateVenue(venue.id, venue.copy(averageRating = avg, ratingCount = reviews.size))
        return BasicApiResponse(success = true, message = "Review added")
    }

    suspend fun getVenueReviews(venueId: String) = BasicApiResponse(success = true, data = venueRepo.getReviewsByVenueId(venueId))

    suspend fun getVenueStats(): BasicApiResponse<Map<String, Any>> {
        val stats = mapOf(
            "totalActive" to venueRepo.countByStatus(VenueStatus.ACTIVE.name),
            "totalInactive" to venueRepo.countByStatus(VenueStatus.INACTIVE.name),
            "totalUnderReview" to venueRepo.countByStatus(VenueStatus.UNDER_REVIEW.name),
            "byCityCairo" to venueRepo.countByCity("Cairo"),
            "byCityGiza" to venueRepo.countByCity("Giza"),
            "byCityAlexandria" to venueRepo.countByCity("Alexandria")
        )
        return BasicApiResponse(success = true, data = stats)
    }

    suspend fun seedDefaultVenues() {
        if (venueRepo.getActiveVenues(0, 1).isNotEmpty()) return

        val venues = listOf(
            ApprovedVenue(name = "Café Corniche — Family Area", nameAr = "كافيه كورنيش — منطقة العائلات", description = "Waterfront café with private reserved area for group sessions. Quiet atmosphere, great for conversation.", address = "26th of July Corridor, Zamalek", city = "Cairo", country = "EG", latitude = 30.0594, longitude = 31.2234, venueType = "CAFE", amenities = listOf("wifi", "private_area", "baby_friendly"), babyFriendly = true, strollerAccess = true, hasParking = true, hasPrivateRoom = true, hasWifi = true, maxGroupSize = 8, operatingHours = "8:00 AM - 11:00 PM", tags = listOf("zamalek", "waterfront", "quiet", "family")),
            ApprovedVenue(name = "Al-Azhar Park — Lakeside Garden", nameAr = "حديقة الأزهر — الحديقة بجانب البحيرة", description = "Beautiful park with shaded lakeside area. Perfect for yoga and outdoor group sessions. Entry ticket included.", address = "Al-Azhar Park, Salah Salem St, El-Darb El-Ahmar", city = "Cairo", country = "EG", latitude = 30.0394, longitude = 31.2661, venueType = "PARK", amenities = listOf("outdoor", "shaded", "scenic"), babyFriendly = true, strollerAccess = true, hasParking = true, maxGroupSize = 10, operatingHours = "9:00 AM - 10:00 PM", tags = listOf("park", "outdoor", "yoga", "nature")),
            ApprovedVenue(name = "The Garden Restaurant — Private Room", nameAr = "مطعم الحديقة — غرفة خاصة", description = "Family restaurant with private dining room. Brunch sessions with food included. Highchairs and play corner available.", address = "8 Ahmed Nessim St", city = "Giza", country = "EG", latitude = 30.0131, longitude = 31.2089, venueType = "RESTAURANT", amenities = listOf("private_room", "food_included", "play_area", "highchairs"), babyFriendly = true, strollerAccess = true, hasParking = true, hasPrivateRoom = true, hasChildcare = false, maxGroupSize = 8, operatingHours = "9:00 AM - 11:00 PM", tags = listOf("giza", "brunch", "private", "family")),
            ApprovedVenue(name = "Bibliotheca Alexandrina — Community Room", nameAr = "مكتبة الإسكندرية — قاعة المجتمع", description = "Community room in the famous library. Quiet, air-conditioned, with projector for presentations. Book in advance.", address = "El Shatby, Bab Sharqi", city = "Alexandria", country = "EG", latitude = 31.2089, longitude = 29.9092, venueType = "COMMUNITY_CENTER", amenities = listOf("projector", "air_conditioning", "quiet"), babyFriendly = true, strollerAccess = true, hasParking = true, hasPrivateRoom = true, hasWifi = true, maxGroupSize = 15, operatingHours = "10:00 AM - 7:00 PM", tags = listOf("alexandria", "library", "educational", "cultural")),
            ApprovedVenue(name = "Gezira Sporting Club — Ladies Lounge", nameAr = "نادي الجزيرة الرياضي — صالة السيدات", description = "Exclusive ladies section with comfortable lounge area. Membership or guest pass required.", address = "Gezira Island, Zamalek", city = "Cairo", country = "EG", latitude = 30.0536, longitude = 31.2244, venueType = "SUPPORT_CLUB", amenities = listOf("ladies_only", "comfortable", "refreshments"), babyFriendly = true, strollerAccess = true, hasParking = true, hasPrivateRoom = true, hasChildcare = true, maxGroupSize = 8, operatingHours = "8:00 AM - 10:00 PM", tags = listOf("club", "zamalek", "ladies", "exclusive")),
            ApprovedVenue(name = "Marriott Mena House — Garden Terrace", nameAr = "ماريوت مينا هاوس — تراس الحديقة", description = "Hotel garden terrace with pyramid views. Elegant atmosphere for special sessions. Tea and refreshments included.", address = "6 Pyramids Rd, Giza", city = "Giza", country = "EG", latitude = 29.9877, longitude = 31.1344, venueType = "HOTEL_LOUNGE", amenities = listOf("pyramid_view", "elegant", "refreshments_included"), babyFriendly = true, strollerAccess = true, hasParking = true, maxGroupSize = 8, operatingHours = "7:00 AM - 11:00 PM", tags = listOf("giza", "pyramids", "luxury", "hotel")),
            ApprovedVenue(name = "Nile Ritz-Carlton — Mothers Lounge", nameAr = "نايل ريتز كارلتون — صالة الأمهات", description = "Dedicated mothers lounge with Nile views. Baby changing facilities, comfortable seating, quiet environment.", address = "1113 Corniche El Nil, Downtown", city = "Cairo", country = "EG", latitude = 30.0422, longitude = 31.2333, venueType = "HOTEL_LOUNGE", amenities = listOf("nile_view", "baby_changing", "comfortable", "quiet"), babyFriendly = true, strollerAccess = true, hasParking = true, hasPrivateRoom = true, hasChildcare = true, hasWifi = true, maxGroupSize = 7, operatingHours = "8:00 AM - 10:00 PM", tags = listOf("downtown", "nile", "luxury", "mothers")),
            ApprovedVenue(name = "Heliopolis Club — Family Garden", nameAr = "نادي هليوبوليس — حديقة العائلات", description = "Large family garden area in the historic Heliopolis Club. Shaded seating, playground nearby, casual atmosphere.", address = "Ibrahim Al-Lakany St, Heliopolis", city = "Cairo", country = "EG", latitude = 30.0866, longitude = 31.3286, venueType = "SUPPORT_CLUB", amenities = listOf("garden", "playground", "shaded", "casual"), babyFriendly = true, strollerAccess = true, hasParking = true, hasChildcare = false, maxGroupSize = 10, operatingHours = "8:00 AM - 10:00 PM", tags = listOf("heliopolis", "club", "garden", "family"))
        )

        venues.forEach { venueRepo.createVenue(it) }
    }
}

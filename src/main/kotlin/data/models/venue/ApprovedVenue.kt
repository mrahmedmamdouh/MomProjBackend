package com.evelolvetech.data.models.venue

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

enum class VenueStatus { ACTIVE, INACTIVE, UNDER_REVIEW, REJECTED }

@Serializable
data class ApprovedVenue(
    @BsonId
    val id: String = ObjectId().toString(),
    val name: String,
    val nameAr: String = "",
    val description: String = "",
    val descriptionAr: String = "",
    val address: String,
    val city: String,
    val state: String = "",
    val country: String = "EG",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val venueType: String,
    val amenities: List<String> = emptyList(),
    val babyFriendly: Boolean = true,
    val strollerAccess: Boolean = false,
    val hasParking: Boolean = false,
    val hasPrivateRoom: Boolean = false,
    val hasChildcare: Boolean = false,
    val hasWifi: Boolean = false,
    val maxGroupSize: Int = 10,
    val operatingHours: String = "",
    val contactPhone: String = "",
    val contactEmail: String = "",
    val websiteUrl: String = "",
    val photoUrls: List<String> = emptyList(),
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0,
    val notes: String = "",
    val notesAr: String = "",
    val status: String = VenueStatus.ACTIVE.name,
    val addedBy: String = "",
    val approvedBy: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class VenueReview(
    @BsonId
    val id: String = ObjectId().toString(),
    val venueId: String,
    val reviewerType: String,
    val reviewerId: String,
    val reviewerName: String,
    val rating: Int,
    val comment: String = "",
    val visitDate: Long? = null,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

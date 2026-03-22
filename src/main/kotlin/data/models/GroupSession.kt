package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

enum class SessionType {
    ONLINE_VIDEO,
    PHYSICAL_MEETING
}

enum class VenueType {
    RESTAURANT,
    PARK,
    CAFE,
    COMMUNITY_CENTER,
    SUPPORT_CLUB,
    HOTEL_LOUNGE,
    LIBRARY,
    OTHER
}

enum class SessionStatus {
    SCHEDULED,
    OPEN_FOR_BOOKING,
    FULL,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

@Serializable
data class GroupSession(
    @BsonId
    val id: String = ObjectId().toString(),
    val doctorId: String,
    val doctorRef: String,
    val doctorName: String,
    val title: String,
    val description: String,
    val sessionType: String,
    val maxCapacity: Int = 7,
    val currentBookings: Int = 0,
    val scheduledAt: Long,
    val durationMinutes: Int = 60,
    val status: String = SessionStatus.OPEN_FOR_BOOKING.name,
    val meetingUrl: String? = null,
    val venue: SessionVenue? = null,
    val circleId: String? = null,
    val circleRef: String? = null,
    val targetInterests: List<String> = emptyList(),
    val targetPregnancyStages: List<String> = emptyList(),
    val language: String = "ar",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class SessionVenue(
    val name: String,
    val address: String,
    val city: String,
    val state: String = "",
    val country: String = "EG",
    val latitude: Double,
    val longitude: Double,
    val venueType: String = "PUBLIC",
    val notes: String = ""
)

@Serializable
data class SessionBooking(
    @BsonId
    val id: String = ObjectId().toString(),
    val sessionId: String,
    val sessionRef: String,
    val momId: String,
    val momRef: String,
    val momName: String,
    val status: String = BookingStatus.CONFIRMED.name,
    val attendanceMarked: Boolean = false,
    val joinedAt: Long? = null,
    val leftAt: Long? = null,
    val feedbackRating: Int? = null,
    val feedbackComment: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class BookingStatus {
    CONFIRMED,
    CANCELLED,
    ATTENDED,
    NO_SHOW
}

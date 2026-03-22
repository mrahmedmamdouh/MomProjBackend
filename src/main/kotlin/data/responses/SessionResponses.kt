package com.evelolvetech.data.responses

import com.evelolvetech.data.models.GroupSession
import com.evelolvetech.data.models.SessionBooking
import com.evelolvetech.data.models.SessionVenue
import kotlinx.serialization.Serializable

@Serializable
data class GroupSessionResponse(
    val id: String,
    val doctorId: String,
    val doctorName: String,
    val title: String,
    val description: String,
    val sessionType: String,
    val maxCapacity: Int,
    val currentBookings: Int,
    val availableSpots: Int,
    val scheduledAt: Long,
    val durationMinutes: Int,
    val status: String,
    val meetingUrl: String?,
    val venue: SessionVenueResponse?,
    val circleId: String?,
    val targetInterests: List<String>,
    val targetPregnancyStages: List<String>,
    val language: String,
    val createdAt: Long
) {
    companion object {
        fun fromGroupSession(session: GroupSession) = GroupSessionResponse(
            id = session.id,
            doctorId = session.doctorId,
            doctorName = session.doctorName,
            title = session.title,
            description = session.description,
            sessionType = session.sessionType,
            maxCapacity = session.maxCapacity,
            currentBookings = session.currentBookings,
            availableSpots = session.maxCapacity - session.currentBookings,
            scheduledAt = session.scheduledAt,
            durationMinutes = session.durationMinutes,
            status = session.status,
            meetingUrl = session.meetingUrl,
            venue = session.venue?.let { SessionVenueResponse.fromVenue(it) },
            circleId = session.circleId,
            targetInterests = session.targetInterests,
            targetPregnancyStages = session.targetPregnancyStages,
            language = session.language,
            createdAt = session.createdAt
        )
    }
}

@Serializable
data class SessionVenueResponse(
    val name: String,
    val address: String,
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val venueType: String,
    val notes: String
) {
    companion object {
        fun fromVenue(venue: SessionVenue) = SessionVenueResponse(
            name = venue.name,
            address = venue.address,
            city = venue.city,
            country = venue.country,
            latitude = venue.latitude,
            longitude = venue.longitude,
            venueType = venue.venueType,
            notes = venue.notes
        )
    }
}

@Serializable
data class SessionBookingResponse(
    val id: String,
    val sessionId: String,
    val momId: String,
    val momName: String,
    val status: String,
    val attendanceMarked: Boolean,
    val feedbackRating: Int?,
    val feedbackComment: String?,
    val createdAt: Long
) {
    companion object {
        fun fromBooking(booking: SessionBooking) = SessionBookingResponse(
            id = booking.id,
            sessionId = booking.sessionId,
            momId = booking.momId,
            momName = booking.momName,
            status = booking.status,
            attendanceMarked = booking.attendanceMarked,
            feedbackRating = booking.feedbackRating,
            feedbackComment = booking.feedbackComment,
            createdAt = booking.createdAt
        )
    }
}

@Serializable
data class MomSessionSummaryResponse(
    val totalBookings: Int,
    val completedSessions: Int,
    val upcomingBookings: Int,
    val isEligibleForEcommerce: Boolean,
    val sessionsUntilEcommerce: Int
)

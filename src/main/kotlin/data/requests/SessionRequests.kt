package com.evelolvetech.data.requests

data class CreateGroupSessionRequest(
    val title: String,
    val description: String,
    val sessionType: String,
    val maxCapacity: Int = 7,
    val scheduledAt: Long,
    val durationMinutes: Int = 60,
    val meetingUrl: String? = null,
    val venue: VenueRequest? = null,
    val circleId: String? = null,
    val targetInterests: List<String> = emptyList(),
    val targetPregnancyStages: List<String> = emptyList(),
    val language: String = "ar"
)

data class VenueRequest(
    val name: String,
    val address: String,
    val city: String,
    val state: String = "",
    val country: String = "EG",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val venueType: String = "CAFE",
    val notes: String = ""
)

data class UpdateGroupSessionRequest(
    val title: String? = null,
    val description: String? = null,
    val scheduledAt: Long? = null,
    val durationMinutes: Int? = null,
    val meetingUrl: String? = null,
    val maxCapacity: Int? = null,
    val status: String? = null
)

data class BookSessionRequest(
    val sessionId: String
)

data class MarkAttendanceRequest(
    val bookingId: String,
    val attended: Boolean,
    val joinedAt: Long? = null,
    val leftAt: Long? = null
)

data class SessionFeedbackRequest(
    val rating: Int,
    val comment: String = ""
)

data class CompleteSessionRequest(
    val attendanceRecords: List<AttendanceRecord>
)

data class AttendanceRecord(
    val bookingId: String,
    val attended: Boolean
)

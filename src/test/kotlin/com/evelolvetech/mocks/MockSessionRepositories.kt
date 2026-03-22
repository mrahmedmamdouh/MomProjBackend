package com.evelolvetech.mocks

import com.evelolvetech.data.models.BookingStatus
import com.evelolvetech.data.models.GroupSession
import com.evelolvetech.data.models.SessionBooking
import com.evelolvetech.data.models.SessionStatus
import com.evelolvetech.data.repository.api.session.GroupSessionRepository
import com.evelolvetech.data.repository.api.session.SessionBookingRepository

class MockGroupSessionRepository : GroupSessionRepository {
    val sessions = mutableMapOf<String, GroupSession>()

    override suspend fun createSession(session: GroupSession): Boolean {
        sessions[session.id] = session
        return true
    }

    override suspend fun getSessionById(id: String): GroupSession? = sessions[id]

    override suspend fun getSessionsByDoctorId(doctorId: String, page: Int, size: Int): List<GroupSession> {
        return sessions.values.filter { it.doctorId == doctorId }
            .sortedByDescending { it.scheduledAt }
            .drop(page * size).take(size)
    }

    override suspend fun getUpcomingSessions(page: Int, size: Int): List<GroupSession> {
        val now = System.currentTimeMillis()
        return sessions.values.filter {
            it.scheduledAt > now && it.status in listOf(SessionStatus.OPEN_FOR_BOOKING.name, SessionStatus.SCHEDULED.name)
        }.sortedBy { it.scheduledAt }.drop(page * size).take(size)
    }

    override suspend fun getUpcomingSessionsByCity(city: String, page: Int, size: Int): List<GroupSession> {
        val now = System.currentTimeMillis()
        return sessions.values.filter {
            it.scheduledAt > now &&
            it.status in listOf(SessionStatus.OPEN_FOR_BOOKING.name, SessionStatus.SCHEDULED.name) &&
            (it.sessionType == "ONLINE_VIDEO" || it.venue?.city?.contains(city, ignoreCase = true) == true)
        }.sortedBy { it.scheduledAt }.drop(page * size).take(size)
    }

    override suspend fun getSessionsByCircleId(circleId: String): List<GroupSession> {
        return sessions.values.filter { it.circleId == circleId }
    }

    override suspend fun getSessionsByStatus(status: String, page: Int, size: Int): List<GroupSession> {
        return sessions.values.filter { it.status == status }.drop(page * size).take(size)
    }

    override suspend fun updateSession(id: String, updates: Map<String, Any?>): Boolean {
        return sessions.containsKey(id)
    }

    override suspend fun updateSessionStatus(id: String, status: String): Boolean {
        val session = sessions[id] ?: return false
        sessions[id] = session.copy(status = status, updatedAt = System.currentTimeMillis())
        return true
    }

    override suspend fun incrementBookingCount(id: String): Boolean {
        val session = sessions[id] ?: return false
        sessions[id] = session.copy(currentBookings = session.currentBookings + 1)
        return true
    }

    override suspend fun decrementBookingCount(id: String): Boolean {
        val session = sessions[id] ?: return false
        sessions[id] = session.copy(currentBookings = (session.currentBookings - 1).coerceAtLeast(0))
        return true
    }

    override suspend fun deleteSession(id: String): Boolean = sessions.remove(id) != null

    override suspend fun searchSessions(query: String, page: Int, size: Int): List<GroupSession> {
        return sessions.values.filter {
            it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
        }.drop(page * size).take(size)
    }
}

class MockSessionBookingRepository : SessionBookingRepository {
    val bookings = mutableMapOf<String, SessionBooking>()

    override suspend fun createBooking(booking: SessionBooking): Boolean {
        bookings[booking.id] = booking
        return true
    }

    override suspend fun getBookingById(id: String): SessionBooking? = bookings[id]

    override suspend fun getBookingsBySessionId(sessionId: String): List<SessionBooking> {
        return bookings.values.filter { it.sessionId == sessionId }
    }

    override suspend fun getBookingsByMomId(momId: String, page: Int, size: Int): List<SessionBooking> {
        return bookings.values.filter { it.momId == momId }
            .sortedByDescending { it.createdAt }
            .drop(page * size).take(size)
    }

    override suspend fun getActiveBookingsByMomId(momId: String): List<SessionBooking> {
        return bookings.values.filter { it.momId == momId && it.status == BookingStatus.CONFIRMED.name }
    }

    override suspend fun getBookingBySessionAndMom(sessionId: String, momId: String): SessionBooking? {
        return bookings.values.find {
            it.sessionId == sessionId && it.momId == momId &&
            it.status in listOf(BookingStatus.CONFIRMED.name, BookingStatus.ATTENDED.name)
        }
    }

    override suspend fun updateBookingStatus(id: String, status: String): Boolean {
        val booking = bookings[id] ?: return false
        bookings[id] = booking.copy(status = status)
        return true
    }

    override suspend fun markAttendance(id: String, attended: Boolean, joinedAt: Long?, leftAt: Long?): Boolean {
        val booking = bookings[id] ?: return false
        val status = if (attended) BookingStatus.ATTENDED.name else BookingStatus.NO_SHOW.name
        bookings[id] = booking.copy(status = status, attendanceMarked = true, joinedAt = joinedAt, leftAt = leftAt)
        return true
    }

    override suspend fun addFeedback(id: String, rating: Int, comment: String): Boolean {
        val booking = bookings[id] ?: return false
        bookings[id] = booking.copy(feedbackRating = rating, feedbackComment = comment)
        return true
    }

    override suspend fun deleteBooking(id: String): Boolean = bookings.remove(id) != null

    override suspend fun countConfirmedBookings(sessionId: String): Int {
        return bookings.values.count { it.sessionId == sessionId && it.status == BookingStatus.CONFIRMED.name }
    }

    override suspend fun getCompletedSessionCountByMomId(momId: String): Int {
        return bookings.values.count { it.momId == momId && it.status == BookingStatus.ATTENDED.name }
    }
}

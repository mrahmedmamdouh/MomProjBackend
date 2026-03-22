package com.evelolvetech.data.repository.api.session

import com.evelolvetech.data.models.GroupSession
import com.evelolvetech.data.models.SessionBooking

interface GroupSessionRepository {
    suspend fun createSession(session: GroupSession): Boolean
    suspend fun getSessionById(id: String): GroupSession?
    suspend fun getSessionsByDoctorId(doctorId: String, page: Int = 0, size: Int = 20): List<GroupSession>
    suspend fun getUpcomingSessions(page: Int = 0, size: Int = 20): List<GroupSession>
    suspend fun getUpcomingSessionsByCity(city: String, page: Int = 0, size: Int = 20): List<GroupSession>
    suspend fun getSessionsByCircleId(circleId: String): List<GroupSession>
    suspend fun getSessionsByStatus(status: String, page: Int = 0, size: Int = 20): List<GroupSession>
    suspend fun updateSession(id: String, updates: Map<String, Any?>): Boolean
    suspend fun updateSessionStatus(id: String, status: String): Boolean
    suspend fun incrementBookingCount(id: String): Boolean
    suspend fun decrementBookingCount(id: String): Boolean
    suspend fun deleteSession(id: String): Boolean
    suspend fun searchSessions(query: String, page: Int = 0, size: Int = 20): List<GroupSession>
}

interface SessionBookingRepository {
    suspend fun createBooking(booking: SessionBooking): Boolean
    suspend fun getBookingById(id: String): SessionBooking?
    suspend fun getBookingsBySessionId(sessionId: String): List<SessionBooking>
    suspend fun getBookingsByMomId(momId: String, page: Int = 0, size: Int = 20): List<SessionBooking>
    suspend fun getActiveBookingsByMomId(momId: String): List<SessionBooking>
    suspend fun getBookingBySessionAndMom(sessionId: String, momId: String): SessionBooking?
    suspend fun updateBookingStatus(id: String, status: String): Boolean
    suspend fun markAttendance(id: String, attended: Boolean, joinedAt: Long?, leftAt: Long?): Boolean
    suspend fun addFeedback(id: String, rating: Int, comment: String): Boolean
    suspend fun deleteBooking(id: String): Boolean
    suspend fun countConfirmedBookings(sessionId: String): Int
    suspend fun getCompletedSessionCountByMomId(momId: String): Int
}

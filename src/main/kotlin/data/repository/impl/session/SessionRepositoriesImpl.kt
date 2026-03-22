package com.evelolvetech.data.repository.impl.session

import com.evelolvetech.data.models.BookingStatus
import com.evelolvetech.data.models.GroupSession
import com.evelolvetech.data.models.SessionBooking
import com.evelolvetech.data.models.SessionStatus
import com.evelolvetech.data.repository.api.session.GroupSessionRepository
import com.evelolvetech.data.repository.api.session.SessionBookingRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.*

class GroupSessionRepositoryImpl(
    db: MongoDatabase
) : GroupSessionRepository {

    private val sessions: MongoCollection<GroupSession> = db.getCollection<GroupSession>("group_sessions")

    override suspend fun createSession(session: GroupSession): Boolean {
        return try {
            sessions.insertOne(session)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getSessionById(id: String): GroupSession? {
        return sessions.findOne(GroupSession::id eq id)
    }

    override suspend fun getSessionsByDoctorId(doctorId: String, page: Int, size: Int): List<GroupSession> {
        return sessions.find(GroupSession::doctorId eq doctorId)
            .sort(descending(GroupSession::scheduledAt))
            .skip(page * size)
            .limit(size)
            .toList()
    }

    override suspend fun getUpcomingSessions(page: Int, size: Int): List<GroupSession> {
        val now = System.currentTimeMillis()
        return sessions.find(
            and(
                GroupSession::scheduledAt gt now,
                GroupSession::status `in` listOf(
                    SessionStatus.OPEN_FOR_BOOKING.name,
                    SessionStatus.SCHEDULED.name
                )
            )
        )
            .sort(ascending(GroupSession::scheduledAt))
            .skip(page * size)
            .limit(size)
            .toList()
    }

    override suspend fun getUpcomingSessionsByCity(city: String, page: Int, size: Int): List<GroupSession> {
        val now = System.currentTimeMillis()
        return sessions.find(
            and(
                GroupSession::scheduledAt gt now,
                GroupSession::status `in` listOf(
                    SessionStatus.OPEN_FOR_BOOKING.name,
                    SessionStatus.SCHEDULED.name
                ),
                or(
                    GroupSession::sessionType eq "ONLINE_VIDEO",
                    GroupSession::venue / com.evelolvetech.data.models.SessionVenue::city regex Regex(city, RegexOption.IGNORE_CASE)
                )
            )
        )
            .sort(ascending(GroupSession::scheduledAt))
            .skip(page * size)
            .limit(size)
            .toList()
    }

    override suspend fun getSessionsByCircleId(circleId: String): List<GroupSession> {
        return sessions.find(GroupSession::circleId eq circleId)
            .sort(descending(GroupSession::scheduledAt))
            .toList()
    }

    override suspend fun getSessionsByStatus(status: String, page: Int, size: Int): List<GroupSession> {
        return sessions.find(GroupSession::status eq status)
            .sort(descending(GroupSession::scheduledAt))
            .skip(page * size)
            .limit(size)
            .toList()
    }

    override suspend fun updateSession(id: String, updates: Map<String, Any?>): Boolean {
        return try {
            val setDoc = org.bson.Document(updates)
            setDoc["updatedAt"] = System.currentTimeMillis()
            val result = sessions.updateOne(
                GroupSession::id eq id,
                org.bson.Document("\$set", setDoc)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateSessionStatus(id: String, status: String): Boolean {
        return try {
            val result = sessions.updateOne(
                GroupSession::id eq id,
                combine(
                    setValue(GroupSession::status, status),
                    setValue(GroupSession::updatedAt, System.currentTimeMillis())
                )
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun incrementBookingCount(id: String): Boolean {
        return try {
            val result = sessions.updateOne(
                GroupSession::id eq id,
                combine(
                    inc(GroupSession::currentBookings, 1),
                    setValue(GroupSession::updatedAt, System.currentTimeMillis())
                )
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun decrementBookingCount(id: String): Boolean {
        return try {
            val result = sessions.updateOne(
                GroupSession::id eq id,
                combine(
                    inc(GroupSession::currentBookings, -1),
                    setValue(GroupSession::updatedAt, System.currentTimeMillis())
                )
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteSession(id: String): Boolean {
        return try {
            val result = sessions.deleteOne(GroupSession::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun searchSessions(query: String, page: Int, size: Int): List<GroupSession> {
        val regex = Regex(query, RegexOption.IGNORE_CASE)
        return sessions.find(
            or(
                GroupSession::title regex regex,
                GroupSession::description regex regex,
                GroupSession::doctorName regex regex
            )
        )
            .sort(ascending(GroupSession::scheduledAt))
            .skip(page * size)
            .limit(size)
            .toList()
    }
}

class SessionBookingRepositoryImpl(
    db: MongoDatabase
) : SessionBookingRepository {

    private val bookings: MongoCollection<SessionBooking> = db.getCollection<SessionBooking>("session_bookings")

    override suspend fun createBooking(booking: SessionBooking): Boolean {
        return try {
            bookings.insertOne(booking)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getBookingById(id: String): SessionBooking? {
        return bookings.findOne(SessionBooking::id eq id)
    }

    override suspend fun getBookingsBySessionId(sessionId: String): List<SessionBooking> {
        return bookings.find(SessionBooking::sessionId eq sessionId)
            .sort(descending(SessionBooking::createdAt))
            .toList()
    }

    override suspend fun getBookingsByMomId(momId: String, page: Int, size: Int): List<SessionBooking> {
        return bookings.find(SessionBooking::momId eq momId)
            .sort(descending(SessionBooking::createdAt))
            .skip(page * size)
            .limit(size)
            .toList()
    }

    override suspend fun getActiveBookingsByMomId(momId: String): List<SessionBooking> {
        return bookings.find(
            and(
                SessionBooking::momId eq momId,
                SessionBooking::status eq BookingStatus.CONFIRMED.name
            )
        ).toList()
    }

    override suspend fun getBookingBySessionAndMom(sessionId: String, momId: String): SessionBooking? {
        return bookings.findOne(
            and(
                SessionBooking::sessionId eq sessionId,
                SessionBooking::momId eq momId,
                SessionBooking::status `in` listOf(BookingStatus.CONFIRMED.name, BookingStatus.ATTENDED.name)
            )
        )
    }

    override suspend fun updateBookingStatus(id: String, status: String): Boolean {
        return try {
            val result = bookings.updateOne(
                SessionBooking::id eq id,
                setValue(SessionBooking::status, status)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun markAttendance(id: String, attended: Boolean, joinedAt: Long?, leftAt: Long?): Boolean {
        return try {
            val status = if (attended) BookingStatus.ATTENDED.name else BookingStatus.NO_SHOW.name
            val result = bookings.updateOne(
                SessionBooking::id eq id,
                combine(
                    setValue(SessionBooking::status, status),
                    setValue(SessionBooking::attendanceMarked, true),
                    setValue(SessionBooking::joinedAt, joinedAt),
                    setValue(SessionBooking::leftAt, leftAt)
                )
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun addFeedback(id: String, rating: Int, comment: String): Boolean {
        return try {
            val result = bookings.updateOne(
                SessionBooking::id eq id,
                combine(
                    setValue(SessionBooking::feedbackRating, rating),
                    setValue(SessionBooking::feedbackComment, comment)
                )
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteBooking(id: String): Boolean {
        return try {
            val result = bookings.deleteOne(SessionBooking::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun countConfirmedBookings(sessionId: String): Int {
        return bookings.countDocuments(
            and(
                SessionBooking::sessionId eq sessionId,
                SessionBooking::status eq BookingStatus.CONFIRMED.name
            )
        ).toInt()
    }

    override suspend fun getCompletedSessionCountByMomId(momId: String): Int {
        return bookings.countDocuments(
            and(
                SessionBooking::momId eq momId,
                SessionBooking::status eq BookingStatus.ATTENDED.name
            )
        ).toInt()
    }
}

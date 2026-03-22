package com.evelolvetech.service.session

import com.evelolvetech.data.models.*
import com.evelolvetech.data.repository.api.mom.MomRepository
import com.evelolvetech.data.repository.api.doctor.DoctorRepository
import com.evelolvetech.data.repository.api.session.GroupSessionRepository
import com.evelolvetech.data.repository.api.session.SessionBookingRepository
import com.evelolvetech.data.requests.CompleteSessionRequest
import com.evelolvetech.data.requests.CreateGroupSessionRequest
import com.evelolvetech.data.responses.*
import com.evelolvetech.util.AuthConfig
import com.evelolvetech.util.Constants
import com.evelolvetech.util.MomAuthUtil

class GroupSessionService(
    private val sessionRepository: GroupSessionRepository,
    private val bookingRepository: SessionBookingRepository,
    private val momRepository: MomRepository,
    private val doctorRepository: DoctorRepository,
    private val authConfig: AuthConfig
) {

    suspend fun createSession(doctorId: String, request: CreateGroupSessionRequest): BasicApiResponse<GroupSessionResponse> {
        return try {
            val doctor = doctorRepository.getDoctorById(doctorId)
                ?: return BasicApiResponse(success = false, message = "Doctor not found")

            if (!doctor.isAuthorized) {
                return BasicApiResponse(success = false, message = "Doctor is not authorized to create sessions")
            }

            val validationError = validateCreateSessionRequest(request)
            if (validationError != null) {
                return BasicApiResponse(success = false, message = validationError)
            }

            val venue = if (request.sessionType == SessionType.PHYSICAL_MEETING.name && request.venue != null) {
                SessionVenue(
                    name = request.venue.name,
                    address = request.venue.address,
                    city = request.venue.city,
                    state = request.venue.state,
                    country = request.venue.country,
                    latitude = request.venue.latitude,
                    longitude = request.venue.longitude,
                    venueType = request.venue.venueType,
                    notes = request.venue.notes
                )
            } else null

            val session = GroupSession(
                doctorId = doctorId,
                doctorRef = "/doctors/$doctorId",
                doctorName = doctor.name,
                title = request.title,
                description = request.description,
                sessionType = request.sessionType,
                maxCapacity = request.maxCapacity.coerceIn(2, 7),
                scheduledAt = request.scheduledAt,
                durationMinutes = request.durationMinutes,
                meetingUrl = if (request.sessionType == SessionType.ONLINE_VIDEO.name) request.meetingUrl else null,
                venue = venue,
                circleId = request.circleId,
                circleRef = request.circleId?.let { "/circles/$it" },
                targetInterests = request.targetInterests,
                targetPregnancyStages = request.targetPregnancyStages,
                language = request.language,
                status = SessionStatus.OPEN_FOR_BOOKING.name
            )

            val created = sessionRepository.createSession(session)
            if (!created) {
                return BasicApiResponse(success = false, message = "Failed to create session")
            }

            BasicApiResponse(
                success = true,
                data = GroupSessionResponse.fromGroupSession(session),
                message = "Session created successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error creating session: ${e.message}")
        }
    }

    suspend fun bookSession(momId: String, sessionId: String): BasicApiResponse<SessionBookingResponse> {
        return try {
            val mom = momRepository.getMomById(momId)
                ?: return BasicApiResponse(success = false, message = "Mom not found")

            val session = sessionRepository.getSessionById(sessionId)
                ?: return BasicApiResponse(success = false, message = "Session not found")

            if (session.status != SessionStatus.OPEN_FOR_BOOKING.name) {
                return BasicApiResponse(success = false, message = "Session is not open for booking (status: ${session.status})")
            }

            if (session.currentBookings >= session.maxCapacity) {
                return BasicApiResponse(success = false, message = "Session is full (${session.maxCapacity} spots taken)")
            }

            if (session.scheduledAt <= System.currentTimeMillis()) {
                return BasicApiResponse(success = false, message = "Cannot book a session that has already started or passed")
            }

            val existingBooking = bookingRepository.getBookingBySessionAndMom(sessionId, momId)
            if (existingBooking != null) {
                return BasicApiResponse(success = false, message = "You are already booked for this session")
            }

            val booking = SessionBooking(
                sessionId = sessionId,
                sessionRef = "/sessions/$sessionId",
                momId = momId,
                momRef = "/moms/$momId",
                momName = mom.fullName,
                status = BookingStatus.CONFIRMED.name
            )

            val booked = bookingRepository.createBooking(booking)
            if (!booked) {
                return BasicApiResponse(success = false, message = "Failed to create booking")
            }

            sessionRepository.incrementBookingCount(sessionId)

            val updatedSession = sessionRepository.getSessionById(sessionId)
            if (updatedSession != null && updatedSession.currentBookings >= updatedSession.maxCapacity) {
                sessionRepository.updateSessionStatus(sessionId, SessionStatus.FULL.name)
            }

            BasicApiResponse(
                success = true,
                data = SessionBookingResponse.fromBooking(booking),
                message = "Session booked successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error booking session: ${e.message}")
        }
    }

    suspend fun cancelBooking(momId: String, bookingId: String): BasicApiResponse<Unit> {
        return try {
            val booking = bookingRepository.getBookingById(bookingId)
                ?: return BasicApiResponse(success = false, message = "Booking not found")

            if (booking.momId != momId) {
                return BasicApiResponse(success = false, message = "Access denied: Booking does not belong to user")
            }

            if (booking.status != BookingStatus.CONFIRMED.name) {
                return BasicApiResponse(success = false, message = "Can only cancel confirmed bookings")
            }

            val session = sessionRepository.getSessionById(booking.sessionId)
            if (session != null && session.scheduledAt <= System.currentTimeMillis()) {
                return BasicApiResponse(success = false, message = "Cannot cancel booking for a session that has already started")
            }

            bookingRepository.updateBookingStatus(bookingId, BookingStatus.CANCELLED.name)
            sessionRepository.decrementBookingCount(booking.sessionId)

            if (session != null && session.status == SessionStatus.FULL.name) {
                sessionRepository.updateSessionStatus(booking.sessionId, SessionStatus.OPEN_FOR_BOOKING.name)
            }

            BasicApiResponse(success = true, message = "Booking cancelled successfully")
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error cancelling booking: ${e.message}")
        }
    }

    suspend fun completeSession(doctorId: String, sessionId: String, request: CompleteSessionRequest): BasicApiResponse<GroupSessionResponse> {
        return try {
            val session = sessionRepository.getSessionById(sessionId)
                ?: return BasicApiResponse(success = false, message = "Session not found")

            if (session.doctorId != doctorId) {
                return BasicApiResponse(success = false, message = "Access denied: Session does not belong to this doctor")
            }

            if (session.status == SessionStatus.COMPLETED.name) {
                return BasicApiResponse(success = false, message = "Session is already completed")
            }

            if (session.status == SessionStatus.CANCELLED.name) {
                return BasicApiResponse(success = false, message = "Cannot complete a cancelled session")
            }

            for (record in request.attendanceRecords) {
                val booking = bookingRepository.getBookingById(record.bookingId) ?: continue

                if (booking.sessionId != sessionId) continue

                bookingRepository.markAttendance(
                    id = record.bookingId,
                    attended = record.attended,
                    joinedAt = if (record.attended) session.scheduledAt else null,
                    leftAt = if (record.attended) session.scheduledAt + (session.durationMinutes * 60000L) else null
                )

                if (record.attended) {
                    val completedCount = bookingRepository.getCompletedSessionCountByMomId(booking.momId)
                    momRepository.updateMomSessions(booking.momId, completedCount)

                    if (completedCount >= authConfig.momAuthorizationSessionThreshold) {
                        momRepository.updateMomAuthorization(booking.momId, true)
                        MomAuthUtil.invalidateMomAuthCache(booking.momId)
                    }
                }
            }

            sessionRepository.updateSessionStatus(sessionId, SessionStatus.COMPLETED.name)

            val updatedSession = sessionRepository.getSessionById(sessionId)
            BasicApiResponse(
                success = true,
                data = updatedSession?.let { GroupSessionResponse.fromGroupSession(it) },
                message = "Session completed and attendance recorded"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error completing session: ${e.message}")
        }
    }

    suspend fun addFeedback(momId: String, bookingId: String, rating: Int, comment: String): BasicApiResponse<SessionBookingResponse> {
        return try {
            val booking = bookingRepository.getBookingById(bookingId)
                ?: return BasicApiResponse(success = false, message = "Booking not found")

            if (booking.momId != momId) {
                return BasicApiResponse(success = false, message = "Access denied: Booking does not belong to user")
            }

            if (booking.status != BookingStatus.ATTENDED.name) {
                return BasicApiResponse(success = false, message = "Can only provide feedback for attended sessions")
            }

            if (rating < 1 || rating > 5) {
                return BasicApiResponse(success = false, message = "Rating must be between 1 and 5")
            }

            bookingRepository.addFeedback(bookingId, rating, comment.trim())

            val updatedBooking = bookingRepository.getBookingById(bookingId)
            BasicApiResponse(
                success = true,
                data = updatedBooking?.let { SessionBookingResponse.fromBooking(it) },
                message = "Feedback submitted successfully"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error submitting feedback: ${e.message}")
        }
    }

    suspend fun getUpcomingSessions(page: Int, size: Int): BasicApiResponse<List<GroupSessionResponse>> {
        return try {
            val sessions = sessionRepository.getUpcomingSessions(page, size)
            BasicApiResponse(
                success = true,
                data = sessions.map { GroupSessionResponse.fromGroupSession(it) }
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving sessions: ${e.message}")
        }
    }

    suspend fun getUpcomingSessionsByCity(city: String, page: Int, size: Int): BasicApiResponse<List<GroupSessionResponse>> {
        return try {
            val sessions = sessionRepository.getUpcomingSessionsByCity(city, page, size)
            BasicApiResponse(
                success = true,
                data = sessions.map { GroupSessionResponse.fromGroupSession(it) }
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving sessions: ${e.message}")
        }
    }

    suspend fun getSessionById(sessionId: String): BasicApiResponse<GroupSessionResponse> {
        return try {
            val session = sessionRepository.getSessionById(sessionId)
                ?: return BasicApiResponse(success = false, message = "Session not found")
            BasicApiResponse(success = true, data = GroupSessionResponse.fromGroupSession(session))
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving session: ${e.message}")
        }
    }

    suspend fun getSessionBookings(doctorId: String, sessionId: String): BasicApiResponse<List<SessionBookingResponse>> {
        return try {
            val session = sessionRepository.getSessionById(sessionId)
                ?: return BasicApiResponse(success = false, message = "Session not found")

            if (session.doctorId != doctorId) {
                return BasicApiResponse(success = false, message = "Access denied: Session does not belong to this doctor")
            }

            val bookings = bookingRepository.getBookingsBySessionId(sessionId)
            BasicApiResponse(
                success = true,
                data = bookings.map { SessionBookingResponse.fromBooking(it) }
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving bookings: ${e.message}")
        }
    }

    suspend fun getMyBookings(momId: String, page: Int, size: Int): BasicApiResponse<List<SessionBookingResponse>> {
        return try {
            val bookings = bookingRepository.getBookingsByMomId(momId, page, size)
            BasicApiResponse(
                success = true,
                data = bookings.map { SessionBookingResponse.fromBooking(it) }
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving bookings: ${e.message}")
        }
    }

    suspend fun getMySessionSummary(momId: String): BasicApiResponse<MomSessionSummaryResponse> {
        return try {
            val allBookings = bookingRepository.getBookingsByMomId(momId, 0, 1000)
            val completedCount = bookingRepository.getCompletedSessionCountByMomId(momId)
            val upcomingCount = allBookings.count { it.status == BookingStatus.CONFIRMED.name }
            val threshold = authConfig.momAuthorizationSessionThreshold
            val sessionsUntil = (threshold - completedCount).coerceAtLeast(0)

            BasicApiResponse(
                success = true,
                data = MomSessionSummaryResponse(
                    totalBookings = allBookings.size,
                    completedSessions = completedCount,
                    upcomingBookings = upcomingCount,
                    isEligibleForEcommerce = completedCount >= threshold,
                    sessionsUntilEcommerce = sessionsUntil
                )
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving session summary: ${e.message}")
        }
    }

    suspend fun getDoctorSessions(doctorId: String, page: Int, size: Int): BasicApiResponse<List<GroupSessionResponse>> {
        return try {
            val sessions = sessionRepository.getSessionsByDoctorId(doctorId, page, size)
            BasicApiResponse(
                success = true,
                data = sessions.map { GroupSessionResponse.fromGroupSession(it) }
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving doctor sessions: ${e.message}")
        }
    }

    suspend fun cancelSession(doctorId: String, sessionId: String): BasicApiResponse<Unit> {
        return try {
            val session = sessionRepository.getSessionById(sessionId)
                ?: return BasicApiResponse(success = false, message = "Session not found")

            if (session.doctorId != doctorId) {
                return BasicApiResponse(success = false, message = "Access denied: Session does not belong to this doctor")
            }

            if (session.status == SessionStatus.COMPLETED.name) {
                return BasicApiResponse(success = false, message = "Cannot cancel a completed session")
            }

            val bookings = bookingRepository.getBookingsBySessionId(sessionId)
            for (booking in bookings) {
                if (booking.status == BookingStatus.CONFIRMED.name) {
                    bookingRepository.updateBookingStatus(booking.id, BookingStatus.CANCELLED.name)
                }
            }

            sessionRepository.updateSessionStatus(sessionId, SessionStatus.CANCELLED.name)

            BasicApiResponse(success = true, message = "Session cancelled. ${bookings.size} booking(s) cancelled.")
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error cancelling session: ${e.message}")
        }
    }

    suspend fun searchSessions(query: String, page: Int, size: Int): BasicApiResponse<List<GroupSessionResponse>> {
        return try {
            val sessions = sessionRepository.searchSessions(query, page, size)
            BasicApiResponse(
                success = true,
                data = sessions.map { GroupSessionResponse.fromGroupSession(it) }
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error searching sessions: ${e.message}")
        }
    }

    private fun validateCreateSessionRequest(request: CreateGroupSessionRequest): String? {
        if (request.title.isBlank()) return "Title is required"
        if (request.description.isBlank()) return "Description is required"

        val validTypes = listOf(SessionType.ONLINE_VIDEO.name, SessionType.PHYSICAL_MEETING.name)
        if (request.sessionType !in validTypes) {
            return "Session type must be one of: ${validTypes.joinToString(", ")}"
        }

        if (request.maxCapacity < 2 || request.maxCapacity > 7) {
            return "Capacity must be between 2 and 7 moms"
        }

        if (request.scheduledAt <= System.currentTimeMillis()) {
            return "Session must be scheduled in the future"
        }

        if (request.durationMinutes < 15 || request.durationMinutes > 180) {
            return "Duration must be between 15 and 180 minutes"
        }

        if (request.sessionType == SessionType.PHYSICAL_MEETING.name && request.venue == null) {
            return "Venue is required for physical meeting sessions"
        }

        if (request.sessionType == SessionType.ONLINE_VIDEO.name && request.meetingUrl.isNullOrBlank()) {
            return "Meeting URL is required for online video sessions"
        }

        return null
    }
}

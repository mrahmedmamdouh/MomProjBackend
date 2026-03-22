package com.evelolvetech.service

import com.evelolvetech.data.models.*
import com.evelolvetech.data.requests.AttendanceRecord
import com.evelolvetech.data.requests.CompleteSessionRequest
import com.evelolvetech.data.requests.CreateGroupSessionRequest
import com.evelolvetech.data.requests.VenueRequest
import com.evelolvetech.mocks.*
import com.evelolvetech.service.session.GroupSessionService
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class GroupSessionServiceTest {

    private val mockSessionRepository = MockGroupSessionRepository()
    private val mockBookingRepository = MockSessionBookingRepository()
    private val mockMomRepository = MockMomRepository()
    private val mockDoctorRepository = MockDoctorRepository()
    private val authConfig = MockAuthConfig.instance

    private val sessionService = GroupSessionService(
        mockSessionRepository,
        mockBookingRepository,
        mockMomRepository,
        mockDoctorRepository,
        authConfig
    )

    private val futureTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)

    init {
        mockDoctorRepository.doctors["doc_smith"] = Doctor(
            id = "doc_smith",
            authUid = "auth_smith",
            name = "Dr. Sarah Smith",
            email = "dr.smith@example.com",
            phone = "+1-555-0101",
            specialization = "PSYCHIATRIST",
            isAuthorized = true,
            nidId = "nid_smith",
            nidRef = "/nids/nid_smith"
        )

        mockDoctorRepository.doctors["doc_unauth"] = Doctor(
            id = "doc_unauth",
            authUid = "auth_unauth",
            name = "Dr. Pending",
            email = "dr.pending@example.com",
            phone = "+1-555-0199",
            specialization = "CLINICAL_PSYCHOLOGIST",
            isAuthorized = false,
            nidId = "nid_unauth",
            nidRef = "/nids/nid_unauth"
        )

        mockMomRepository.moms["mom_alice"] = Mom(
            id = "mom_alice",
            authUid = "auth_alice",
            fullName = "Alice Mom",
            email = "alice@example.com",
            phone = "+1234567890",
            maritalStatus = "SINGLE",
            photoUrl = "",
            numberOfSessions = 3,
            isAuthorized = false,
            nidId = "nid_alice",
            nidRef = "/nids/nid_alice"
        )

        mockMomRepository.moms["mom_beth"] = Mom(
            id = "mom_beth",
            authUid = "auth_beth",
            fullName = "Beth Mom",
            email = "beth@example.com",
            phone = "+1234567891",
            maritalStatus = "MARRIED",
            photoUrl = "",
            numberOfSessions = 8,
            isAuthorized = true,
            nidId = "nid_beth",
            nidRef = "/nids/nid_beth"
        )
    }

    @Test
    fun testCreateOnlineSessionSuccess() = runBlocking {
        val request = CreateGroupSessionRequest(
            title = "Anxiety Support Group",
            description = "Group therapy for pregnancy anxiety",
            sessionType = "ONLINE_VIDEO",
            maxCapacity = 7,
            scheduledAt = futureTime,
            durationMinutes = 60,
            meetingUrl = "https://meet.momcare.com/test-session"
        )

        val result = sessionService.createSession("doc_smith", request)

        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals("ONLINE_VIDEO", result.data!!.sessionType)
        assertEquals(7, result.data!!.maxCapacity)
        assertEquals("OPEN_FOR_BOOKING", result.data!!.status)
    }

    @Test
    fun testCreatePhysicalSessionSuccess() = runBlocking {
        val request = CreateGroupSessionRequest(
            title = "Postpartum Support Circle",
            description = "In-person group therapy",
            sessionType = "PHYSICAL_MEETING",
            maxCapacity = 6,
            scheduledAt = futureTime,
            durationMinutes = 90,
            venue = VenueRequest(
                name = "Community Center",
                address = "123 Main St",
                city = "Cairo",
                country = "EG",
                latitude = 30.0444,
                longitude = 31.2357
            )
        )

        val result = sessionService.createSession("doc_smith", request)

        assertTrue(result.success)
        assertNotNull(result.data!!.venue)
        assertEquals("Cairo", result.data!!.venue!!.city)
    }

    @Test
    fun testCreateSessionUnauthorizedDoctor() = runBlocking {
        val request = CreateGroupSessionRequest(
            title = "Test Session",
            description = "Test",
            sessionType = "ONLINE_VIDEO",
            scheduledAt = futureTime,
            meetingUrl = "https://meet.momcare.com/test"
        )

        val result = sessionService.createSession("doc_unauth", request)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("not authorized"))
    }

    @Test
    fun testCreateSessionCapacityExceeds7() = runBlocking {
        val request = CreateGroupSessionRequest(
            title = "Test",
            description = "Test",
            sessionType = "ONLINE_VIDEO",
            maxCapacity = 15,
            scheduledAt = futureTime,
            meetingUrl = "https://meet.momcare.com/test"
        )

        val result = sessionService.createSession("doc_smith", request)

        assertTrue(result.success)
        assertEquals(7, result.data!!.maxCapacity)
    }

    @Test
    fun testCreatePhysicalSessionWithoutVenue() = runBlocking {
        val request = CreateGroupSessionRequest(
            title = "Test",
            description = "Test",
            sessionType = "PHYSICAL_MEETING",
            scheduledAt = futureTime
        )

        val result = sessionService.createSession("doc_smith", request)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("Venue is required"))
    }

    @Test
    fun testCreateOnlineSessionWithoutUrl() = runBlocking {
        val request = CreateGroupSessionRequest(
            title = "Test",
            description = "Test",
            sessionType = "ONLINE_VIDEO",
            scheduledAt = futureTime,
            meetingUrl = null
        )

        val result = sessionService.createSession("doc_smith", request)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("Meeting URL is required"))
    }

    @Test
    fun testBookSessionSuccess() = runBlocking {
        val session = GroupSession(
            id = "session_book_test",
            doctorId = "doc_smith",
            doctorRef = "/doctors/doc_smith",
            doctorName = "Dr. Smith",
            title = "Test Session",
            description = "Test",
            sessionType = "ONLINE_VIDEO",
            maxCapacity = 7,
            currentBookings = 0,
            scheduledAt = futureTime,
            status = SessionStatus.OPEN_FOR_BOOKING.name,
            meetingUrl = "https://meet.momcare.com/test"
        )
        mockSessionRepository.sessions[session.id] = session

        val result = sessionService.bookSession("mom_alice", session.id)

        assertTrue(result.success)
        assertEquals("CONFIRMED", result.data!!.status)
        assertEquals("mom_alice", result.data!!.momId)
        assertEquals(1, mockSessionRepository.sessions[session.id]!!.currentBookings)
    }

    @Test
    fun testBookSessionAlreadyFull() = runBlocking {
        val session = GroupSession(
            id = "session_full",
            doctorId = "doc_smith",
            doctorRef = "/doctors/doc_smith",
            doctorName = "Dr. Smith",
            title = "Full Session",
            description = "Test",
            sessionType = "ONLINE_VIDEO",
            maxCapacity = 7,
            currentBookings = 7,
            scheduledAt = futureTime,
            status = SessionStatus.OPEN_FOR_BOOKING.name,
            meetingUrl = "https://meet.momcare.com/full"
        )
        mockSessionRepository.sessions[session.id] = session

        val result = sessionService.bookSession("mom_alice", session.id)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("full"))
    }

    @Test
    fun testBookSessionDuplicatePrevention() = runBlocking {
        val session = GroupSession(
            id = "session_dup",
            doctorId = "doc_smith",
            doctorRef = "/doctors/doc_smith",
            doctorName = "Dr. Smith",
            title = "Dup Test",
            description = "Test",
            sessionType = "ONLINE_VIDEO",
            maxCapacity = 7,
            currentBookings = 0,
            scheduledAt = futureTime,
            status = SessionStatus.OPEN_FOR_BOOKING.name,
            meetingUrl = "https://meet.momcare.com/dup"
        )
        mockSessionRepository.sessions[session.id] = session

        val first = sessionService.bookSession("mom_beth", session.id)
        assertTrue(first.success)

        val duplicate = sessionService.bookSession("mom_beth", session.id)
        assertFalse(duplicate.success)
        assertTrue(duplicate.message!!.contains("already booked"))
    }

    @Test
    fun testCancelBookingSuccess() = runBlocking {
        val session = GroupSession(
            id = "session_cancel",
            doctorId = "doc_smith",
            doctorRef = "/doctors/doc_smith",
            doctorName = "Dr. Smith",
            title = "Cancel Test",
            description = "Test",
            sessionType = "ONLINE_VIDEO",
            maxCapacity = 7,
            currentBookings = 1,
            scheduledAt = futureTime,
            status = SessionStatus.OPEN_FOR_BOOKING.name,
            meetingUrl = "https://meet.momcare.com/cancel"
        )
        mockSessionRepository.sessions[session.id] = session

        val booking = SessionBooking(
            id = "booking_cancel_test",
            sessionId = session.id,
            sessionRef = "/sessions/${session.id}",
            momId = "mom_alice",
            momRef = "/moms/mom_alice",
            momName = "Alice",
            status = BookingStatus.CONFIRMED.name
        )
        mockBookingRepository.bookings[booking.id] = booking

        val result = sessionService.cancelBooking("mom_alice", booking.id)

        assertTrue(result.success)
        assertEquals(BookingStatus.CANCELLED.name, mockBookingRepository.bookings[booking.id]!!.status)
    }

    @Test
    fun testCancelBookingWrongUser() = runBlocking {
        val booking = SessionBooking(
            id = "booking_wrong_user",
            sessionId = "session_x",
            sessionRef = "/sessions/session_x",
            momId = "mom_beth",
            momRef = "/moms/mom_beth",
            momName = "Beth",
            status = BookingStatus.CONFIRMED.name
        )
        mockBookingRepository.bookings[booking.id] = booking

        val result = sessionService.cancelBooking("mom_alice", booking.id)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("Access denied"))
    }

    @Test
    fun testCompleteSessionAutoIncrementsMomSessions() = runBlocking {
        val session = GroupSession(
            id = "session_complete",
            doctorId = "doc_smith",
            doctorRef = "/doctors/doc_smith",
            doctorName = "Dr. Smith",
            title = "Complete Test",
            description = "Test",
            sessionType = "ONLINE_VIDEO",
            maxCapacity = 7,
            currentBookings = 2,
            scheduledAt = System.currentTimeMillis() - 60000,
            status = SessionStatus.IN_PROGRESS.name,
            meetingUrl = "https://meet.momcare.com/complete"
        )
        mockSessionRepository.sessions[session.id] = session

        val booking1 = SessionBooking(
            id = "booking_complete_1",
            sessionId = session.id,
            sessionRef = "/sessions/${session.id}",
            momId = "mom_alice",
            momRef = "/moms/mom_alice",
            momName = "Alice",
            status = BookingStatus.CONFIRMED.name
        )
        val booking2 = SessionBooking(
            id = "booking_complete_2",
            sessionId = session.id,
            sessionRef = "/sessions/${session.id}",
            momId = "mom_beth",
            momRef = "/moms/mom_beth",
            momName = "Beth",
            status = BookingStatus.CONFIRMED.name
        )
        mockBookingRepository.bookings[booking1.id] = booking1
        mockBookingRepository.bookings[booking2.id] = booking2

        val request = CompleteSessionRequest(
            attendanceRecords = listOf(
                AttendanceRecord(bookingId = booking1.id, attended = true),
                AttendanceRecord(bookingId = booking2.id, attended = true)
            )
        )

        val result = sessionService.completeSession("doc_smith", session.id, request)

        assertTrue(result.success)
        assertEquals(SessionStatus.COMPLETED.name, mockSessionRepository.sessions[session.id]!!.status)
        assertEquals(BookingStatus.ATTENDED.name, mockBookingRepository.bookings[booking1.id]!!.status)
        assertEquals(BookingStatus.ATTENDED.name, mockBookingRepository.bookings[booking2.id]!!.status)
    }

    @Test
    fun testCompleteSessionWrongDoctor() = runBlocking {
        val session = GroupSession(
            id = "session_wrong_doc",
            doctorId = "doc_smith",
            doctorRef = "/doctors/doc_smith",
            doctorName = "Dr. Smith",
            title = "Wrong Doc Test",
            description = "Test",
            sessionType = "ONLINE_VIDEO",
            scheduledAt = System.currentTimeMillis() - 60000,
            status = SessionStatus.IN_PROGRESS.name,
            meetingUrl = "https://meet.momcare.com/wrong"
        )
        mockSessionRepository.sessions[session.id] = session

        val request = CompleteSessionRequest(attendanceRecords = emptyList())
        val result = sessionService.completeSession("doc_unauth", session.id, request)

        assertFalse(result.success)
        assertTrue(result.message!!.contains("Access denied"))
    }

    @Test
    fun testAddFeedbackSuccess() = runBlocking {
        val booking = SessionBooking(
            id = "booking_feedback",
            sessionId = "session_x",
            sessionRef = "/sessions/session_x",
            momId = "mom_beth",
            momRef = "/moms/mom_beth",
            momName = "Beth",
            status = BookingStatus.ATTENDED.name,
            attendanceMarked = true
        )
        mockBookingRepository.bookings[booking.id] = booking

        val result = sessionService.addFeedback("mom_beth", booking.id, 5, "Amazing session, felt truly supported")

        assertTrue(result.success)
        assertEquals(5, mockBookingRepository.bookings[booking.id]!!.feedbackRating)
    }

    @Test
    fun testAddFeedbackNotAttended() = runBlocking {
        val booking = SessionBooking(
            id = "booking_no_attend",
            sessionId = "session_x",
            sessionRef = "/sessions/session_x",
            momId = "mom_alice",
            momRef = "/moms/mom_alice",
            momName = "Alice",
            status = BookingStatus.CONFIRMED.name
        )
        mockBookingRepository.bookings[booking.id] = booking

        val result = sessionService.addFeedback("mom_alice", booking.id, 4, "Good")

        assertFalse(result.success)
        assertTrue(result.message!!.contains("attended sessions"))
    }

    @Test
    fun testGetMySessionSummary() = runBlocking {
        mockBookingRepository.bookings.clear()
        mockBookingRepository.bookings["b1"] = SessionBooking(
            id = "b1", sessionId = "s1", sessionRef = "/sessions/s1",
            momId = "mom_alice", momRef = "/moms/mom_alice", momName = "Alice",
            status = BookingStatus.ATTENDED.name
        )
        mockBookingRepository.bookings["b2"] = SessionBooking(
            id = "b2", sessionId = "s2", sessionRef = "/sessions/s2",
            momId = "mom_alice", momRef = "/moms/mom_alice", momName = "Alice",
            status = BookingStatus.ATTENDED.name
        )
        mockBookingRepository.bookings["b3"] = SessionBooking(
            id = "b3", sessionId = "s3", sessionRef = "/sessions/s3",
            momId = "mom_alice", momRef = "/moms/mom_alice", momName = "Alice",
            status = BookingStatus.CONFIRMED.name
        )

        val result = sessionService.getMySessionSummary("mom_alice")

        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals(3, result.data!!.totalBookings)
        assertEquals(2, result.data!!.completedSessions)
        assertEquals(1, result.data!!.upcomingBookings)
        assertFalse(result.data!!.isEligibleForEcommerce)
        assertTrue(result.data!!.sessionsUntilEcommerce > 0)
    }

    @Test
    fun testCancelSessionByDoctor() = runBlocking {
        val session = GroupSession(
            id = "session_doc_cancel",
            doctorId = "doc_smith",
            doctorRef = "/doctors/doc_smith",
            doctorName = "Dr. Smith",
            title = "Cancel Me",
            description = "Test",
            sessionType = "ONLINE_VIDEO",
            scheduledAt = futureTime,
            status = SessionStatus.OPEN_FOR_BOOKING.name,
            meetingUrl = "https://meet.momcare.com/cancel-me"
        )
        mockSessionRepository.sessions[session.id] = session

        val booking = SessionBooking(
            id = "booking_doc_cancel",
            sessionId = session.id,
            sessionRef = "/sessions/${session.id}",
            momId = "mom_alice",
            momRef = "/moms/mom_alice",
            momName = "Alice",
            status = BookingStatus.CONFIRMED.name
        )
        mockBookingRepository.bookings[booking.id] = booking

        val result = sessionService.cancelSession("doc_smith", session.id)

        assertTrue(result.success)
        assertEquals(SessionStatus.CANCELLED.name, mockSessionRepository.sessions[session.id]!!.status)
        assertEquals(BookingStatus.CANCELLED.name, mockBookingRepository.bookings[booking.id]!!.status)
    }

    @Test
    fun testSessionAutoFillStatus() = runBlocking {
        val session = GroupSession(
            id = "session_autofill",
            doctorId = "doc_smith",
            doctorRef = "/doctors/doc_smith",
            doctorName = "Dr. Smith",
            title = "Almost Full",
            description = "Test",
            sessionType = "ONLINE_VIDEO",
            maxCapacity = 2,
            currentBookings = 1,
            scheduledAt = futureTime,
            status = SessionStatus.OPEN_FOR_BOOKING.name,
            meetingUrl = "https://meet.momcare.com/autofill"
        )
        mockSessionRepository.sessions[session.id] = session

        val result = sessionService.bookSession("mom_alice", session.id)

        assertTrue(result.success)
        assertEquals(SessionStatus.FULL.name, mockSessionRepository.sessions[session.id]!!.status)
    }

    @Test
    fun testSearchSessions() = runBlocking {
        mockSessionRepository.sessions.clear()
        mockSessionRepository.sessions["s1"] = GroupSession(
            id = "s1", doctorId = "doc_smith", doctorRef = "/doctors/doc_smith",
            doctorName = "Dr. Smith", title = "Anxiety Support Group",
            description = "Help with anxiety", sessionType = "ONLINE_VIDEO",
            scheduledAt = futureTime, status = SessionStatus.OPEN_FOR_BOOKING.name,
            meetingUrl = "https://meet.momcare.com/s1"
        )
        mockSessionRepository.sessions["s2"] = GroupSession(
            id = "s2", doctorId = "doc_smith", doctorRef = "/doctors/doc_smith",
            doctorName = "Dr. Smith", title = "Depression Recovery",
            description = "Postpartum depression support", sessionType = "ONLINE_VIDEO",
            scheduledAt = futureTime, status = SessionStatus.OPEN_FOR_BOOKING.name,
            meetingUrl = "https://meet.momcare.com/s2"
        )

        val result = sessionService.searchSessions("anxiety", 0, 20)

        assertTrue(result.success)
        assertEquals(1, result.data!!.size)
        assertEquals("Anxiety Support Group", result.data!![0].title)
    }
}

package com.evelolvetech.routes

import com.evelolvetech.data.models.Doctor
import com.evelolvetech.data.models.Mom
import com.evelolvetech.data.models.User
import com.evelolvetech.data.models.UserType
import com.evelolvetech.routes.auth.authRoutes
import com.evelolvetech.routes.doctor.doctorRoutes
import com.evelolvetech.routes.mom.momRoutes
import com.evelolvetech.service.auth.AuthService
import com.evelolvetech.service.doctor.DoctorService
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.mocks.*
import com.google.gson.Gson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*

class RouteTest {

    private val mockUserRepository = MockUserRepository()
    private val mockHashingService = MockHashingService()
    private val mockRefreshTokenRepository = MockRefreshTokenRepository()
    private val mockAuthConfig = MockAuthConfig.instance
    private val mockMomRepository = MockMomRepository()
    private val mockDoctorRepository = MockDoctorRepository()
    private val mockNidRepository = MockNidRepository()
    private val mockTransactionService = MockTransactionService()

    private val authService = AuthService(mockUserRepository, mockHashingService, mockRefreshTokenRepository, mockAuthConfig)
    private val momService = MomService(mockMomRepository, mockNidRepository, mockUserRepository, mockHashingService, mockTransactionService, mockAuthConfig)
    private val doctorService = DoctorService(mockDoctorRepository, mockNidRepository, mockUserRepository, mockHashingService, mockTransactionService)
    private val gson = Gson()

    @Test
    fun `Doctor model should have correct properties`() {
        val doctor = Doctor(
            id = "doctor123",
            authUid = "auth123",
            name = "Dr. Smith",
            email = "doctor@example.com",
            phone = "+1234567890",
            specialization = "Cardiology",
            rating = 4.5,
            isAuthorized = true,
            photo = "http://example.com/photo.jpg",
            nidId = "nid123",
            nidRef = "nidref123",
            createdAt = System.currentTimeMillis()
        )

        assertEquals("doctor123", doctor.id)
        assertEquals("auth123", doctor.authUid)
        assertEquals("Dr. Smith", doctor.name)
        assertEquals("doctor@example.com", doctor.email)
        assertEquals("+1234567890", doctor.phone)
        assertEquals("Cardiology", doctor.specialization)
        assertEquals(4.5, doctor.rating)
        assertTrue(doctor.isAuthorized)
        assertEquals("http://example.com/photo.jpg", doctor.photo)
        assertEquals("nid123", doctor.nidId)
        assertEquals("nidref123", doctor.nidRef)
        assertTrue(doctor.createdAt > 0)
    }

    @Test
    fun `Mom model should have correct properties`() {
        val mom = Mom(
            id = "mom123",
            authUid = "auth123",
            fullName = "Jane Doe",
            email = "mom@example.com",
            phone = "+1234567890",
            maritalStatus = "MARRIED",
            photoUrl = "http://example.com/photo.jpg",
            numberOfSessions = 5,
            isAuthorized = true,
            nidId = "nid123",
            nidRef = "nidref123",
            createdAt = System.currentTimeMillis()
        )

        assertEquals("mom123", mom.id)
        assertEquals("auth123", mom.authUid)
        assertEquals("Jane Doe", mom.fullName)
        assertEquals("mom@example.com", mom.email)
        assertEquals("+1234567890", mom.phone)
        assertEquals("MARRIED", mom.maritalStatus)
        assertEquals("http://example.com/photo.jpg", mom.photoUrl)
        assertEquals(5, mom.numberOfSessions)
        assertTrue(mom.isAuthorized)
        assertEquals("nid123", mom.nidId)
        assertEquals("nidref123", mom.nidRef)
        assertTrue(mom.createdAt > 0)
    }

    @Test
    fun `User model should have correct properties`() {
        val user = User(
            id = "user123",
            email = "user@example.com",
            password = "hashedPassword",
            userType = UserType.DOCTOR,
            createdAt = System.currentTimeMillis()
        )

        assertEquals("user123", user.id)
        assertEquals("user@example.com", user.email)
        assertEquals("hashedPassword", user.password)
        assertEquals(UserType.DOCTOR, user.userType)
        assertTrue(user.createdAt > 0)
    }


    @Test
    fun `Service classes should be properly initialized`() {
        assertNotNull(authService)
        assertNotNull(momService)
        assertNotNull(doctorService)
        assertNotNull(gson)
    }

    @Test
    fun `Mock repositories should be properly initialized`() {
        assertNotNull(mockUserRepository)
        assertNotNull(mockHashingService)
        assertNotNull(mockRefreshTokenRepository)
        assertNotNull(mockAuthConfig)
        assertNotNull(mockMomRepository)
        assertNotNull(mockDoctorRepository)
        assertNotNull(mockNidRepository)
        assertNotNull(mockTransactionService)
    }

    @Test
    fun `UserType enum should have correct values`() {
        assertEquals("MOM", UserType.MOM.name)
        assertEquals("DOCTOR", UserType.DOCTOR.name)
        assertEquals("ADMIN", UserType.ADMIN.name)
    }

}

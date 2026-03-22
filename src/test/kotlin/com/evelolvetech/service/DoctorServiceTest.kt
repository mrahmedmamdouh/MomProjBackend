package com.evelolvetech.service

import com.evelolvetech.data.models.*
import com.evelolvetech.data.requests.RegisterDoctorMultipartRequest
import com.evelolvetech.data.requests.UpdateDoctorMultipartRequest
import com.evelolvetech.data.requests.UpdateDoctorRequest
import com.evelolvetech.mocks.*
import kotlinx.coroutines.runBlocking
import com.evelolvetech.service.doctor.DoctorService
import kotlin.test.*

class DoctorServiceTest {

    private val mockDoctorRepository = MockDoctorRepository()
    private val mockNidRepository = MockNidRepository()
    private val mockUserRepository = MockUserRepository()
    private val mockHashingService = MockHashingService()
    private val mockTransactionService = MockTransactionService()
    private val doctorService = DoctorService(
        mockDoctorRepository,
        mockNidRepository,
        mockUserRepository,
        mockHashingService,
        mockTransactionService
    )

    @Test
    fun testCreateDoctorMultipartSuccess() = runBlocking {
        val request = RegisterDoctorMultipartRequest(
            email = "doctor@example.com",
            password = "password123",
            name = "Dr. Test",
            phone = "123-456-7890",
            specialization = "PSYCHIATRIST"
        )

        val filePaths = UploadedFilePaths("front.jpg", "back.jpg", "photo.jpg")
        val result = doctorService.createDoctorMultipart(request, filePaths)

        assertNotNull(result)
        assertEquals(1, mockNidRepository.createdNids.size)
        assertEquals(1, mockDoctorRepository.createdDoctors.size)
        assertEquals(1, mockDoctorRepository.createdDoctorAuths.size)
        assertEquals(1, mockUserRepository.createdUsers.size)

        val createdDoctor = mockDoctorRepository.createdDoctors.first()
        assertEquals("Dr. Test", createdDoctor.name)
        assertEquals("doctor@example.com", createdDoctor.email)
        assertEquals("PSYCHIATRIST", createdDoctor.specialization)
    }

    @Test
    fun testGetDoctorByIdExists() = runBlocking {
        val doctor = Doctor(
            id = "test-id",
            authUid = "auth-uid",
            name = "Dr. Test",
            email = "doctor@example.com",
            phone = "123-456-7890",
            specialization = "PSYCHIATRIST",
            photo = "test-photo.jpg",
            nidId = "nid-123",
            nidRef = "/nids/nid-123"
        )
        mockDoctorRepository.doctors["test-id"] = doctor

        val result = doctorService.getDoctorById("test-id")

        assertNotNull(result)
        assertEquals("Dr. Test", result.name)
    }

    @Test
    fun testGetDoctorByIdNotExists() = runBlocking {
        val result = doctorService.getDoctorById("non-existent-id")
        assertNull(result)
    }

    @Test
    fun testGetAllDoctors() = runBlocking {
        val doctor1 = Doctor(
            id = "doc1",
            authUid = "auth1",
            name = "Dr. One",
            email = "one@example.com",
            phone = "111-111-1111",
            specialization = "PSYCHIATRIST",
            photo = "",
            nidId = "nid1",
            nidRef = "/nids/nid1"
        )
        val doctor2 = Doctor(
            id = "doc2",
            authUid = "auth2",
            name = "Dr. Two",
            email = "two@example.com",
            phone = "222-222-2222",
            specialization = "CLINICAL_PSYCHOLOGIST",
            photo = "",
            nidId = "nid2",
            nidRef = "/nids/nid2"
        )

        mockDoctorRepository.doctors["doc1"] = doctor1
        mockDoctorRepository.doctors["doc2"] = doctor2

        val result = doctorService.getAllDoctors()

        assertEquals(2, result.size)
        assertTrue(result.any { it.name == "Dr. One" })
        assertTrue(result.any { it.name == "Dr. Two" })
    }

    @Test
    fun testGetAuthorizedDoctors() = runBlocking {
        val doctor1 = Doctor(
            id = "doc1",
            authUid = "auth1",
            name = "Dr. One",
            email = "one@example.com",
            phone = "111-111-1111",
            specialization = "PSYCHIATRIST",
            isAuthorized = true,
            photo = "",
            nidId = "nid1",
            nidRef = "/nids/nid1"
        )
        val doctor2 = Doctor(
            id = "doc2",
            authUid = "auth2",
            name = "Dr. Two",
            email = "two@example.com",
            phone = "222-222-2222",
            specialization = "CLINICAL_PSYCHOLOGIST",
            isAuthorized = false,
            photo = "",
            nidId = "nid2",
            nidRef = "/nids/nid2"
        )

        mockDoctorRepository.doctors["doc1"] = doctor1
        mockDoctorRepository.doctors["doc2"] = doctor2

        val result = doctorService.getAuthorizedDoctors()

        assertEquals(1, result.size)
        assertEquals("Dr. One", result.first().name)
    }

    @Test
    fun testUpdateDoctorSuccess() = runBlocking {
        val request = UpdateDoctorRequest(
            name = "Dr. Updated",
            phone = "987-654-3210",
            specialization = "FAMILY_THERAPIST"
        )

        mockDoctorRepository.updateResult = true

        val result = doctorService.updateDoctor("test-id", request)

        assertNotNull(result)
        assertEquals(1, mockDoctorRepository.updateCalls.size)
        assertEquals("test-id", mockDoctorRepository.updateCalls.first().first)
    }

    @Test
    fun testUpdateDoctorMultipartSuccess() = runBlocking {
        val request = UpdateDoctorMultipartRequest(
            name = "Dr. Updated",
            phone = "987-654-3210",
            specialization = "FAMILY_THERAPIST",
            photo = null
        )

        mockDoctorRepository.updateResult = true

        val result = doctorService.updateDoctorMultipart("test-id", request, "new-photo.jpg")

        assertNotNull(result)
        assertEquals(1, mockDoctorRepository.updateCalls.size)
    }

    @Test
    fun testValidateCreateDoctorMultipartRequestSuccess() {
        val validRequest = RegisterDoctorMultipartRequest(
            email = "valid@example.com",
            password = "password123",
            name = "Dr. Valid",
            phone = "123-456-7890",
            specialization = "PSYCHIATRIST"
        )

        val result = doctorService.validateCreateDoctorMultipartRequest(validRequest)

        assertEquals(DoctorService.ValidationEvent.Success, result)
    }

    @Test
    fun testValidateCreateDoctorMultipartRequestBlankField() {
        val invalidRequest = RegisterDoctorMultipartRequest(
            email = "",
            password = "password123",
            name = "Dr. Valid",
            phone = "123-456-7890",
            specialization = "PSYCHIATRIST"
        )

        val result = doctorService.validateCreateDoctorMultipartRequest(invalidRequest)

        assertEquals(DoctorService.ValidationEvent.ErrorFieldEmpty, result)
    }

    @Test
    fun testValidateCreateDoctorMultipartRequestInvalidEmail() {
        val invalidRequest = RegisterDoctorMultipartRequest(
            email = "invalid-email",
            password = "password123",
            name = "Dr. Valid",
            phone = "123-456-7890",
            specialization = "PSYCHIATRIST"
        )

        val result = doctorService.validateCreateDoctorMultipartRequest(invalidRequest)

        assertEquals(DoctorService.ValidationEvent.ErrorInvalidEmail, result)
    }

    @Test
    fun testValidateCreateDoctorMultipartRequestShortPassword() {
        val invalidRequest = RegisterDoctorMultipartRequest(
            email = "valid@example.com",
            password = "12345",
            name = "Dr. Valid",
            phone = "123-456-7890",
            specialization = "PSYCHIATRIST"
        )

        val result = doctorService.validateCreateDoctorMultipartRequest(invalidRequest)

        assertEquals(DoctorService.ValidationEvent.ErrorPasswordTooShort, result)
    }

    @Test
    fun testIsValidPassword() = runBlocking {
        val password = "testPassword"
        val hashedPassword = "hash123:salt456"

        mockHashingService.verifyResult = true

        val result = doctorService.isValidPassword(password, hashedPassword)

        assertNotNull(result)
        assertEquals(1, mockHashingService.verifyCalls.size)
        assertEquals(password, mockHashingService.verifyCalls.first().first)
    }

    @Test
    fun testIsValidPasswordInvalidFormat() = runBlocking {
        val password = "testPassword"
        val invalidHashedPassword = "invalidformat"

        val result = doctorService.isValidPassword(password, invalidHashedPassword)

        assertFalse(result)
    }
}

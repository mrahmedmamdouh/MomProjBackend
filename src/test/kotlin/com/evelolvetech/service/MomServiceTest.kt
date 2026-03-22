package com.evelolvetech.service

import com.evelolvetech.data.models.*
import com.evelolvetech.data.requests.RegisterMomMultipartRequest
import com.evelolvetech.data.requests.UpdateMomMultipartRequest
import com.evelolvetech.data.requests.UpdateMomRequest
import com.evelolvetech.mocks.*
import kotlinx.coroutines.runBlocking
import com.evelolvetech.service.mom.MomService
import kotlin.test.*

class MomServiceTest {

    private val mockMomRepository = MockMomRepository()
    private val mockNidRepository = MockNidRepository()
    private val mockUserRepository = MockUserRepository()
    private val mockHashingService = MockHashingService()

    private val mockTransactionService = MockTransactionService()

    private fun testAuthConfig() = com.evelolvetech.util.AuthConfig(
        accessTokenExpiryMinutes = 30L,
        refreshTokenExpiryDays = 30L,
        idleTimeoutHours = 24L,
        momAuthorizationSessionThreshold = 8
    )

    private val mockAuthConfig = testAuthConfig()
    
    private val momService = MomService(
        mockMomRepository,
        mockNidRepository,
        mockUserRepository,
        mockHashingService,
        mockTransactionService,
        mockAuthConfig
    )

    @Test
    fun testCreateMomMultipartSuccess() = runBlocking {
        val request = RegisterMomMultipartRequest(
            email = "test@example.com",
            password = "password123",
            fullName = "Test Mom",
            phone = "123-456-7890",
            maritalStatus = "SINGLE"
        )

        val filePaths = UploadedFilePaths("front.jpg", "back.jpg", "photo.jpg")
        val result = momService.createMomMultipart(request, filePaths)

        assertNotNull(result)
        assertEquals(1, mockNidRepository.createdNids.size)
        assertEquals(1, mockMomRepository.createdMoms.size)
        assertEquals(1, mockMomRepository.createdMomAuths.size)
        assertEquals(1, mockUserRepository.createdUsers.size)

        val createdMom = mockMomRepository.createdMoms.first()
        assertEquals("Test Mom", createdMom.fullName)
        assertEquals("test@example.com", createdMom.email)
        assertEquals("SINGLE", createdMom.maritalStatus)
    }

    @Test
    fun testGetMomByIdExists() = runBlocking {
        val mom = Mom(
            id = "test-id",
            authUid = "auth-uid",
            fullName = "Test Mom",
            email = "test@example.com",
            phone = "123-456-7890",
            maritalStatus = "SINGLE",
            photoUrl = "test-photo.jpg",
            nidId = "nid-123",
            nidRef = "/nids/nid-123"
        )
        mockMomRepository.moms["test-id"] = mom

        val result = momService.getMomById("test-id")

        assertNotNull(result)
        assertEquals("Test Mom", result.fullName)
    }

    @Test
    fun testGetMomByIdNotExists() = runBlocking {
        val result = momService.getMomById("non-existent-id")
        assertNull(result)
    }

    @Test
    fun testUpdateMomSuccess() = runBlocking {
        val request = UpdateMomRequest(
            fullName = "Updated Name",
            phone = "987-654-3210",
            maritalStatus = "MARRIED"
        )

        mockMomRepository.updateResult = true

        val result = momService.updateMom("test-id", request)

        assertNotNull(result)
        assertEquals(1, mockMomRepository.updateCalls.size)
        assertEquals("test-id", mockMomRepository.updateCalls.first().first)
    }

    @Test
    fun testUpdateMomMultipartSuccess() = runBlocking {
        val request = UpdateMomMultipartRequest(
            fullName = "Updated Name",
            phone = "987-654-3210",
            maritalStatus = "MARRIED",
            photo = null
        )

        mockMomRepository.updateResult = true

        val result = momService.updateMomMultipart("test-id", request, "new-photo.jpg")

        assertNotNull(result)
        assertEquals(1, mockMomRepository.updateCalls.size)
    }

    @Test
    fun testUpdateMomSessionsSuccess() = runBlocking {
        mockMomRepository.updateSessionsResult = true

        val result = momService.updateMomSessions("test-id", 10)

        assertNotNull(result)
        assertEquals(1, mockMomRepository.updateSessionsCalls.size)
        assertEquals("test-id", mockMomRepository.updateSessionsCalls.first().first)
        assertEquals(10, mockMomRepository.updateSessionsCalls.first().second)
    }

    @Test
    fun testValidateCreateMomMultipartRequestSuccess() {
        val validRequest = RegisterMomMultipartRequest(
            email = "valid@example.com",
            password = "password123",
            fullName = "Valid Mom",
            phone = "123-456-7890",
            maritalStatus = "SINGLE"
        )

        val result = momService.validateCreateMomMultipartRequest(validRequest)

        assertEquals(MomService.ValidationEvent.Success, result)
    }

    @Test
    fun testValidateCreateMomMultipartRequestBlankField() {
        val invalidRequest = RegisterMomMultipartRequest(
            email = "",
            password = "password123",
            fullName = "Valid Mom",
            phone = "123-456-7890",
            maritalStatus = "SINGLE"
        )

        val result = momService.validateCreateMomMultipartRequest(invalidRequest)

        assertEquals(MomService.ValidationEvent.ErrorFieldEmpty, result)
    }

    @Test
    fun testValidateCreateMomMultipartRequestInvalidEmail() {
        val invalidRequest = RegisterMomMultipartRequest(
            email = "invalid-email",
            password = "password123",
            fullName = "Valid Mom",
            phone = "123-456-7890",
            maritalStatus = "SINGLE"
        )

        val result = momService.validateCreateMomMultipartRequest(invalidRequest)

        assertEquals(MomService.ValidationEvent.ErrorInvalidEmail, result)
    }

    @Test
    fun testValidateCreateMomMultipartRequestShortPassword() {
        val invalidRequest = RegisterMomMultipartRequest(
            email = "valid@example.com",
            password = "12345",
            fullName = "Valid Mom",
            phone = "123-456-7890",
            maritalStatus = "SINGLE"
        )

        val result = momService.validateCreateMomMultipartRequest(invalidRequest)

        assertEquals(MomService.ValidationEvent.ErrorPasswordTooShort, result)
    }

    @Test
    fun testIsValidPassword() = runBlocking {
        val password = "testPassword"
        val hashedPassword = "hash123:salt456"

        mockHashingService.verifyResult = true

        val result = momService.isValidPassword(password, hashedPassword)

        assertNotNull(result)
        assertEquals(1, mockHashingService.verifyCalls.size)
        assertEquals(password, mockHashingService.verifyCalls.first().first)
    }

    @Test
    fun testIsValidPasswordInvalidFormat() = runBlocking {
        val password = "testPassword"
        val invalidHashedPassword = "invalidformat"

        val result = momService.isValidPassword(password, invalidHashedPassword)

        assertFalse(result)
    }

    @Test
    fun testMomAuthorizationBelowThreshold() = runBlocking {
        val momId = "test_mom_1"
        val mom = Mom(
            id = momId,
            authUid = "auth_uid_1",
            fullName = "Test Mom",
            email = "test@example.com",
            phone = "123-456-7890",
            maritalStatus = "SINGLE",
            photoUrl = "photo.jpg",
            numberOfSessions = 5,
            isAuthorized = false,
            nidId = "nid_1",
            nidRef = "/nids/nid_1"
        )
        mockMomRepository.moms[momId] = mom
        mockMomRepository.updateSessionsResult = true

        val result = momService.updateMomSessions(momId, 5)

        assertTrue(result)
        val updatedMom = mockMomRepository.moms[momId]
        assertEquals(5, updatedMom?.numberOfSessions)
        assertFalse(updatedMom?.isAuthorized ?: true)
    }

    @Test
    fun testMomAuthorizationAtThreshold() = runBlocking {
        val momId = "test_mom_2"
        val mom = Mom(
            id = momId,
            authUid = "auth_uid_2",
            fullName = "Test Mom",
            email = "test2@example.com",
            phone = "123-456-7890",
            maritalStatus = "SINGLE",
            photoUrl = "photo.jpg",
            numberOfSessions = 7,
            isAuthorized = false,
            nidId = "nid_2",
            nidRef = "/nids/nid_2"
        )
        mockMomRepository.moms[momId] = mom
        mockMomRepository.updateSessionsResult = true

        val result = momService.updateMomSessions(momId, 8)

        assertTrue(result)
        val updatedMom = mockMomRepository.moms[momId]
        assertEquals(8, updatedMom?.numberOfSessions)
        assertTrue(updatedMom?.isAuthorized ?: false)
    }

    @Test
    fun testMomAuthorizationAboveThreshold() = runBlocking {
        val momId = "test_mom_3"
        val mom = Mom(
            id = momId,
            authUid = "auth_uid_3",
            fullName = "Test Mom",
            email = "test3@example.com",
            phone = "123-456-7890",
            maritalStatus = "SINGLE",
            photoUrl = "photo.jpg",
            numberOfSessions = 10,
            isAuthorized = true,
            nidId = "nid_3",
            nidRef = "/nids/nid_3"
        )
        mockMomRepository.moms[momId] = mom
        mockMomRepository.updateSessionsResult = true

        val result = momService.updateMomSessions(momId, 12)

        assertTrue(result)
        val updatedMom = mockMomRepository.moms[momId]
        assertEquals(12, updatedMom?.numberOfSessions)
        assertTrue(updatedMom?.isAuthorized ?: false)
    }

    @Test
    fun testGetMomByIdForAuthorizationCheck() = runBlocking {
        val momId = "test_mom_auth_check"
        val authorizedMom = Mom(
            id = momId,
            authUid = "auth_uid_check",
            fullName = "Authorized Mom",
            email = "authorized@example.com",
            phone = "123-456-7890",
            maritalStatus = "SINGLE",
            photoUrl = "photo.jpg",
            numberOfSessions = 10,
            isAuthorized = true,
            nidId = "nid_check",
            nidRef = "/nids/nid_check"
        )
        mockMomRepository.moms[momId] = authorizedMom

        val result = momService.getMomById(momId)

        assertNotNull(result)
        assertEquals(momId, result.id)
        assertEquals("Authorized Mom", result.fullName)
        assertEquals(10, result.numberOfSessions)
        assertTrue(result.isAuthorized)
    }

    @Test
    fun testGetMomByIdForAuthorizationCheckUnauthorized() = runBlocking {
        val momId = "test_mom_unauth_check"
        val unauthorizedMom = Mom(
            id = momId,
            authUid = "auth_uid_unauth",
            fullName = "Unauthorized Mom",
            email = "unauthorized@example.com",
            phone = "123-456-7890",
            maritalStatus = "SINGLE",
            photoUrl = "photo.jpg",
            numberOfSessions = 3,
            isAuthorized = false,
            nidId = "nid_unauth",
            nidRef = "/nids/nid_unauth"
        )
        mockMomRepository.moms[momId] = unauthorizedMom

        val result = momService.getMomById(momId)

        assertNotNull(result)
        assertEquals(momId, result.id)
        assertEquals("Unauthorized Mom", result.fullName)
        assertEquals(3, result.numberOfSessions)
        assertFalse(result.isAuthorized)
    }

    @Test
    fun testGetMomByIdNotFound() = runBlocking {
        val result = momService.getMomById("non_existent_mom")
        assertNull(result)
    }
}


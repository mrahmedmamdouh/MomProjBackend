package com.evelolvetech.service

import com.evelolvetech.data.models.*
import com.evelolvetech.data.repository.api.auth.RefreshTokenRepository
import com.evelolvetech.data.repository.api.auth.UserRepository
import com.evelolvetech.data.requests.RefreshTokenRequest
import com.evelolvetech.data.requests.UnifiedLoginRequest
import com.evelolvetech.util.HashingService
import com.evelolvetech.util.SaltedHash
import com.evelolvetech.util.AuthConfig
import com.evelolvetech.service.auth.AuthService
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.*

class AuthServiceTest {

    private lateinit var authService: AuthService
    private lateinit var mockUserRepository: UserRepository
    private lateinit var mockHashingService: HashingService
    private lateinit var mockRefreshTokenRepository: RefreshTokenRepository
    private lateinit var mockAuthConfig: AuthConfig
    
    private val jwtIssuer = "https://jwt-provider-domain/"
    private val jwtAudience = "jwt-audience"
    private val jwtSecret = "test-secret"

    @BeforeEach
    fun setUp() {
        mockUserRepository = mockk()
        mockHashingService = mockk()
        mockRefreshTokenRepository = mockk()
        mockAuthConfig = mockk()
        
        every { mockAuthConfig.accessTokenExpiryMinutes } returns 30L
        every { mockAuthConfig.refreshTokenExpirySeconds } returns 2592000L
        every { mockAuthConfig.idleTimeoutSeconds } returns 86400L
        every { mockAuthConfig.accessTokenExpiryMs } returns 1800000L
        
        authService = AuthService(mockUserRepository, mockHashingService, mockRefreshTokenRepository, mockAuthConfig)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `login should return AuthResponse when credentials are valid`() = runBlocking {
        val email = "test@example.com"
        val password = "password123"
        val request = UnifiedLoginRequest(email, password)
        
        val mockUser = User(
            id = "user123",
            email = email,
            password = "hashedPassword:salt123",
            userType = UserType.MOM,
            momId = "mom123"
        )

        coEvery { mockUserRepository.getUserByEmail(email) } returns mockUser
        coEvery { mockHashingService.verify(password, SaltedHash("hashedPassword", "salt123")) } returns true
        coEvery { mockRefreshTokenRepository.revokeAllUserTokens("user123") } returns true
        coEvery { mockRefreshTokenRepository.createRefreshToken(any()) } returns true

        val result = authService.login(request, jwtIssuer, jwtAudience, jwtSecret)

        assertNotNull(result)
        assertEquals("mom123", result.userId)
        assertEquals("MOM", result.userType)
        assertEquals(1800L, result.expiresIn)
        assertEquals(2592000L, result.refreshExpiresIn)
        assertNotNull(result.token)
        assertNotNull(result.refreshToken)
        
        coVerify { mockUserRepository.getUserByEmail(email) }
        coVerify { mockHashingService.verify(password, SaltedHash("hashedPassword", "salt123")) }
        coVerify { mockRefreshTokenRepository.revokeAllUserTokens("mom123") }
        coVerify { mockRefreshTokenRepository.createRefreshToken(any()) }
    }

    @Test
    fun `login should return null when user not found`() = runBlocking {
        val email = "nonexistent@example.com"
        val password = "password123"
        val request = UnifiedLoginRequest(email, password)

        coEvery { mockUserRepository.getUserByEmail(email) } returns null

        val result = authService.login(request, jwtIssuer, jwtAudience, jwtSecret)

        assertNull(result)
        coVerify { mockUserRepository.getUserByEmail(email) }
        coVerify(exactly = 0) { mockHashingService.verify(any(), any()) }
    }

    @Test
    fun `login should return null when password is invalid`() = runBlocking {
        val email = "test@example.com"
        val password = "wrongpassword"
        val request = UnifiedLoginRequest(email, password)
        
        val mockUser = User(
            id = "user123",
            email = email,
            password = "hashedPassword:salt123",
            userType = UserType.MOM,
            momId = "mom123"
        )

        coEvery { mockUserRepository.getUserByEmail(email) } returns mockUser
        coEvery { mockHashingService.verify(password, SaltedHash("hashedPassword", "salt123")) } returns false

        val result = authService.login(request, jwtIssuer, jwtAudience, jwtSecret)

        assertNull(result)
        coVerify { mockUserRepository.getUserByEmail(email) }
        coVerify { mockHashingService.verify(password, SaltedHash("hashedPassword", "salt123")) }
    }

    @Test
    fun `refreshToken should return new tokens when refresh token is valid`() = runBlocking {
        val userId = "user123"
        val userType = "MOM"
        val email = "test@example.com"
        val refreshTokenString = "valid-refresh-token"
        val request = RefreshTokenRequest(refreshTokenString)
        
        val mockRefreshToken = RefreshToken(
            token = refreshTokenString,
            userId = userId,
            userType = userType,
            email = email,
            expiresAt = System.currentTimeMillis() / 1000L + 2592000,
            lastUsedAt = System.currentTimeMillis() / 1000L
        )

        coEvery { mockRefreshTokenRepository.isTokenValid(refreshTokenString, 86400) } returns true
        coEvery { mockRefreshTokenRepository.getRefreshToken(refreshTokenString) } returns mockRefreshToken
        coEvery { mockRefreshTokenRepository.updateLastUsed(any(), any()) } returns true
        coEvery { mockRefreshTokenRepository.revokeToken(refreshTokenString) } returns true
        coEvery { mockRefreshTokenRepository.createRefreshToken(any()) } returns true

        val result = authService.refreshToken(request, jwtIssuer, jwtAudience, jwtSecret)

        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(userType, result.userType)
        assertEquals(1800L, result.expiresIn)
        assertEquals(2592000L, result.refreshExpiresIn)
        assertNotNull(result.token)
        assertNotNull(result.refreshToken)
        
        coVerify { mockRefreshTokenRepository.isTokenValid(refreshTokenString, 86400) }
        coVerify { mockRefreshTokenRepository.getRefreshToken(refreshTokenString) }
        coVerify { mockRefreshTokenRepository.updateLastUsed(any(), any()) }
        coVerify { mockRefreshTokenRepository.revokeToken(refreshTokenString) }
        coVerify { mockRefreshTokenRepository.createRefreshToken(any()) }
    }

    @Test
    fun `refreshToken should return null when refresh token is invalid`() = runBlocking {
        val refreshTokenString = "invalid-refresh-token"
        val request = RefreshTokenRequest(refreshTokenString)

        coEvery { mockRefreshTokenRepository.isTokenValid(refreshTokenString, 86400) } returns false

        val result = authService.refreshToken(request, jwtIssuer, jwtAudience, jwtSecret)

        assertNull(result)
        
        coVerify { mockRefreshTokenRepository.isTokenValid(refreshTokenString, 86400) }
        coVerify(exactly = 0) { mockRefreshTokenRepository.getRefreshToken(any()) }
    }

    @Test
    fun `refreshToken should return null when refresh token not found`() = runBlocking {
        val refreshTokenString = "valid-but-not-found-token"
        val request = RefreshTokenRequest(refreshTokenString)

        coEvery { mockRefreshTokenRepository.isTokenValid(refreshTokenString, 86400) } returns true
        coEvery { mockRefreshTokenRepository.getRefreshToken(refreshTokenString) } returns null

        val result = authService.refreshToken(request, jwtIssuer, jwtAudience, jwtSecret)

        assertNull(result)
        
        coVerify { mockRefreshTokenRepository.isTokenValid(refreshTokenString, 86400) }
        coVerify { mockRefreshTokenRepository.getRefreshToken(refreshTokenString) }
    }

    @Test
    fun `logout should revoke refresh token successfully`() = runBlocking {
        val refreshTokenString = "token-to-logout"

        coEvery { mockRefreshTokenRepository.revokeToken(refreshTokenString) } returns true

        val result = authService.logout(refreshTokenString)

        assertTrue(result)
        coVerify { mockRefreshTokenRepository.revokeToken(refreshTokenString) }
    }

    @Test
    fun `logout should return false when revocation fails`() = runBlocking {
        val refreshTokenString = "token-to-logout"

        coEvery { mockRefreshTokenRepository.revokeToken(refreshTokenString) } returns false

        val result = authService.logout(refreshTokenString)

        assertFalse(result)
        coVerify { mockRefreshTokenRepository.revokeToken(refreshTokenString) }
    }

    @Test
    fun `login should return AuthResponse for admin user with valid credentials`() = runBlocking {
        val email = "admin@momcare.com"
        val password = "admin123"
        val request = UnifiedLoginRequest(email, password)
        
        val mockUser = User(
            id = "admin_main",
            email = email,
            password = "hashedPassword:salt123",
            userType = UserType.ADMIN
        )

        coEvery { mockUserRepository.getUserByEmail(email) } returns mockUser
        coEvery { mockHashingService.verify(password, SaltedHash("hashedPassword", "salt123")) } returns true
        coEvery { mockRefreshTokenRepository.revokeAllUserTokens("admin_main") } returns true
        coEvery { mockRefreshTokenRepository.createRefreshToken(any()) } returns true

        val result = authService.login(request, jwtIssuer, jwtAudience, jwtSecret)

        assertNotNull(result)
        assertEquals("admin_main", result.userId)
        assertEquals("ADMIN", result.userType)
        assertEquals(1800L, result.expiresIn)
        assertEquals(2592000L, result.refreshExpiresIn)
        assertNotNull(result.token)
        assertNotNull(result.refreshToken)
        
        coVerify { mockUserRepository.getUserByEmail(email) }
        coVerify { mockHashingService.verify(password, SaltedHash("hashedPassword", "salt123")) }
        coVerify { mockRefreshTokenRepository.revokeAllUserTokens("admin_main") }
        coVerify { mockRefreshTokenRepository.createRefreshToken(any()) }
    }

    @Test
    fun `login should return AuthResponse for doctor user with valid credentials`() = runBlocking {
        val email = "doctor@example.com"
        val password = "doctor123"
        val request = UnifiedLoginRequest(email, password)
        
        val mockUser = User(
            id = "user123",
            email = email,
            password = "hashedPassword:salt123",
            userType = UserType.DOCTOR,
            doctorId = "doc123"
        )

        coEvery { mockUserRepository.getUserByEmail(email) } returns mockUser
        coEvery { mockHashingService.verify(password, SaltedHash("hashedPassword", "salt123")) } returns true
        coEvery { mockRefreshTokenRepository.revokeAllUserTokens("doc123") } returns true
        coEvery { mockRefreshTokenRepository.createRefreshToken(any()) } returns true

        val result = authService.login(request, jwtIssuer, jwtAudience, jwtSecret)

        assertNotNull(result)
        assertEquals("doc123", result.userId)
        assertEquals("DOCTOR", result.userType)
        assertEquals(1800L, result.expiresIn)
        assertEquals(2592000L, result.refreshExpiresIn)
        assertNotNull(result.token)
        assertNotNull(result.refreshToken)
        
        coVerify { mockUserRepository.getUserByEmail(email) }
        coVerify { mockHashingService.verify(password, SaltedHash("hashedPassword", "salt123")) }
        coVerify { mockRefreshTokenRepository.revokeAllUserTokens("doc123") }
        coVerify { mockRefreshTokenRepository.createRefreshToken(any()) }
    }

    @Test
    fun `refreshToken should work correctly for admin user`() = runBlocking {
        val userId = "admin_main"
        val userType = "ADMIN"
        val email = "admin@momcare.com"
        val refreshTokenString = "valid-admin-refresh-token"
        val request = RefreshTokenRequest(refreshTokenString)
        
        val mockRefreshToken = RefreshToken(
            token = refreshTokenString,
            userId = userId,
            userType = userType,
            email = email,
            expiresAt = System.currentTimeMillis() / 1000L + 2592000,
            lastUsedAt = System.currentTimeMillis() / 1000L
        )

        coEvery { mockRefreshTokenRepository.isTokenValid(refreshTokenString, 86400) } returns true
        coEvery { mockRefreshTokenRepository.getRefreshToken(refreshTokenString) } returns mockRefreshToken
        coEvery { mockRefreshTokenRepository.updateLastUsed(any(), any()) } returns true
        coEvery { mockRefreshTokenRepository.revokeToken(refreshTokenString) } returns true
        coEvery { mockRefreshTokenRepository.createRefreshToken(any()) } returns true

        val result = authService.refreshToken(request, jwtIssuer, jwtAudience, jwtSecret)

        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(userType, result.userType)
        assertEquals(1800L, result.expiresIn)
        assertEquals(2592000L, result.refreshExpiresIn)
        assertNotNull(result.token)
        assertNotNull(result.refreshToken)
        
        coVerify { mockRefreshTokenRepository.isTokenValid(refreshTokenString, 86400) }
        coVerify { mockRefreshTokenRepository.getRefreshToken(refreshTokenString) }
        coVerify { mockRefreshTokenRepository.updateLastUsed(any(), any()) }
        coVerify { mockRefreshTokenRepository.revokeToken(refreshTokenString) }
        coVerify { mockRefreshTokenRepository.createRefreshToken(any()) }
    }
}

package com.evelolvetech.util

import io.ktor.server.config.*
import kotlin.test.*

class AuthConfigTest {

    @Test
    fun `AuthConfig should have correct default values`() {
        val authConfig = AuthConfig()

        assertEquals(30L, authConfig.accessTokenExpiryMinutes)
        assertEquals(30L, authConfig.refreshTokenExpiryDays)
        assertEquals(24L, authConfig.idleTimeoutHours)
        assertEquals(8, authConfig.momAuthorizationSessionThreshold)
        assertEquals(5L, authConfig.momAuthCacheDurationMinutes)
    }

    @Test
    fun `AuthConfig should calculate expiry times correctly`() {
        val authConfig = AuthConfig()

        assertEquals(30L * 60 * 1000L, authConfig.accessTokenExpiryMs)
        assertEquals(30L * 24 * 60 * 60L, authConfig.refreshTokenExpirySeconds)
        assertEquals(24L * 60 * 60L, authConfig.idleTimeoutSeconds)
    }

    @Test
    fun `AuthConfig should create from application config with default values`() {
        val config = ApplicationConfig("test")
        val authConfig = AuthConfig.fromApplicationConfig(config)

        assertEquals(30L, authConfig.accessTokenExpiryMinutes)
        assertEquals(30L, authConfig.refreshTokenExpiryDays)
        assertEquals(24L, authConfig.idleTimeoutHours)
        assertEquals(8, authConfig.momAuthorizationSessionThreshold)
        assertEquals(5L, authConfig.momAuthCacheDurationMinutes)
    }

    @Test
    fun `AuthConfig should create from application config with custom values`() {
        val config = ApplicationConfig("test")
        val authConfig = AuthConfig.fromApplicationConfig(config)

        assertEquals(30L, authConfig.accessTokenExpiryMinutes)
        assertEquals(30L, authConfig.refreshTokenExpiryDays)
        assertEquals(24L, authConfig.idleTimeoutHours)
        assertEquals(8, authConfig.momAuthorizationSessionThreshold)
        assertEquals(5L, authConfig.momAuthCacheDurationMinutes)
    }

    @Test
    fun `AuthConfig should be immutable`() {
        val authConfig = AuthConfig()
        val originalAccessTokenExpiry = authConfig.accessTokenExpiryMinutes
        val originalRefreshTokenExpiry = authConfig.refreshTokenExpiryDays
        val originalIdleTimeout = authConfig.idleTimeoutHours
        val originalSessionThreshold = authConfig.momAuthorizationSessionThreshold
        val originalCacheDuration = authConfig.momAuthCacheDurationMinutes

        assertEquals(originalAccessTokenExpiry, authConfig.accessTokenExpiryMinutes)
        assertEquals(originalRefreshTokenExpiry, authConfig.refreshTokenExpiryDays)
        assertEquals(originalIdleTimeout, authConfig.idleTimeoutHours)
        assertEquals(originalSessionThreshold, authConfig.momAuthorizationSessionThreshold)
        assertEquals(originalCacheDuration, authConfig.momAuthCacheDurationMinutes)
    }

    @Test
    fun `AuthConfig should have reasonable time values`() {
        val authConfig = AuthConfig()

        assertTrue(authConfig.accessTokenExpiryMinutes > 0)
        assertTrue(authConfig.refreshTokenExpiryDays > 0)
        assertTrue(authConfig.idleTimeoutHours > 0)
        assertTrue(authConfig.momAuthorizationSessionThreshold > 0)
        assertTrue(authConfig.momAuthCacheDurationMinutes > 0)

        assertTrue(authConfig.accessTokenExpiryMs > 0)
        assertTrue(authConfig.refreshTokenExpirySeconds > 0)
        assertTrue(authConfig.idleTimeoutSeconds > 0)
    }
}

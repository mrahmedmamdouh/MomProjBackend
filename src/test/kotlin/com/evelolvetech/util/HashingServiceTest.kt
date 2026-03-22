package com.evelolvetech.util

import kotlin.test.*

class HashingServiceTest {

    private val hashingService = SHA256HashingService()

    @Test
    fun testGenerateSaltedHash() {
        val password = "testPassword123"
        val saltedHash = hashingService.generateSaltedHash(password)

        assertNotNull(saltedHash.hash)
        assertNotNull(saltedHash.salt)
        assertTrue(saltedHash.hash.isNotBlank())
        assertTrue(saltedHash.salt.isNotBlank())
        assertEquals(64, saltedHash.salt.length) // 32 bytes = 64 hex chars
        assertEquals(64, saltedHash.hash.length) // 32 bytes = 64 hex chars
    }

    @Test
    fun testVerifyCorrectPassword() {
        val password = "testPassword123"
        val saltedHash = hashingService.generateSaltedHash(password)

        val isValid = hashingService.verify(password, saltedHash)
        assertTrue(isValid)
    }

    @Test
    fun testVerifyIncorrectPassword() {
        val password = "testPassword123"
        val wrongPassword = "wrongPassword456"
        val saltedHash = hashingService.generateSaltedHash(password)

        val isValid = hashingService.verify(wrongPassword, saltedHash)
        assertFalse(isValid)
    }

    @Test
    fun testDifferentPasswordsGenerateDifferentHashes() {
        val password1 = "password1"
        val password2 = "password2"

        val hash1 = hashingService.generateSaltedHash(password1)
        val hash2 = hashingService.generateSaltedHash(password2)

        assertNotEquals(hash1.hash, hash2.hash)
        assertNotEquals(hash1.salt, hash2.salt)
    }

    @Test
    fun testSamePasswordGeneratesDifferentSalts() {
        val password = "samePassword"

        val hash1 = hashingService.generateSaltedHash(password)
        val hash2 = hashingService.generateSaltedHash(password)

        assertNotEquals(hash1.salt, hash2.salt)
        assertNotEquals(hash1.hash, hash2.hash)
    }

    @Test
    fun testEmptyPasswordHandling() {
        val emptyPassword = ""
        val saltedHash = hashingService.generateSaltedHash(emptyPassword)

        assertNotNull(saltedHash.hash)
        assertNotNull(saltedHash.salt)

        val isValid = hashingService.verify(emptyPassword, saltedHash)
        assertTrue(isValid)
    }

    @Test
    fun testSpecialCharactersInPassword() {
        val specialPassword = "P@ssw0rd!@#$%^&*()"
        val saltedHash = hashingService.generateSaltedHash(specialPassword)

        val isValid = hashingService.verify(specialPassword, saltedHash)
        assertTrue(isValid)
    }

    @Test
    fun testUnicodeCharactersInPassword() {
        val unicodePassword = "пароль密码パスワード"
        val saltedHash = hashingService.generateSaltedHash(unicodePassword)

        val isValid = hashingService.verify(unicodePassword, saltedHash)
        assertTrue(isValid)
    }

    @Test
    fun testLongPasswordHandling() {
        val longPassword = "a".repeat(1000)
        val saltedHash = hashingService.generateSaltedHash(longPassword)

        val isValid = hashingService.verify(longPassword, saltedHash)
        assertTrue(isValid)
    }
}

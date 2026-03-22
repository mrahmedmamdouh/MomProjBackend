package com.evelolvetech.mocks

import com.evelolvetech.data.models.RefreshToken
import com.evelolvetech.data.repository.api.auth.RefreshTokenRepository

class MockRefreshTokenRepository : RefreshTokenRepository {
    private val tokens = mutableMapOf<String, RefreshToken>()

    override suspend fun createRefreshToken(refreshToken: RefreshToken): Boolean {
        tokens[refreshToken.token] = refreshToken
        return true
    }

    override suspend fun getRefreshToken(token: String): RefreshToken? {
        return when (token) {
            "valid-refresh-token" -> RefreshToken(
                token = token,
                userId = "test-user-id",
                userType = "MOM",
                email = "test@example.com",
                expiresAt = System.currentTimeMillis() + 86400000,
                lastUsedAt = System.currentTimeMillis()
            )
            else -> tokens[token]
        }
    }

    override suspend fun updateLastUsed(token: String, lastUsedAt: Long): Boolean {
        val token = tokens[token]
        return if (token != null) {
            tokens[token.token] = token.copy(lastUsedAt = lastUsedAt)
            true
        } else {
            false
        }
    }

    override suspend fun revokeToken(token: String): Boolean {
        return when (token) {
            "valid-refresh-token" -> true
            "invalid-refresh-token" -> false
            else -> tokens.remove(token) != null
        }
    }

    override suspend fun revokeAllUserTokens(userId: String): Boolean {
        tokens.entries.removeAll { it.value.userId == userId }
        return true
    }

    override suspend fun deleteExpiredTokens(): Int {
        val now = System.currentTimeMillis()
        val expiredTokens = tokens.entries.filter { it.value.expiresAt < now }
        expiredTokens.forEach { tokens.remove(it.key) }
        return expiredTokens.size
    }

    override suspend fun isTokenValid(token: String, idleTimeoutSeconds: Long): Boolean {
        val refreshToken = tokens[token] ?: return false
        val now = System.currentTimeMillis()
        return refreshToken.expiresAt > now && 
               (now - refreshToken.lastUsedAt) < (idleTimeoutSeconds * 1000)
    }

    fun addToken(token: String, refreshToken: RefreshToken) {
        tokens[token] = refreshToken
    }

    fun clear() {
        tokens.clear()
    }
}

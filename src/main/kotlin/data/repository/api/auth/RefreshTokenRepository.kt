package com.evelolvetech.data.repository.api.auth

import com.evelolvetech.data.models.RefreshToken

interface RefreshTokenRepository {
    suspend fun createRefreshToken(refreshToken: RefreshToken): Boolean
    suspend fun getRefreshToken(token: String): RefreshToken?
    suspend fun updateLastUsed(token: String, lastUsedAt: Long): Boolean
    suspend fun revokeToken(token: String): Boolean
    suspend fun revokeAllUserTokens(userId: String): Boolean
    suspend fun deleteExpiredTokens(): Int
    suspend fun isTokenValid(token: String, idleTimeoutSeconds: Long): Boolean
}

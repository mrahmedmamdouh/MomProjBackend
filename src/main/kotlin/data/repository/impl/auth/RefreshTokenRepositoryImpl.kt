package com.evelolvetech.data.repository.impl.auth

import com.evelolvetech.data.models.RefreshToken
import com.evelolvetech.data.repository.api.auth.RefreshTokenRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.litote.kmongo.eq
import org.litote.kmongo.and
import org.litote.kmongo.lt
import org.litote.kmongo.gt
import org.litote.kmongo.getCollection
import org.litote.kmongo.findOne
import java.time.Instant

class RefreshTokenRepositoryImpl(
    database: MongoDatabase
) : RefreshTokenRepository {
    
    private val refreshTokens: MongoCollection<RefreshToken> = database.getCollection()
    
    override suspend fun createRefreshToken(refreshToken: RefreshToken): Boolean {
        return try {
            refreshTokens.insertOne(refreshToken)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getRefreshToken(token: String): RefreshToken? {
        return try {
            refreshTokens.findOne(
                and(
                    RefreshToken::token eq token,
                    RefreshToken::isRevoked eq false,
                    RefreshToken::expiresAt gt Instant.now().epochSecond
                )
            )
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun updateLastUsed(token: String, lastUsedAt: Long): Boolean {
        return try {
            refreshTokens.updateOne(
                RefreshToken::token eq token,
                Updates.set(RefreshToken::lastUsedAt.name, lastUsedAt)
            ).modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun revokeToken(token: String): Boolean {
        return try {
            refreshTokens.updateOne(
                RefreshToken::token eq token,
                Updates.set(RefreshToken::isRevoked.name, true)
            ).modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun revokeAllUserTokens(userId: String): Boolean {
        return try {
            refreshTokens.updateMany(
                and(
                    RefreshToken::userId eq userId,
                    RefreshToken::isRevoked eq false
                ),
                Updates.set(RefreshToken::isRevoked.name, true)
            ).modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun deleteExpiredTokens(): Int {
        return try {
            refreshTokens.deleteMany(
                RefreshToken::expiresAt lt Instant.now().epochSecond
            ).deletedCount.toInt()
        } catch (e: Exception) {
            0
        }
    }
    
    override suspend fun isTokenValid(token: String, idleTimeoutSeconds: Long): Boolean {
        return try {
            val refreshToken = refreshTokens.findOne(
                and(
                    RefreshToken::token eq token,
                    RefreshToken::isRevoked eq false,
                    RefreshToken::expiresAt gt Instant.now().epochSecond
                )
            ) ?: return false
            
            val currentTime = Instant.now().epochSecond
            val idleThreshold = currentTime - idleTimeoutSeconds
            
            refreshToken.lastUsedAt > idleThreshold
        } catch (e: Exception) {
            false
        }
    }
}

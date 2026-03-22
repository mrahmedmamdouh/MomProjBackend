package com.evelolvetech.util

import com.evelolvetech.data.repository.api.mom.MomRepository
import com.evelolvetech.data.responses.BasicApiResponse
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class MomAuthCacheEntry(
    val isAuthorized: Boolean,
    val exists: Boolean,
    val timestamp: Long
) {
    companion object {
        fun createValidated(
            isAuthorized: Boolean,
            exists: Boolean,
            timestamp: Long
        ): MomAuthCacheEntry {
            val now = System.currentTimeMillis()
            require(timestamp <= now) { "Cache entry timestamp cannot be in the future." }
            require(!(isAuthorized && !exists)) { "isAuthorized cannot be true when exists is false." }
            return MomAuthCacheEntry(isAuthorized, exists, timestamp)
        }
    }
}

object MomAuthUtil {
    private val authCache = ConcurrentHashMap<String, MomAuthCacheEntry>()
    
    private fun isCacheEntryValid(entry: MomAuthCacheEntry, currentTime: Long, authConfig: AuthConfig): Boolean {
        return (currentTime - entry.timestamp) < TimeUnit.MINUTES.toMillis(authConfig.momAuthCacheDurationMinutes)
    }
    
    suspend fun <T> checkMomAuthorization(
        momId: String,
        momRepository: MomRepository,
        authConfig: AuthConfig,
        onAuthorized: suspend () -> BasicApiResponse<T>
    ): BasicApiResponse<T> {
        val authStatus = getMomAuthorizationStatus(momId, momRepository, authConfig)
        
        return when {
            !authStatus.exists -> BasicApiResponse(
                success = false,
                message = "Mom not found"
            )
            
            !authStatus.isAuthorized -> BasicApiResponse(
                success = false,
                message = "Access denied. Mom must be authorized to access this feature."
            )
            
            else -> onAuthorized()
        }
    }
    
    private suspend fun getMomAuthorizationStatus(
        momId: String,
        momRepository: MomRepository,
        authConfig: AuthConfig
    ): MomAuthCacheEntry {
        val currentTime = System.currentTimeMillis()
        val cachedEntry = authCache[momId]
        
        if (cachedEntry != null && isCacheEntryValid(cachedEntry, currentTime, authConfig)) {
            return cachedEntry
        }
        
        val mom = momRepository.getMomById(momId)
        val newEntry = if (mom != null) {
            MomAuthCacheEntry(
                isAuthorized = mom.isAuthorized,
                exists = true,
                timestamp = currentTime
            )
        } else {
            MomAuthCacheEntry(
                isAuthorized = false,
                exists = false,
                timestamp = currentTime
            )
        }
        
        authCache[momId] = newEntry
        
        return newEntry
    }
    
    fun invalidateMomAuthCache(momId: String) {
        authCache.remove(momId)
    }

    fun clearAuthCache() {
        authCache.clear()
    }

    fun getCacheStats(authConfig: AuthConfig): Map<String, Any> {
        val currentTime = System.currentTimeMillis()
        val validEntries = authCache.values.count { entry ->
            isCacheEntryValid(entry, currentTime, authConfig)
        }
        
        return mapOf(
            "totalEntries" to authCache.size,
            "validEntries" to validEntries,
            "expiredEntries" to (authCache.size - validEntries),
            "cacheDurationMinutes" to authConfig.momAuthCacheDurationMinutes
        )
    }
}

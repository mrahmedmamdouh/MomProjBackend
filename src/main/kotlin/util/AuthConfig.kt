package com.evelolvetech.util

import io.ktor.server.config.*

data class AuthConfig(
    val accessTokenExpiryMinutes: Long = 30L,
    val refreshTokenExpiryDays: Long = 30L,
    val idleTimeoutHours: Long = 24L,
    val momAuthorizationSessionThreshold: Int = 8,
    val momAuthCacheDurationMinutes: Long = 5L
) {
    companion object {
        fun fromApplicationConfig(config: ApplicationConfig): AuthConfig {
            return AuthConfig(
                accessTokenExpiryMinutes = config.propertyOrNull("auth.accessTokenExpiryMinutes")?.getString()?.toLongOrNull() ?: 30L,
                refreshTokenExpiryDays = config.propertyOrNull("auth.refreshTokenExpiryDays")?.getString()?.toLongOrNull() ?: 30L,
                idleTimeoutHours = config.propertyOrNull("auth.idleTimeoutHours")?.getString()?.toLongOrNull() ?: 24L,
                momAuthorizationSessionThreshold = config.propertyOrNull("auth.momAuthorizationSessionThreshold")?.getString()?.toIntOrNull() ?: 8,
                momAuthCacheDurationMinutes = config.propertyOrNull("auth.momAuthCacheDurationMinutes")?.getString()?.toLongOrNull() ?: 5L
            )
        }
    }
    
    val accessTokenExpiryMs: Long get() = accessTokenExpiryMinutes * 60 * 1000L
    val refreshTokenExpirySeconds: Long get() = refreshTokenExpiryDays * 24 * 60 * 60L
    val idleTimeoutSeconds: Long get() = idleTimeoutHours * 60 * 60L
}

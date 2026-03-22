package com.evelolvetech.mocks

import com.evelolvetech.util.AuthConfig

object MockAuthConfig {
    val instance = AuthConfig(
        accessTokenExpiryMinutes = 30L,
        refreshTokenExpiryDays = 30L,
        idleTimeoutHours = 24L,
        momAuthorizationSessionThreshold = 8,
        momAuthCacheDurationMinutes = 5L
    )
}

package com.evelolvetech.service.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.evelolvetech.data.models.RefreshToken
import com.evelolvetech.data.models.UserType
import com.evelolvetech.data.repository.api.auth.RefreshTokenRepository
import com.evelolvetech.data.repository.api.auth.UserRepository
import com.evelolvetech.data.requests.RefreshTokenRequest
import com.evelolvetech.data.requests.UnifiedLoginRequest
import com.evelolvetech.data.responses.AuthResponse
import com.evelolvetech.util.HashingService
import com.evelolvetech.util.AuthConfig
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import kotlin.text.toByteArray

class AuthService(
    private val userRepository: UserRepository,
    private val hashingService: HashingService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val authConfig: AuthConfig = AuthConfig()
) {

    suspend fun login(
        request: UnifiedLoginRequest,
        jwtIssuer: String,
        jwtAudience: String,
        jwtSecret: String
    ): AuthResponse? {
        val user = userRepository.getUserByEmail(request.email) ?: return null

        val (hash, salt) = user.password.split(":")
        val saltedHash = com.evelolvetech.util.SaltedHash(hash, salt)

        if (!hashingService.verify(request.password, saltedHash)) {
            return null
        }

        val (userId, userType) = when (user.userType) {
            UserType.MOM -> user.momId!! to "MOM"
            UserType.DOCTOR -> user.doctorId!! to "DOCTOR"
            UserType.ADMIN -> user.id to "ADMIN"
        }

        refreshTokenRepository.revokeAllUserTokens(userId)

        val accessTokenExpiryMs = authConfig.accessTokenExpiryMs
        val accessToken = JWT.create()
            .withClaim("userId", userId)
            .withClaim("userType", userType)
            .withClaim("email", user.email)
            .withIssuer(jwtIssuer)
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpiryMs))
            .withAudience(jwtAudience)
            .sign(Algorithm.HMAC256(jwtSecret))

        val refreshTokenString = generateSecureToken()
        val refreshTokenExpirySeconds = authConfig.refreshTokenExpirySeconds
        val refreshToken = RefreshToken(
            token = refreshTokenString,
            userId = userId,
            userType = userType,
            email = user.email,
            expiresAt = Instant.now().epochSecond + refreshTokenExpirySeconds
        )

        if (!refreshTokenRepository.createRefreshToken(refreshToken)) {
            return null
        }

        return AuthResponse(
            userId = userId,
            token = accessToken,
            refreshToken = refreshTokenString,
            userType = userType,
            expiresIn = authConfig.accessTokenExpiryMinutes * 60,
            refreshExpiresIn = refreshTokenExpirySeconds
        )
    }

    suspend fun refreshToken(
        request: RefreshTokenRequest,
        jwtIssuer: String,
        jwtAudience: String,
        jwtSecret: String
    ): AuthResponse? {
        val idleTimeoutSeconds = authConfig.idleTimeoutSeconds
        
        if (!refreshTokenRepository.isTokenValid(request.refreshToken, idleTimeoutSeconds)) {
            return null
        }

        val refreshToken = refreshTokenRepository.getRefreshToken(request.refreshToken) ?: return null
        
        refreshTokenRepository.updateLastUsed(request.refreshToken, Instant.now().epochSecond)

        val accessTokenExpiryMs = authConfig.accessTokenExpiryMs
        val accessToken = JWT.create()
            .withClaim("userId", refreshToken.userId)
            .withClaim("userType", refreshToken.userType)
            .withClaim("email", refreshToken.email)
            .withIssuer(jwtIssuer)
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpiryMs))
            .withAudience(jwtAudience)
            .sign(Algorithm.HMAC256(jwtSecret))

        val newRefreshTokenString = generateSecureToken()
        val refreshTokenExpirySeconds = authConfig.refreshTokenExpirySeconds
        val newRefreshToken = RefreshToken(
            token = newRefreshTokenString,
            userId = refreshToken.userId,
            userType = refreshToken.userType,
            email = refreshToken.email,
            expiresAt = Instant.now().epochSecond + refreshTokenExpirySeconds
        )

        if (!refreshTokenRepository.createRefreshToken(newRefreshToken)) {
            return null
        }

        if (!refreshTokenRepository.revokeToken(request.refreshToken)) {
            refreshTokenRepository.revokeToken(newRefreshTokenString)
            return null
        }

        return AuthResponse(
            userId = refreshToken.userId,
            token = accessToken,
            refreshToken = newRefreshTokenString,
            userType = refreshToken.userType,
            expiresIn = authConfig.accessTokenExpiryMinutes * 60,
            refreshExpiresIn = refreshTokenExpirySeconds
        )
    }

    suspend fun logout(refreshToken: String): Boolean {
        return refreshTokenRepository.revokeToken(refreshToken)
    }

    private fun generateSecureToken(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    suspend fun doesEmailExist(email: String): Boolean {
        return userRepository.doesEmailExist(email)
    }

    suspend fun deleteUserByEmail(email: String): Boolean {
        val user = userRepository.getUserByEmail(email) ?: return false
        return userRepository.deleteUser(user.id)
    }
}

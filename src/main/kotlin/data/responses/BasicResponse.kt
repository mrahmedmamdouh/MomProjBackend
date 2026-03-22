package com.evelolvetech.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class BasicApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

@Serializable
data class AuthResponse(
    val userId: String,
    val token: String,
    val refreshToken: String,
    val userType: String,
    val expiresIn: Long,
    val refreshExpiresIn: Long
)

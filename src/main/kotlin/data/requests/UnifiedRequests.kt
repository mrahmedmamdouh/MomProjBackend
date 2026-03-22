package com.evelolvetech.data.requests

data class RegisterMomMultipartRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val phone: String,
    val maritalStatus: String,
)

data class RegisterDoctorMultipartRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String,
    val specialization: String
)

data class UnifiedLoginRequest(
    val email: String,
    val password: String
)

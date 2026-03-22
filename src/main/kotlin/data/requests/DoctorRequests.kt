package com.evelolvetech.data.requests

data class UpdateDoctorRequest(
    val name: String?,
    val phone: String?,
    val specialization: String?
)

data class UpdateDoctorMultipartRequest(
    val name: String?,
    val phone: String?,
    val specialization: String?,
    val photo: String?
)
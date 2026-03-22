package com.evelolvetech.data.requests

data class UpdateMomRequest(
    val fullName: String?,
    val phone: String?,
    val maritalStatus: String?
)

data class UpdateMomMultipartRequest(
    val fullName: String?,
    val phone: String?,
    val maritalStatus: String?,
    val photo: String?
)

data class UpdateSessionsRequest(
    val numberOfSessions: Int?
)

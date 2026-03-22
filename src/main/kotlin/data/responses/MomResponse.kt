package com.evelolvetech.data.responses

import com.evelolvetech.data.models.Mom
import kotlinx.serialization.Serializable

@Serializable
data class MomResponse(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val maritalStatus: String,
    val photoUrl: String,
    val createdAt: Long
) {
    companion object {
        fun fromMom(mom: Mom) = MomResponse(
            id = mom.id,
            fullName = mom.fullName,
            email = mom.email,
            phone = mom.phone,
            maritalStatus = mom.maritalStatus,
            photoUrl = mom.photoUrl,
            createdAt = mom.createdAt
        )
    }
}

package com.evelolvetech.data.responses

import com.evelolvetech.data.models.Doctor
import kotlinx.serialization.Serializable

@Serializable
data class DoctorResponse(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val specialization: String,
    val rating: Double,
    val photo: String,
    val createdAt: Long
) {
    companion object {
        fun fromDoctor(doctor: Doctor) = DoctorResponse(
            id = doctor.id,
            name = doctor.name,
            email = doctor.email,
            phone = doctor.phone,
            specialization = doctor.specialization,
            rating = doctor.rating,
            photo = doctor.photo,
            createdAt = doctor.createdAt
        )
    }
}

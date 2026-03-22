package com.evelolvetech.data.repository.api.doctor

import com.evelolvetech.data.models.Doctor
import com.evelolvetech.data.models.DoctorAuth
import com.mongodb.client.ClientSession

interface DoctorRepository {
    suspend fun createDoctor(doctor: Doctor): Boolean
    suspend fun createDoctor(doctor: Doctor, session: ClientSession): Boolean
    suspend fun getDoctorById(id: String): Doctor?
    suspend fun getDoctorByEmail(email: String): Doctor?
    suspend fun getDoctorByAuthUid(authUid: String): Doctor?
    suspend fun updateDoctor(
        id: String,
        name: String?,
        phone: String?,
        specialization: String?,
        photo: String?
    ): Boolean

    suspend fun updateDoctorAuthorization(id: String, isAuthorized: Boolean): Boolean
    suspend fun deleteDoctor(id: String): Boolean
    suspend fun deleteDoctor(id: String, session: ClientSession): Boolean
    suspend fun doesEmailExist(email: String): Boolean
    suspend fun createDoctorAuth(doctorAuth: DoctorAuth): Boolean
    suspend fun createDoctorAuth(doctorAuth: DoctorAuth, session: ClientSession): Boolean
    suspend fun getDoctorAuthByUid(uid: String): DoctorAuth?
    suspend fun deleteDoctorAuth(uid: String): Boolean
    suspend fun getAllDoctors(): List<Doctor>
    suspend fun getAuthorizedDoctors(): List<Doctor>
    suspend fun getUnauthorizedDoctors(page: Int = 0, size: Int = 50): List<Doctor>
    suspend fun getDoctorsBySpecialization(specialization: String): List<Doctor>
}

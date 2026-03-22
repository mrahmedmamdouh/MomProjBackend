package com.evelolvetech.mocks

import com.evelolvetech.data.models.Doctor
import com.evelolvetech.data.models.DoctorAuth
import com.evelolvetech.data.repository.api.doctor.DoctorRepository
import com.evelolvetech.data.requests.UpdateDoctorRequest
import com.mongodb.client.ClientSession

class MockDoctorRepository : DoctorRepository {
    val doctors = mutableMapOf<String, Doctor>()
    val createdDoctors = mutableListOf<Doctor>()
    val createdDoctorAuths = mutableListOf<DoctorAuth>()
    val updateCalls = mutableListOf<Pair<String, UpdateDoctorRequest?>>()
    var updateResult = false

    override suspend fun createDoctor(doctor: Doctor): Boolean {
        createdDoctors.add(doctor)
        doctors[doctor.id] = doctor
        return true
    }

    override suspend fun getDoctorById(id: String): Doctor? = doctors[id]

    override suspend fun getDoctorByEmail(email: String): Doctor? =
        doctors.values.find { it.email == email }

    override suspend fun getDoctorByAuthUid(authUid: String): Doctor? =
        doctors.values.find { it.authUid == authUid }

    override suspend fun getAllDoctors(): List<Doctor> = doctors.values.toList()

    override suspend fun getAuthorizedDoctors(): List<Doctor> =
        doctors.values.filter { it.isAuthorized }

    override suspend fun updateDoctor(
        id: String,
        name: String?,
        phone: String?,
        specialization: String?,
        photo: String?
    ): Boolean {
        updateCalls.add(id to null)
        return updateResult
    }

    override suspend fun getDoctorsBySpecialization(specialization: String): List<Doctor> =
        doctors.values.filter { it.specialization == specialization }

    override suspend fun getUnauthorizedDoctors(page: Int, size: Int): List<Doctor> =
        doctors.values.filter { !it.isAuthorized }.drop(page * size).take(size)

    override suspend fun updateDoctorAuthorization(id: String, isAuthorized: Boolean): Boolean {
        val doctor = doctors[id]
        return if (doctor != null) {
            doctors[id] = doctor.copy(isAuthorized = isAuthorized)
            true
        } else {
            false
        }
    }

    override suspend fun deleteDoctor(id: String): Boolean = true

    override suspend fun doesEmailExist(email: String): Boolean =
        doctors.values.any { it.email == email }

    override suspend fun createDoctorAuth(doctorAuth: DoctorAuth): Boolean {
        createdDoctorAuths.add(doctorAuth)
        return true
    }

    override suspend fun createDoctorAuth(doctorAuth: DoctorAuth, session: ClientSession): Boolean {
        createdDoctorAuths.add(doctorAuth)
        return true
    }

    override suspend fun createDoctor(doctor: Doctor, session: ClientSession): Boolean {
        createdDoctors.add(doctor)
        doctors[doctor.id] = doctor
        return true
    }

    override suspend fun deleteDoctor(id: String, session: ClientSession): Boolean = true

    override suspend fun getDoctorAuthByUid(uid: String): DoctorAuth? = null

    override suspend fun deleteDoctorAuth(uid: String): Boolean = true
}

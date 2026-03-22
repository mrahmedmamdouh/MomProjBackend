package com.evelolvetech.data.repository.impl.doctor

import com.evelolvetech.data.models.Doctor
import com.evelolvetech.data.models.DoctorAuth
import com.evelolvetech.data.repository.api.doctor.DoctorRepository
import com.mongodb.client.ClientSession
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class DoctorRepositoryImpl(
    db: MongoDatabase
) : DoctorRepository {

    private val doctors: MongoCollection<Doctor> = db.getCollection<Doctor>()
    private val doctorAuth: MongoCollection<DoctorAuth> = db.getCollection<DoctorAuth>()

    override suspend fun createDoctor(doctor: Doctor): Boolean {
        return try {
            doctors.insertOne(doctor)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun createDoctor(doctor: Doctor, session: ClientSession): Boolean {
        return try {
            doctors.insertOne(session, doctor)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getDoctorById(id: String): Doctor? {
        return doctors.findOne(Doctor::id eq id)
    }

    override suspend fun getDoctorByEmail(email: String): Doctor? {
        return doctors.findOne(Doctor::email eq email)
    }

    override suspend fun getDoctorByAuthUid(authUid: String): Doctor? {
        return doctors.findOne(Doctor::authUid eq authUid)
    }

    override suspend fun updateDoctor(
        id: String,
        name: String?,
        phone: String?,
        specialization: String?,
        photo: String?
    ): Boolean {
        return try {
            val updates = mutableListOf<Bson>()
            name?.let { updates.add(Updates.set(Doctor::name.name, it)) }
            phone?.let { updates.add(Updates.set(Doctor::phone.name, it)) }
            specialization?.let { updates.add(Updates.set(Doctor::specialization.name, it)) }
            photo?.let { updates.add(Updates.set(Doctor::photo.name, it)) }

            if (updates.isNotEmpty()) {
                val result = doctors.updateOne(
                    Doctor::id eq id,
                    Updates.combine(updates)
                )
                result.modifiedCount > 0
            } else false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateDoctorAuthorization(id: String, isAuthorized: Boolean): Boolean {
        return try {
            val result = doctors.updateOne(
                Doctor::id eq id,
                Updates.set(Doctor::isAuthorized.name, isAuthorized)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteDoctor(id: String): Boolean {
        return try {
            val result = doctors.deleteOne(Doctor::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteDoctor(id: String, session: ClientSession): Boolean {
        return try {
            val result = doctors.deleteOne(session, Doctor::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun doesEmailExist(email: String): Boolean {
        return doctors.findOne(Doctor::email eq email) != null
    }

    override suspend fun createDoctorAuth(doctorAuth: DoctorAuth): Boolean {
        return try {
            this.doctorAuth.insertOne(doctorAuth)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun createDoctorAuth(doctorAuth: DoctorAuth, session: ClientSession): Boolean {
        return try {
            this.doctorAuth.insertOne(session, doctorAuth)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getDoctorAuthByUid(uid: String): DoctorAuth? {
        return doctorAuth.findOne(DoctorAuth::uid eq uid)
    }

    override suspend fun deleteDoctorAuth(uid: String): Boolean {
        return try {
            val result = doctorAuth.deleteOne(DoctorAuth::uid eq uid)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getAllDoctors(): List<Doctor> {
        return doctors.find().toList()
    }

    override suspend fun getAuthorizedDoctors(): List<Doctor> {
        return doctors.find(Doctor::isAuthorized eq true).toList()
    }

    override suspend fun getUnauthorizedDoctors(page: Int, size: Int): List<Doctor> {
        return doctors.find(Doctor::isAuthorized eq false)
            .sort(org.litote.kmongo.descending(Doctor::createdAt))
            .skip(page * size).limit(size).toList()
    }

    override suspend fun getDoctorsBySpecialization(specialization: String): List<Doctor> {
        return doctors.find(Doctor::specialization eq specialization).toList()
    }
}

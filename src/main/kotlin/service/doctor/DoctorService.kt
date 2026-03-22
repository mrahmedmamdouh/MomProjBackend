package com.evelolvetech.service.doctor

import com.evelolvetech.data.models.*
import com.evelolvetech.data.repository.api.doctor.DoctorRepository
import com.evelolvetech.data.repository.api.mom.NidRepository
import com.evelolvetech.data.repository.api.auth.UserRepository
import com.evelolvetech.data.requests.RegisterDoctorMultipartRequest
import com.evelolvetech.data.requests.UpdateDoctorMultipartRequest
import com.evelolvetech.data.requests.UpdateDoctorRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.data.responses.DoctorResponse
import com.evelolvetech.util.*
import com.evelolvetech.service.TransactionServiceInterface
import org.bson.types.ObjectId

class DoctorService(
    private val doctorRepository: DoctorRepository,
    private val nidRepository: NidRepository,
    private val userRepository: UserRepository,
    private val hashingService: HashingService,
    private val transactionService: TransactionServiceInterface
) {
    suspend fun createDoctorMultipart(request: RegisterDoctorMultipartRequest, filePaths: UploadedFilePaths): String? {
        val doctorId = ObjectId().toString()
        val authUid = ObjectId().toString()
        val nidId = ObjectId().toString()

        val nid = Nid(
            id = nidId,
            imageFront = filePaths.nidFrontPath,
            imageBack = filePaths.nidBackPath
        )

        val hashedPassword = hashingService.generateSaltedHash(request.password)

        val doctor = Doctor(
            id = doctorId,
            authUid = authUid,
            name = request.name,
            email = request.email,
            phone = request.phone,
            specialization = request.specialization,
            photo = filePaths.photoPath,
            nidId = nidId,
            nidRef = "/nids/$nidId"
        )

        val doctorAuth = DoctorAuth(
            uid = authUid,
            doctorId = doctorId
        )

        val user = User(
            email = request.email,
            password = "${hashedPassword.hash}:${hashedPassword.salt}",
            userType = UserType.DOCTOR,
            doctorId = doctorId
        )

        return try {
            transactionService.withTransaction { session ->
                val nidSuccess = nidRepository.createNid(nid, session)
                if (!nidSuccess) throw NidCreationException()

                val doctorSuccess = doctorRepository.createDoctor(doctor, session)
                if (!doctorSuccess) throw DoctorCreationException()

                val doctorAuthSuccess = doctorRepository.createDoctorAuth(doctorAuth, session)
                if (!doctorAuthSuccess) throw DoctorAuthCreationException()

                val userSuccess = userRepository.createUserEntry(user, session)
                if (!userSuccess) throw UserEntryCreationException()

                doctorId
            }
        } catch (e: UserRegistrationException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getDoctorById(id: String): Doctor? {
        return doctorRepository.getDoctorById(id)
    }

    suspend fun getDoctorByEmail(email: String): Doctor? {
        return doctorRepository.getDoctorByEmail(email)
    }

    suspend fun getDoctorByAuthUid(authUid: String): Doctor? {
        return doctorRepository.getDoctorByAuthUid(authUid)
    }

    suspend fun updateDoctor(id: String, request: UpdateDoctorRequest): Boolean {
        return doctorRepository.updateDoctor(
            id,
            request.name,
            request.phone,
            request.specialization,
            null
        )
    }

    suspend fun updateDoctorMultipart(id: String, request: UpdateDoctorMultipartRequest, photoPath: String?): Boolean {
        return doctorRepository.updateDoctor(
            id,
            request.name,
            request.phone,
            request.specialization,
            photoPath
        )
    }

    suspend fun deleteDoctor(id: String): Boolean {
        return doctorRepository.deleteDoctor(id)
    }

    suspend fun doesEmailExist(email: String): Boolean {
        return doctorRepository.doesEmailExist(email)
    }

    suspend fun getDoctorAuthByUid(uid: String): DoctorAuth? {
        return doctorRepository.getDoctorAuthByUid(uid)
    }

    suspend fun getAuthorizedDoctors(): List<Doctor> {
        return doctorRepository.getAuthorizedDoctors()
    }

    suspend fun getAllDoctors(): List<Doctor> {
        return doctorRepository.getAllDoctors()
    }

    suspend fun getDoctorsBySpecialization(specialization: String): List<DoctorResponse> {
        return doctorRepository.getDoctorsBySpecialization(specialization).map { DoctorResponse.fromDoctor(it) }
    }

    suspend fun isValidPassword(enteredPassword: String, actualPassword: String): Boolean {
        val parts = actualPassword.split(":")
        if (parts.size != 2) return false

        val saltedHash = com.evelolvetech.util.SaltedHash(parts[0], parts[1])
        return hashingService.verify(enteredPassword, saltedHash)
    }

    fun validateCreateDoctorMultipartRequest(request: RegisterDoctorMultipartRequest): ValidationEvent {
        if (request.email.isBlank() || request.password.isBlank() ||
            request.name.isBlank() || request.phone.isBlank() ||
            request.specialization.isBlank()
        ) {
            return ValidationEvent.ErrorFieldEmpty
        }
        if (!ValidationUtil.isValidEmail(request.email)) {
            return ValidationEvent.ErrorInvalidEmail
        }
        if (!ValidationUtil.isValidPassword(request.password)) {
            return ValidationEvent.ErrorPasswordTooShort
        }
        if (!ValidationUtil.isValidPhone(request.phone)) {
            return ValidationEvent.ErrorInvalidPhone
        }
        if (!ValidationUtil.isValidSpecialization(request.specialization)) {
            return ValidationEvent.ErrorInvalidSpecialization
        }
        return ValidationEvent.Success
    }

    fun validateUpdateDoctorRequest(request: UpdateDoctorRequest): ValidationEvent {
        if (request.name?.isBlank() == true) {
            return ValidationEvent.ErrorFieldEmpty
        }
        if (request.phone != null && !ValidationUtil.isValidPhone(request.phone)) {
            return ValidationEvent.ErrorInvalidPhone
        }
        if (request.specialization != null && !ValidationUtil.isValidSpecialization(request.specialization)) {
            return ValidationEvent.ErrorInvalidSpecialization
        }
        return ValidationEvent.Success
    }

    fun validateUpdateDoctorMultipartRequest(request: UpdateDoctorMultipartRequest): ValidationEvent {
        if (request.name?.isBlank() == true) {
            return ValidationEvent.ErrorFieldEmpty
        }
        if (request.phone != null && !ValidationUtil.isValidPhone(request.phone)) {
            return ValidationEvent.ErrorInvalidPhone
        }
        if (request.specialization != null && !ValidationUtil.isValidSpecialization(request.specialization)) {
            return ValidationEvent.ErrorInvalidSpecialization
        }
        return ValidationEvent.Success
    }

    suspend fun updateDoctorAuthorization(doctorId: String, isAuthorized: Boolean): BasicApiResponse<Map<String, Any>> {
        val doctor = doctorRepository.getDoctorById(doctorId)
        
        return when {
            doctor == null -> BasicApiResponse(
                success = false,
                message = "Doctor not found"
            )
            
            doctor.isAuthorized == isAuthorized -> BasicApiResponse(
                success = true,
                message = "Doctor authorization status is already ${if (isAuthorized) "authorized" else "revoked"}",
                data = mapOf(
                    "doctorId" to doctorId,
                    "isAuthorized" to isAuthorized,
                    "noChangeRequired" to true
                )
            )
            
            else -> {
                val updateSuccess = doctorRepository.updateDoctorAuthorization(doctorId, isAuthorized)
                if (updateSuccess) {
                    BasicApiResponse(
                        success = true,
                        message = if (isAuthorized) "Doctor authorized successfully" else "Doctor access revoked successfully",
                        data = mapOf(
                            "doctorId" to doctorId,
                            "isAuthorized" to isAuthorized
                        )
                    )
                } else {
                    BasicApiResponse(
                        success = false,
                        message = "Failed to update doctor authorization"
                    )
                }
            }
        }
    }

    suspend fun getDoctorStatus(doctorId: String): BasicApiResponse<Map<String, Any>> {
        val doctor = doctorRepository.getDoctorById(doctorId)
        
        return if (doctor != null) {
            BasicApiResponse(
                success = true,
                data = mapOf(
                    "doctorId" to doctorId,
                    "isAuthorized" to doctor.isAuthorized,
                    "name" to doctor.name,
                    "email" to doctor.email,
                    "specialization" to doctor.specialization
                )
            )
        } else {
            BasicApiResponse(
                success = false,
                message = "Doctor not found"
            )
        }
    }

    sealed class ValidationEvent {
        object ErrorFieldEmpty : ValidationEvent()
        object ErrorInvalidEmail : ValidationEvent()
        object ErrorPasswordTooShort : ValidationEvent()
        object ErrorInvalidPhone : ValidationEvent()
        object ErrorInvalidSpecialization : ValidationEvent()
        object Success : ValidationEvent()
    }
}

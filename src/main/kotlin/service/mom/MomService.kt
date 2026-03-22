package com.evelolvetech.service.mom

import com.evelolvetech.data.models.*
import com.evelolvetech.data.repository.api.mom.MomRepository
import com.evelolvetech.data.repository.api.mom.NidRepository
import com.evelolvetech.data.repository.api.auth.UserRepository
import com.evelolvetech.data.requests.RegisterMomMultipartRequest
import com.evelolvetech.data.requests.UpdateMomMultipartRequest
import com.evelolvetech.data.requests.UpdateMomRequest
import com.evelolvetech.util.*
import com.evelolvetech.service.TransactionServiceInterface
import org.bson.types.ObjectId

class MomService(
    private val momRepository: MomRepository,
    private val nidRepository: NidRepository,
    private val userRepository: UserRepository,
    private val hashingService: HashingService,
    private val transactionService: TransactionServiceInterface,
    private val authConfig: com.evelolvetech.util.AuthConfig
) {
    suspend fun createMomMultipart(request: RegisterMomMultipartRequest, filePaths: UploadedFilePaths): String? {
        val momId = ObjectId().toString()
        val authUid = ObjectId().toString()
        val nidId = ObjectId().toString()

        val nid = Nid(
            id = nidId,
            imageFront = filePaths.nidFrontPath,
            imageBack = filePaths.nidBackPath
        )

        val hashedPassword = hashingService.generateSaltedHash(request.password)

        val mom = Mom(
            id = momId,
            authUid = authUid,
            fullName = request.fullName,
            email = request.email,
            phone = request.phone,
            maritalStatus = request.maritalStatus,
            photoUrl = filePaths.photoPath,
            nidId = nidId,
            nidRef = "/nids/$nidId",
            numberOfSessions = 0
        )

        val momAuth = MomAuth(
            uid = authUid,
            momId = momId
        )

        val user = User(
            email = request.email,
            password = "${hashedPassword.hash}:${hashedPassword.salt}",
            userType = UserType.MOM,
            momId = momId
        )

        return try {
            transactionService.withTransaction { session ->
                val nidSuccess = nidRepository.createNid(nid, session)
                if (!nidSuccess) throw NidCreationException()

                val momSuccess = momRepository.createMom(mom, session)
                if (!momSuccess) throw MomCreationException()

                val momAuthSuccess = momRepository.createMomAuth(momAuth, session)
                if (!momAuthSuccess) throw MomAuthCreationException()

                val userSuccess = userRepository.createUserEntry(user, session)
                if (!userSuccess) throw UserEntryCreationException()

                momId
            }
            } catch (e: UserRegistrationException) {
                null
            } catch (e: Exception) {
                null
            }
    }

    suspend fun getMomById(id: String): Mom? {
        return momRepository.getMomById(id)
    }

    suspend fun getMomByEmail(email: String): Mom? {
        return momRepository.getMomByEmail(email)
    }

    suspend fun getMomByAuthUid(authUid: String): Mom? {
        return momRepository.getMomByAuthUid(authUid)
    }

    suspend fun updateMom(id: String, request: UpdateMomRequest): Boolean {
        return momRepository.updateMom(id, request.fullName, request.phone, request.maritalStatus, null)
    }

    suspend fun updateMomMultipart(id: String, request: UpdateMomMultipartRequest, photoPath: String?): Boolean {
        return momRepository.updateMom(id, request.fullName, request.phone, request.maritalStatus, photoPath)
    }

    suspend fun deleteMom(id: String): Boolean {
        return momRepository.deleteMom(id)
    }

    suspend fun doesEmailExist(email: String): Boolean {
        return momRepository.doesEmailExist(email)
    }

    suspend fun getMomAuthByUid(uid: String): MomAuth? {
        return momRepository.getMomAuthByUid(uid)
    }


    suspend fun updateMomSessions(id: String, sessions: Int): Boolean {
        val result = momRepository.updateMomSessions(id, sessions)
        if (result) {
            checkAndUpdateAuthorization(id, sessions)
        }
        return result
    }

    private suspend fun checkAndUpdateAuthorization(momId: String, sessions: Int) {
        val wasUpdated = if (sessions >= authConfig.momAuthorizationSessionThreshold) {
            momRepository.updateMomAuthorization(momId, true)
        } else {
            momRepository.updateMomAuthorization(momId, false)
        }
        
        if (wasUpdated) {
            MomAuthUtil.invalidateMomAuthCache(momId)
        }
    }

    suspend fun isValidPassword(enteredPassword: String, actualPassword: String): Boolean {
        val parts = actualPassword.split(":")
        if (parts.size != 2) return false

        val saltedHash = com.evelolvetech.util.SaltedHash(parts[0], parts[1])
        return hashingService.verify(enteredPassword, saltedHash)
    }

    fun validateCreateMomMultipartRequest(request: RegisterMomMultipartRequest): ValidationEvent {
        if (request.email.isBlank() || request.password.isBlank() ||
            request.fullName.isBlank() || request.phone.isBlank() ||
            request.maritalStatus.isBlank()
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
        if (!ValidationUtil.isValidMaritalStatus(request.maritalStatus)) {
            return ValidationEvent.ErrorInvalidMaritalStatus
        }
        return ValidationEvent.Success
    }

    sealed class ValidationEvent {
        object ErrorFieldEmpty : ValidationEvent()
        object ErrorInvalidEmail : ValidationEvent()
        object ErrorPasswordTooShort : ValidationEvent()
        object ErrorInvalidPhone : ValidationEvent()
        object ErrorInvalidMaritalStatus : ValidationEvent()
        object Success : ValidationEvent()
    }
}

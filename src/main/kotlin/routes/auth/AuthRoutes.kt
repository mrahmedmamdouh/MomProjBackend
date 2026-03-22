package com.evelolvetech.routes.auth

import com.evelolvetech.data.models.UploadedFilePaths
import com.evelolvetech.data.requests.RefreshTokenRequest
import com.evelolvetech.data.requests.RegisterDoctorMultipartRequest
import com.evelolvetech.data.requests.RegisterMomMultipartRequest
import com.evelolvetech.data.requests.UnifiedLoginRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.service.auth.AuthService
import com.evelolvetech.service.doctor.DoctorService
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.util.Constants
import com.evelolvetech.util.FileUploadUtil
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.google.gson.Gson


fun Route.authRoutes(
    authService: AuthService,
    momService: MomService,
    doctorService: DoctorService,
    jwtIssuer: String,
    jwtAudience: String,
    jwtSecret: String,
    gson: Gson
) {
    route("/api/auth") {
        post("/register/mom") {
            val multipartData = call.receiveMultipart()
            var momRequest: RegisterMomMultipartRequest? = null
            var requestProcessed = false
            var validationFailed = false
            var emailExists = false
            var photoPath: String? = null
            var nidFrontPath: String? = null
            var nidBackPath: String? = null
            val fileParts = mutableMapOf<String, PartData.FileItem>()

            multipartData.forEachPart { part ->
                when {
                    part is PartData.FormItem && part.name == "data" -> {
                        try {
                            momRequest = gson.fromJson(part.value, RegisterMomMultipartRequest::class.java)

                            val validationResult = momService.validateCreateMomMultipartRequest(momRequest!!)
                            if (validationResult != MomService.ValidationEvent.Success) {
                                validationFailed = true
                                requestProcessed = true
                            } else {
                                emailExists = authService.doesEmailExist(momRequest!!.email)
                                requestProcessed = true
                            }
                        } catch (e: Exception) {
                            momRequest = null
                            validationFailed = true
                            requestProcessed = true
                            part.dispose()
                            call.respond(
                                HttpStatusCode.BadRequest,
                                BasicApiResponse<Unit>(
                                    success = false,
                                    message = "Invalid JSON format in multipart data: ${e.message}"
                                )
                            )
                            return@forEachPart
                        }
                        part.dispose()
                    }

                    part is PartData.FileItem && (part.name == "photo" || part.name == "nidFront" || part.name == "nidBack") -> {
                        if (!requestProcessed) {
                            fileParts[part.name!!] = part
                        } else if (validationFailed || emailExists) {
                            part.dispose()
                        } else {
                            when (part.name) {
                                "photo" -> photoPath = FileUploadUtil.saveProfileImage(part)
                                "nidFront" -> nidFrontPath = FileUploadUtil.saveNidImage(part, "mom_front")
                                "nidBack" -> nidBackPath = FileUploadUtil.saveNidImage(part, "mom_back")
                            }
                            part.dispose()
                        }
                    }

                    else -> part.dispose()
                }
                if (requestProcessed && fileParts.isNotEmpty()) {
                    fileParts.forEach { (name, bufferedPart) ->
                        if (validationFailed || emailExists) {
                            bufferedPart.dispose()
                        } else {
                            when (name) {
                                "photo" -> photoPath = FileUploadUtil.saveProfileImage(bufferedPart)
                                "nidFront" -> nidFrontPath = FileUploadUtil.saveNidImage(bufferedPart, "mom_front")
                                "nidBack" -> nidBackPath = FileUploadUtil.saveNidImage(bufferedPart, "mom_back")
                            }
                            bufferedPart.dispose()
                        }
                    }
                    fileParts.clear()
                }
            }

            if (momRequest == null || validationFailed) {
                val errorMessage = if (momRequest == null) {
                    "Invalid request data. Please check that all required fields are provided and properly formatted."
                } else {
                    val validationResult = momService.validateCreateMomMultipartRequest(momRequest!!)
                    when (validationResult) {
                        is MomService.ValidationEvent.ErrorFieldEmpty -> Constants.FIELDS_BLANK
                        is MomService.ValidationEvent.ErrorInvalidEmail -> Constants.INVALID_EMAIL
                        is MomService.ValidationEvent.ErrorPasswordTooShort -> Constants.PASSWORD_TOO_SHORT
                        is MomService.ValidationEvent.ErrorInvalidPhone -> "Invalid phone number format. Please use a valid phone number."
                        is MomService.ValidationEvent.ErrorInvalidMaritalStatus -> "Invalid marital status. Must be one of: SINGLE, MARRIED, DIVORCED, WIDOWED"
                        else -> "Validation failed"
                    }
                }
                call.respond(
                    HttpStatusCode.BadRequest, BasicApiResponse<Unit>(
                        success = false,
                        message = errorMessage
                    )
                )
                return@post
            }

            if (emailExists) {
                call.respond(
                    BasicApiResponse<Unit>(
                        success = false,
                        message = Constants.USER_ALREADY_EXISTS
                    )
                )
                return@post
            }
            if (photoPath == null || nidFrontPath == null || nidBackPath == null) {
                call.respond(
                    HttpStatusCode.BadRequest, BasicApiResponse<Unit>(
                        success = false,
                        message = "Failed to save uploaded files"
                    )
                )
                return@post
            }
            val filePaths = UploadedFilePaths(nidFrontPath!!, nidBackPath!!, photoPath!!)
            val momId = momService.createMomMultipart(momRequest!!, filePaths)
            if (momId != null) {
                val loginRequest = UnifiedLoginRequest(
                    email = momRequest!!.email,
                    password = momRequest!!.password
                )
                val authResponse = authService.login(loginRequest, jwtIssuer, jwtAudience, jwtSecret)
                
                if (authResponse != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse(
                            success = true,
                            data = authResponse
                        )
                    )
                } else {
                    FileUploadUtil.cleanupUploadedFiles(filePaths)
                    momService.deleteMom(momId)
                    authService.deleteUserByEmail(momRequest!!.email)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = "Registration succeeded but login failed. Please try logging in manually."
                        )
                    )
                }
            } else {
                FileUploadUtil.cleanupUploadedFiles(filePaths)

                call.respond(
                    HttpStatusCode.InternalServerError,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = "Registration failed. Please try again."
                    )
                )
            }
        }

        post("/register/doctor") {
            val multipartData = call.receiveMultipart()
            var doctorRequest: RegisterDoctorMultipartRequest? = null
            var requestProcessed = false
            var validationFailed = false
            var emailExists = false
            var photoPath: String? = null
            var nidFrontPath: String? = null
            var nidBackPath: String? = null
            val fileParts = mutableMapOf<String, PartData.FileItem>()

            multipartData.forEachPart { part ->
                when {
                    part is PartData.FormItem && part.name == "data" -> {
                        try {
                            doctorRequest = gson.fromJson(part.value, RegisterDoctorMultipartRequest::class.java)
                            val validationResult = doctorService.validateCreateDoctorMultipartRequest(doctorRequest!!)
                            if (validationResult != DoctorService.ValidationEvent.Success) {
                                validationFailed = true
                            } else {
                                emailExists = authService.doesEmailExist(doctorRequest!!.email)
                            }
                            requestProcessed = true
                        } catch (e: Exception) {
                            doctorRequest = null
                            validationFailed = true
                            requestProcessed = true
                            part.dispose()
                            call.respond(
                                HttpStatusCode.BadRequest,
                                BasicApiResponse<Unit>(
                                    success = false,
                                    message = "Invalid JSON format in multipart data: ${e.message}"
                                )
                            )
                            return@forEachPart
                        }
                        part.dispose()
                    }

                    part is PartData.FileItem && (part.name == "photo" || part.name == "nidFront" || part.name == "nidBack") -> {
                        if (!requestProcessed) {
                            fileParts[part.name!!] = part
                        } else if (validationFailed || emailExists) {
                            part.dispose()
                        } else {
                            when (part.name) {
                                "photo" -> photoPath = FileUploadUtil.saveProfileImage(part)
                                "nidFront" -> nidFrontPath = FileUploadUtil.saveNidImage(part, "doctor_front")
                                "nidBack" -> nidBackPath = FileUploadUtil.saveNidImage(part, "doctor_back")
                            }
                            part.dispose()
                        }
                    }

                    else -> part.dispose()
                }
                if (requestProcessed && fileParts.isNotEmpty()) {
                    fileParts.forEach { (name, bufferedPart) ->
                        if (validationFailed || emailExists) {
                            bufferedPart.dispose()
                        } else {
                            when (name) {
                                "photo" -> photoPath = FileUploadUtil.saveProfileImage(bufferedPart)
                                "nidFront" -> nidFrontPath = FileUploadUtil.saveNidImage(bufferedPart, "doctor_front")
                                "nidBack" -> nidBackPath = FileUploadUtil.saveNidImage(bufferedPart, "doctor_back")
                            }
                            bufferedPart.dispose()
                        }
                    }
                    fileParts.clear()
                }
            }

            if (doctorRequest == null || validationFailed) {
                val errorMessage = if (doctorRequest == null) {
                    "Invalid request data. Please check that all required fields are provided and properly formatted."
                } else {
                    val validationResult = doctorService.validateCreateDoctorMultipartRequest(doctorRequest!!)
                    when (validationResult) {
                        is DoctorService.ValidationEvent.ErrorFieldEmpty -> Constants.FIELDS_BLANK
                        is DoctorService.ValidationEvent.ErrorInvalidEmail -> Constants.INVALID_EMAIL
                        is DoctorService.ValidationEvent.ErrorPasswordTooShort -> Constants.PASSWORD_TOO_SHORT
                        is DoctorService.ValidationEvent.ErrorInvalidPhone -> "Invalid phone number format. Please use a valid phone number."
                        is DoctorService.ValidationEvent.ErrorInvalidSpecialization -> "Invalid specialization. Must be one of: PSYCHIATRIST, CLINICAL_PSYCHOLOGIST, COUNSELING_PSYCHOLOGIST, PERINATAL_MENTAL_HEALTH, FAMILY_THERAPIST, TRAUMA_THERAPIST, COGNITIVE_BEHAVIORAL_THERAPIST, GROUP_THERAPIST"
                        else -> "Validation failed"
                    }
                }
                call.respond(
                    HttpStatusCode.BadRequest, BasicApiResponse<Unit>(
                        success = false,
                        message = errorMessage
                    )
                )
                return@post
            }

            if (emailExists) {
                call.respond(
                    BasicApiResponse<Unit>(
                        success = false,
                        message = Constants.DOCTOR_ALREADY_EXISTS
                    )
                )
                return@post
            }
            if (photoPath == null || nidFrontPath == null || nidBackPath == null) {
                call.respond(
                    HttpStatusCode.BadRequest, BasicApiResponse<Unit>(
                        success = false,
                        message = "Failed to save uploaded files"
                    )
                )
                return@post
            }
            val filePaths = UploadedFilePaths(nidFrontPath!!, nidBackPath!!, photoPath!!)
            val doctorId = doctorService.createDoctorMultipart(doctorRequest!!, filePaths)
            if (doctorId != null) {
                val loginRequest = UnifiedLoginRequest(
                    email = doctorRequest!!.email,
                    password = doctorRequest!!.password
                )
                val authResponse = authService.login(loginRequest, jwtIssuer, jwtAudience, jwtSecret)
                
                if (authResponse != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse(
                            success = true,
                            data = authResponse
                        )
                    )
                } else {
                    FileUploadUtil.cleanupUploadedFiles(filePaths)
                    doctorService.deleteDoctor(doctorId)
                    authService.deleteUserByEmail(doctorRequest!!.email)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = "Registration succeeded but login failed. Please try logging in manually."
                        )
                    )
                }
            } else {
                FileUploadUtil.cleanupUploadedFiles(filePaths)

                call.respond(
                    HttpStatusCode.InternalServerError,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = "Registration failed. Please try again."
                    )
                )
            }
        }

        post("/login") {
            val request = try {
                val jsonString = call.receiveText()
                gson.fromJson(jsonString, UnifiedLoginRequest::class.java)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val authResponse = authService.login(request, jwtIssuer, jwtAudience, jwtSecret)
            if (authResponse != null) {
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse(
                        success = true,
                        data = authResponse
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = Constants.INVALID_CREDENTIALS
                    )
                )
            }
        }

        post("/refresh") {
            val request = try {
                val jsonString = call.receiveText()
                gson.fromJson(jsonString, RefreshTokenRequest::class.java)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = "Invalid request format"
                    )
                )
                return@post
            }

            val authResponse = authService.refreshToken(request, jwtIssuer, jwtAudience, jwtSecret)
            if (authResponse != null) {
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse(
                        success = true,
                        data = authResponse
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = "Refresh token is invalid, expired, or idle timeout exceeded. Please login again."
                    )
                )
            }
        }

        post("/logout") {
            val request = try {
                val jsonString = call.receiveText()
                gson.fromJson(jsonString, RefreshTokenRequest::class.java)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = "Invalid request format"
                    )
                )
                return@post
            }

            val success = authService.logout(request.refreshToken)
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse<Unit>(
                    success = success,
                    message = if (success) "Logged out successfully" else "Logout failed"
                )
            )
        }
    }
}

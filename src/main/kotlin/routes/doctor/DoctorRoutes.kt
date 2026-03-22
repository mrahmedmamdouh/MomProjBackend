package com.evelolvetech.routes.doctor

import com.evelolvetech.auth.doctorRoute
import com.evelolvetech.auth.doctorRouteBasic
import com.evelolvetech.auth.getCurrentUserIdSafe
import com.evelolvetech.data.requests.UpdateDoctorMultipartRequest
import com.evelolvetech.data.requests.UpdateDoctorRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.data.responses.DoctorResponse
import com.evelolvetech.service.doctor.DoctorService
import com.evelolvetech.util.Constants
import com.evelolvetech.util.FileUploadUtil
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.google.gson.Gson

fun Route.doctorRoutes(doctorService: DoctorService, gson: Gson) {
    doctorRoute("/api/doctors/profile", doctorService) {
        get {
            val userId = call.getCurrentUserIdSafe()

            val doctor = doctorService.getDoctorById(userId)
            if (doctor != null) {
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse(
                        success = true,
                        data = DoctorResponse.fromDoctor(doctor)
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    BasicApiResponse<Unit>(
                        success = false,
                        message = Constants.USER_NOT_FOUND
                    )
                )
            }
        }

        put {
            val userId = call.getCurrentUserIdSafe()

            val contentType = call.request.contentType()

            if (contentType.match(ContentType.MultiPart.FormData)) {
                val multipart = call.receiveMultipart()
                var updateRequest: UpdateDoctorMultipartRequest? = null
                var photoFile: PartData.FileItem? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            if (part.name == "data") {
                                try {
                                    val jsonString = part.value.trim()
                                    updateRequest = gson.fromJson(jsonString, UpdateDoctorMultipartRequest::class.java)
                                } catch (e: Exception) {
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
                            }
                        }

                        is PartData.FileItem -> {
                            if (part.name == "photo") {
                                val originalFileName = part.originalFileName
                                if (originalFileName != null && originalFileName.substringAfterLast(".", "")
                                        .lowercase() in setOf("jpg", "jpeg", "png", "gif")
                                ) {
                                    photoFile = part
                                } else {
                                    part.dispose()
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        BasicApiResponse<Unit>(
                                            success = false,
                                            message = "Invalid image file type"
                                        )
                                    )
                                    return@forEachPart
                                }
                            }
                        }

                        else -> {}
                    }
                    part.dispose()
                }

                val request = updateRequest ?: run {
                    photoFile?.dispose()
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = "Invalid request data. Please check that all required fields are provided and properly formatted."
                        )
                    )
                    return@put
                }

                when (doctorService.validateUpdateDoctorMultipartRequest(request)) {
                    is DoctorService.ValidationEvent.ErrorFieldEmpty -> {
                        photoFile?.dispose()
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Name cannot be empty"
                            )
                        )
                        return@put
                    }
                    is DoctorService.ValidationEvent.ErrorInvalidPhone -> {
                        photoFile?.dispose()
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Invalid phone number format"
                            )
                        )
                        return@put
                    }
                    is DoctorService.ValidationEvent.ErrorInvalidSpecialization -> {
                        photoFile?.dispose()
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Invalid specialization"
                            )
                        )
                        return@put
                    }
                    is DoctorService.ValidationEvent.ErrorInvalidEmail -> {
                        photoFile?.dispose()
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Invalid email format"
                            )
                        )
                        return@put
                    }
                    is DoctorService.ValidationEvent.ErrorPasswordTooShort -> {
                        photoFile?.dispose()
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Password too short"
                            )
                        )
                        return@put
                    }
                    is DoctorService.ValidationEvent.Success -> {
                    }
                }

                val photoPath: String? = photoFile?.let { file ->
                    FileUploadUtil.saveProfileImage(file)
                }
                photoFile?.dispose()

                val isUpdated = doctorService.updateDoctorMultipart(userId, request, photoPath)
                if (isUpdated) {
                    val updatedDoctor = doctorService.getDoctorById(userId)
                    if (updatedDoctor != null) {
                        call.respond(
                            HttpStatusCode.OK,
                            BasicApiResponse(
                                success = true,
                                data = DoctorResponse.fromDoctor(updatedDoctor)
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = Constants.UNKNOWN_ERROR
                            )
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = Constants.UNKNOWN_ERROR
                        )
                    )
                }
            } else {
                val request = try {
                    val jsonString = call.receiveText()
                    gson.fromJson(jsonString, UpdateDoctorRequest::class.java)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = "Invalid request data. Please check that all required fields are provided and properly formatted."
                        )
                    )
                    return@put
                }

                when (doctorService.validateUpdateDoctorRequest(request)) {
                    is DoctorService.ValidationEvent.ErrorFieldEmpty -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Name cannot be empty"
                            )
                        )
                        return@put
                    }
                    is DoctorService.ValidationEvent.ErrorInvalidPhone -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Invalid phone number format"
                            )
                        )
                        return@put
                    }
                    is DoctorService.ValidationEvent.ErrorInvalidSpecialization -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Invalid specialization"
                            )
                        )
                        return@put
                    }
                    is DoctorService.ValidationEvent.ErrorInvalidEmail -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Invalid email format"
                            )
                        )
                        return@put
                    }
                    is DoctorService.ValidationEvent.ErrorPasswordTooShort -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Password too short"
                            )
                        )
                        return@put
                    }
                    is DoctorService.ValidationEvent.Success -> {
                    }
                }

                val isUpdated = doctorService.updateDoctor(userId, request)
                if (isUpdated) {
                    val updatedDoctor = doctorService.getDoctorById(userId)
                    if (updatedDoctor != null) {
                        call.respond(
                            HttpStatusCode.OK,
                            BasicApiResponse(
                                success = true,
                                data = DoctorResponse.fromDoctor(updatedDoctor)
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = Constants.UNKNOWN_ERROR
                            )
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = Constants.UNKNOWN_ERROR
                        )
                    )
                }
            }
        }
    }

    doctorRouteBasic(doctorService) {
        route("/api/doctors/check-authorization") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val doctorId = call.getCurrentUserIdSafe()

                val doctor = doctorService.getDoctorById(doctorId)
                val isAuthorized = doctor?.isAuthorized ?: false

                call.respond(
                    HttpStatusCode.OK, BasicApiResponse(
                        success = true,
                        data = mapOf(
                            "isAuthorized" to isAuthorized,
                            "doctorId" to doctorId
                        )
                    )
                )
            }
        }
    }
}

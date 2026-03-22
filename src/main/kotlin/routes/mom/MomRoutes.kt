package com.evelolvetech.routes.mom

import com.evelolvetech.auth.momRoute
import com.evelolvetech.auth.momRouteBasic
import com.evelolvetech.auth.getCurrentUserIdSafe
import com.evelolvetech.data.requests.UpdateMomMultipartRequest
import com.evelolvetech.data.requests.UpdateMomRequest
import com.evelolvetech.data.requests.UpdateSessionsRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.data.responses.MomResponse
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.util.Constants
import com.evelolvetech.util.FileUploadUtil
import com.evelolvetech.util.respondWithValidationError
import com.evelolvetech.util.respondWithMapping
import com.evelolvetech.util.respondWithData
import com.evelolvetech.util.respondWithNotFound
import com.evelolvetech.util.respondWithError
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.momRoutes(
    momService: MomService,
    gson: Gson
) {
    momRouteBasic(momService) {
        route("/api/moms/profile") {
            get {
                val userId = call.getCurrentUserIdSafe()

                val mom = momService.getMomById(userId)
                if (mom != null) {
                    call.respondWithData(MomResponse.fromMom(mom))
                } else {
                    call.respondWithNotFound(Constants.USER_NOT_FOUND)
                }
            }

            put {
                val userId = call.getCurrentUserIdSafe()

                val contentType = call.request.contentType()

                if (contentType.match(ContentType.MultiPart.FormData)) {
                val multipart = call.receiveMultipart()
                var updateRequest: UpdateMomMultipartRequest? = null
                var photoFile: PartData.FileItem? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            if (part.name == "data") {
                                try {
                                    val jsonString = part.value.trim()
                                    updateRequest = gson.fromJson(jsonString, UpdateMomMultipartRequest::class.java)
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
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@put
                }

                val photoPath: String? = photoFile?.let { file ->
                    FileUploadUtil.saveProfileImage(file)
                }
                photoFile?.dispose()

                val isUpdated = momService.updateMomMultipart(userId, request, photoPath)
                if (isUpdated) {
                    val updatedMom = momService.getMomById(userId)
                    if (updatedMom != null) {
                        call.respond(
                            HttpStatusCode.OK,
                            BasicApiResponse(
                                success = true,
                                data = MomResponse.fromMom(updatedMom)
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
                    gson.fromJson(jsonString, UpdateMomRequest::class.java)
                } catch (e: Exception) {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@put
                }

                val isUpdated = momService.updateMom(userId, request)
                if (isUpdated) {
                    val updatedMom = momService.getMomById(userId)
                    if (updatedMom != null) {
                        call.respond(
                            HttpStatusCode.OK,
                            BasicApiResponse(
                                success = true,
                                data = MomResponse.fromMom(updatedMom)
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
    }

    momRouteBasic(momService) {
        route("/api/moms/sessions") {
            put {
                val userId = call.getCurrentUserIdSafe()

                val request = try {
                    val jsonString = call.receiveText()
                    gson.fromJson(jsonString, UpdateSessionsRequest::class.java)
                } catch (e: Exception) {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@put
                }

                val numberOfSessions = request.numberOfSessions ?: run {
                    call.respondWithValidationError("numberOfSessions is required")
                    return@put
                }

                val isUpdated = momService.updateMomSessions(userId, numberOfSessions)
                if (isUpdated) {
                    call.respondWithData(emptyMap<String, Any>(), message = Constants.SUCCESS)
                } else {
                    call.respondWithError(Constants.UNKNOWN_ERROR, HttpStatusCode.InternalServerError)
                }
            }
        }
    }

    momRouteBasic(momService) {
        route("/api/moms/check-authorization") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val momId = call.getCurrentUserIdSafe()

                val mom = momService.getMomById(momId)
                val isAuthorized = mom?.isAuthorized ?: false

                val data = mapOf(
                    "isAuthorized" to isAuthorized,
                    "momId" to momId
                )
                
                call.respondWithData(data)
            }
        }
    }
}
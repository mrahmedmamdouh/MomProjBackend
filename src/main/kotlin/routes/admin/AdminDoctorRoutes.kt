package com.evelolvetech.routes.admin

import com.evelolvetech.auth.adminRoute
import com.evelolvetech.data.requests.AuthorizationRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.service.doctor.DoctorService
import com.evelolvetech.util.Constants
import com.evelolvetech.util.HttpStatusMapper
import com.evelolvetech.util.respondWithMapping
import com.evelolvetech.util.respondWithValidationError
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminDoctorRoutes(doctorService: DoctorService, gson: Gson) {
    route("/api/admin/doctors") {
        adminRoute {
            put("/{doctorId}/authorize") {
                val doctorId = call.parameters["doctorId"]
                if (doctorId.isNullOrBlank()) {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@put
                }

                val request = try {
                    val jsonString = call.receiveText()
                    gson.fromJson(jsonString, AuthorizationRequest::class.java)
                } catch (e: Exception) {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@put
                }

                val result = doctorService.updateDoctorAuthorization(doctorId, request.isAuthorized)
                call.respondWithMapping(result)
            }

            get("/{doctorId}/status") {
                val doctorId = call.parameters["doctorId"]
                if (doctorId.isNullOrBlank()) {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@get
                }

                val result = doctorService.getDoctorStatus(doctorId)
                call.respondWithMapping(result)
            }
        }
    }
}

package com.evelolvetech.routes.session

import com.evelolvetech.auth.getCurrentUserIdSafe
import com.evelolvetech.auth.doctorRouteBasic
import com.evelolvetech.data.requests.CompleteSessionRequest
import com.evelolvetech.data.requests.CreateGroupSessionRequest
import com.evelolvetech.service.doctor.DoctorService
import com.evelolvetech.service.session.GroupSessionService
import com.evelolvetech.util.respondWithError
import com.evelolvetech.util.respondWithMapping
import com.evelolvetech.util.respondWithValidationError
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.doctorSessionRoutes(sessionService: GroupSessionService, doctorService: DoctorService) {

    doctorRouteBasic(doctorService) {

        route("/api/doctor/sessions") {

            get {
                try {
                    val doctorId = call.getCurrentUserIdSafe()
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                    val result = sessionService.getDoctorSessions(doctorId, page, size)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error retrieving sessions: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            get("/{id}/bookings") {
                try {
                    val doctorId = call.getCurrentUserIdSafe()
                    val sessionId = call.parameters["id"] ?: run {
                        call.respondWithValidationError("Session ID is required")
                        return@get
                    }
                    val result = sessionService.getSessionBookings(doctorId, sessionId)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error retrieving bookings: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            post {
                try {
                    val doctorId = call.getCurrentUserIdSafe()
                    val request = call.receive<CreateGroupSessionRequest>()
                    val result = sessionService.createSession(doctorId, request)
                    call.respondWithMapping(result, statusCode = HttpStatusCode.Created)
                } catch (e: Exception) {
                    call.respondWithError("Error creating session: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            post("/{id}/complete") {
                try {
                    val doctorId = call.getCurrentUserIdSafe()
                    val sessionId = call.parameters["id"] ?: run {
                        call.respondWithValidationError("Session ID is required")
                        return@post
                    }
                    val request = call.receive<CompleteSessionRequest>()
                    val result = sessionService.completeSession(doctorId, sessionId, request)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error completing session: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            delete("/{id}") {
                try {
                    val doctorId = call.getCurrentUserIdSafe()
                    val sessionId = call.parameters["id"] ?: run {
                        call.respondWithValidationError("Session ID is required")
                        return@delete
                    }
                    val result = sessionService.cancelSession(doctorId, sessionId)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error cancelling session: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

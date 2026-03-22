package com.evelolvetech.routes.session

import com.evelolvetech.auth.getCurrentUserIdSafe
import com.evelolvetech.auth.momRouteBasic
import com.evelolvetech.data.requests.BookSessionRequest
import com.evelolvetech.data.requests.SessionFeedbackRequest
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.service.session.GroupSessionService
import com.evelolvetech.util.respondWithError
import com.evelolvetech.util.respondWithMapping
import com.evelolvetech.util.respondWithValidationError
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.momSessionRoutes(sessionService: GroupSessionService, momService: MomService) {

    momRouteBasic(momService) {

        route("/api/sessions") {

            get {
                try {
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                    val city = call.request.queryParameters["city"]

                    val result = if (!city.isNullOrBlank()) {
                        sessionService.getUpcomingSessionsByCity(city, page, size)
                    } else {
                        sessionService.getUpcomingSessions(page, size)
                    }
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error retrieving sessions: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            get("/{id}") {
                try {
                    val sessionId = call.parameters["id"] ?: run {
                        call.respondWithValidationError("Session ID is required")
                        return@get
                    }
                    val result = sessionService.getSessionById(sessionId)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error retrieving session: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            get("/search") {
                try {
                    val query = call.request.queryParameters["q"] ?: ""
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20

                    if (query.isBlank()) {
                        call.respondWithValidationError("Search query is required")
                        return@get
                    }

                    val result = sessionService.searchSessions(query, page, size)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error searching sessions: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            post("/book") {
                try {
                    val momId = call.getCurrentUserIdSafe()
                    val request = call.receive<BookSessionRequest>()

                    if (request.sessionId.isBlank()) {
                        call.respondWithValidationError("Session ID is required")
                        return@post
                    }

                    val result = sessionService.bookSession(momId, request.sessionId)
                    call.respondWithMapping(result, statusCode = HttpStatusCode.Created)
                } catch (e: Exception) {
                    call.respondWithError("Error booking session: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            delete("/bookings/{id}") {
                try {
                    val momId = call.getCurrentUserIdSafe()
                    val bookingId = call.parameters["id"] ?: run {
                        call.respondWithValidationError("Booking ID is required")
                        return@delete
                    }
                    val result = sessionService.cancelBooking(momId, bookingId)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error cancelling booking: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            get("/my-bookings") {
                try {
                    val momId = call.getCurrentUserIdSafe()
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                    val result = sessionService.getMyBookings(momId, page, size)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error retrieving bookings: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            get("/my-summary") {
                try {
                    val momId = call.getCurrentUserIdSafe()
                    val result = sessionService.getMySessionSummary(momId)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error retrieving summary: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            post("/bookings/{id}/feedback") {
                try {
                    val momId = call.getCurrentUserIdSafe()
                    val bookingId = call.parameters["id"] ?: run {
                        call.respondWithValidationError("Booking ID is required")
                        return@post
                    }
                    val request = call.receive<SessionFeedbackRequest>()
                    val result = sessionService.addFeedback(momId, bookingId, request.rating, request.comment)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error submitting feedback: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

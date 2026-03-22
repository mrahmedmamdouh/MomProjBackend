package com.evelolvetech.routes.streaming

import com.evelolvetech.auth.doctorRoute
import com.evelolvetech.auth.getCurrentUserIdSafe
import com.evelolvetech.auth.momRouteBasic
import com.evelolvetech.service.doctor.DoctorService
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.service.streaming.StreamingService
import com.evelolvetech.util.respondWithError
import com.evelolvetech.util.respondWithMapping
import io.ktor.http.*
import io.ktor.server.routing.*

fun Route.doctorStreamingRoutes(
    streamingService: StreamingService,
    doctorService: DoctorService
) {
    doctorRoute(doctorService) {
        route("/api/streaming") {
            post("/start/{sessionId}") {
                try {
                    val doctorId = call.getCurrentUserIdSafe()
                    val sessionId = call.parameters["sessionId"] ?: run {
                        call.respondWithError("Session ID required", HttpStatusCode.BadRequest)
                        return@post
                    }
                    val result = streamingService.doctorStartLiveSession(doctorId, sessionId)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            post("/end/{sessionId}") {
                try {
                    val doctorId = call.getCurrentUserIdSafe()
                    val sessionId = call.parameters["sessionId"] ?: run {
                        call.respondWithError("Session ID required", HttpStatusCode.BadRequest)
                        return@post
                    }
                    val result = streamingService.doctorEndLiveSession(doctorId, sessionId)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

fun Route.momStreamingRoutes(
    streamingService: StreamingService,
    momService: MomService
) {
    momRouteBasic(momService) {
        route("/api/streaming") {
            post("/join/{sessionId}") {
                try {
                    val momId = call.getCurrentUserIdSafe()
                    val sessionId = call.parameters["sessionId"] ?: run {
                        call.respondWithError("Session ID required", HttpStatusCode.BadRequest)
                        return@post
                    }
                    val result = streamingService.momJoinLiveSession(momId, sessionId)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            get("/room/{sessionId}") {
                try {
                    val sessionId = call.parameters["sessionId"] ?: run {
                        call.respondWithError("Session ID required", HttpStatusCode.BadRequest)
                        return@get
                    }
                    val result = streamingService.getSessionRoomInfo(sessionId)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

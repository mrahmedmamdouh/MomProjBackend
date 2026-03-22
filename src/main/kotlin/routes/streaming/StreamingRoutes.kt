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
    doctorRoute("/api/streaming", doctorService) {
        post("/start/{sessionId}") {
            try {
                val doctorId = call.getCurrentUserIdSafe()
                val sessionId = call.parameters["sessionId"] ?: run {
                    call.respondWithError("Session ID required", HttpStatusCode.BadRequest)
                    return@post
                }
                call.respondWithMapping(streamingService.doctorStartLiveSession(doctorId, sessionId))
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
                call.respondWithMapping(streamingService.doctorEndLiveSession(doctorId, sessionId))
            } catch (e: Exception) {
                call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
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
                    call.respondWithMapping(streamingService.momJoinLiveSession(momId, sessionId))
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
                    call.respondWithMapping(streamingService.getSessionRoomInfo(sessionId))
                } catch (e: Exception) {
                    call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

package com.evelolvetech.routes.admin

import com.evelolvetech.auth.adminRoute
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.util.respondWithMapping
import com.evelolvetech.util.respondWithValidationError
import com.evelolvetech.util.respondWithNotFound
import com.evelolvetech.util.respondWithError
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminMomRoutes(momService: MomService, gson: Gson) {
    adminRoute {
        route("/api/admin/moms") {
            get("/{momId}/sessions") {
                try {
                    val momId = call.parameters["momId"]?.takeIf { it.isNotBlank() } ?: run {
                        call.respondWithValidationError("Mom ID is required")
                        return@get
                    }

                    val mom = momService.getMomById(momId)
                    if (mom == null) {
                        call.respondWithNotFound("Mom not found")
                        return@get
                    }

                    val sessionData = mapOf(
                        "momId" to mom.id,
                        "sessionCount" to mom.numberOfSessions,
                        "lastActive" to mom.createdAt,
                        "isAuthorized" to mom.isAuthorized
                    )

                    val result = BasicApiResponse(
                        success = true,
                        data = sessionData
                    )

                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error retrieving mom session data: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

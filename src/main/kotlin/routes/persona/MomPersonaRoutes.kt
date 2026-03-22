package com.evelolvetech.routes.persona

import com.evelolvetech.auth.getCurrentUserIdSafe
import com.evelolvetech.auth.momRouteBasic
import com.evelolvetech.data.requests.ExtractPersonaRequest
import com.evelolvetech.data.requests.UpdatePersonaFieldRequest
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.service.persona.CircleFormationService
import com.evelolvetech.service.persona.ClusteringService
import com.evelolvetech.service.persona.PersonaService
import com.evelolvetech.util.respondWithError
import com.evelolvetech.util.respondWithMapping
import com.evelolvetech.util.respondWithValidationError
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.momPersonaRoutes(
    personaService: PersonaService,
    clusteringService: ClusteringService,
    circleFormationService: CircleFormationService,
    momService: MomService
) {
    momRouteBasic(momService) {

        route("/api/persona") {

            get {
                try {
                    val momId = call.getCurrentUserIdSafe()
                    val result = personaService.getPersonaStatus(momId)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            post("/update") {
                try {
                    val momId = call.getCurrentUserIdSafe()
                    val request = call.receive<UpdatePersonaFieldRequest>()

                    if (request.field.isBlank() || request.value.isBlank()) {
                        call.respondWithValidationError("Field and value are required")
                        return@post
                    }

                    val result = personaService.updatePersonaField(momId, request.field, request.value)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            post("/extract") {
                try {
                    val momId = call.getCurrentUserIdSafe()
                    val request = call.receive<ExtractPersonaRequest>()

                    if (request.message.isBlank()) {
                        call.respondWithValidationError("Message is required")
                        return@post
                    }

                    val result = personaService.extractFromMessage(momId, request.message)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            post("/cluster") {
                try {
                    val momId = call.getCurrentUserIdSafe()
                    val clusterId = clusteringService.assignToCluster(momId)

                    if (clusterId != null) {
                        val result = personaService.getPersonaStatus(momId)
                        call.respondWithMapping(result)
                    } else {
                        call.respondWithError("Persona must be complete before clustering", HttpStatusCode.BadRequest)
                    }
                } catch (e: Exception) {
                    call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }
        }

        route("/api/circles") {

            get("/mine") {
                try {
                    val momId = call.getCurrentUserIdSafe()
                    val result = circleFormationService.getMyCircles(momId)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            get("/recommendations") {
                try {
                    val momId = call.getCurrentUserIdSafe()
                    val max = call.request.queryParameters["max"]?.toIntOrNull() ?: 5
                    val result = circleFormationService.getRecommendedCircles(momId, max)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            post("/{id}/join") {
                try {
                    val momId = call.getCurrentUserIdSafe()
                    val circleId = call.parameters["id"] ?: run {
                        call.respondWithValidationError("Circle ID is required")
                        return@post
                    }
                    val result = circleFormationService.joinCircle(momId, circleId)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }

            delete("/{id}/leave") {
                try {
                    val momId = call.getCurrentUserIdSafe()
                    val circleId = call.parameters["id"] ?: run {
                        call.respondWithValidationError("Circle ID is required")
                        return@delete
                    }
                    val result = circleFormationService.leaveCircle(momId, circleId)
                    call.respondWithMapping(result)
                } catch (e: Exception) {
                    call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

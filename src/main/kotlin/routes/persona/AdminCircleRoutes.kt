package com.evelolvetech.routes.persona

import com.evelolvetech.auth.adminRoute
import com.evelolvetech.data.requests.FormCirclesFromClusterRequest
import com.evelolvetech.data.requests.FormCirclesRequest
import com.evelolvetech.service.persona.CircleFormationService
import com.evelolvetech.service.persona.ClusteringService
import com.evelolvetech.util.respondWithError
import com.evelolvetech.util.respondWithMapping
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.adminCircleRoutes(
    clusteringService: ClusteringService,
    circleFormationService: CircleFormationService
) {
    adminRoute("/api/admin/circles") {

        post("/form") {
            try {
                val request = call.receive<FormCirclesRequest>()
                val result = circleFormationService.formAllCircles(request.numClusters, request.maxCircleSize)
                call.respondWithMapping(result, statusCode = HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        post("/form-from-cluster") {
            try {
                val request = call.receive<FormCirclesFromClusterRequest>()
                val result = circleFormationService.formCirclesFromCluster(request.clusterId, request.maxCircleSize)
                call.respondWithMapping(result, statusCode = HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        post("/recluster") {
            try {
                val numClusters = call.request.queryParameters["clusters"]?.toIntOrNull() ?: 5
                val results = clusteringService.clusterAllMoms(numClusters)
                call.respondWithMapping(
                    com.evelolvetech.data.responses.BasicApiResponse(
                        success = true,
                        data = results.map {
                            mapOf(
                                "clusterId" to it.clusterId,
                                "memberCount" to it.memberIds.size,
                                "description" to it.description
                            )
                        },
                        message = "Re-clustering complete: ${results.size} clusters formed"
                    )
                )
            } catch (e: Exception) {
                call.respondWithError("Error: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }
    }
}

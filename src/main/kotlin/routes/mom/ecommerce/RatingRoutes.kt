package com.evelolvetech.routes.mom.ecommerce

import com.evelolvetech.auth.getCurrentUserIdSafe
import com.evelolvetech.auth.momRoute
import com.evelolvetech.data.requests.CreateRatingRequest
import com.evelolvetech.data.requests.UpdateRatingRequest
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.service.mom.ecommerce.ProductRatingService
import com.evelolvetech.util.respondWithError
import com.evelolvetech.util.respondWithMapping
import com.evelolvetech.util.respondWithValidationError
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.ratingRoutes(ratingService: ProductRatingService, momService: MomService) {

    momRoute("/api/ratings", momService) {

        get("/product/{productId}") {
            try {
                val productId = call.parameters["productId"] ?: run {
                    call.respondWithValidationError("Product ID is required")
                    return@get
                }
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20

                if (page < 0) {
                    call.respondWithValidationError("Page must be non-negative")
                    return@get
                }
                if (size <= 0 || size > 100) {
                    call.respondWithValidationError("Size must be between 1 and 100")
                    return@get
                }

                val result = ratingService.getRatingsByProductId(productId, page, size)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error retrieving ratings: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        get("/product/{productId}/mine") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val productId = call.parameters["productId"] ?: run {
                    call.respondWithValidationError("Product ID is required")
                    return@get
                }

                val result = ratingService.getUserRatingForProduct(productId, momId)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error retrieving your rating: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        get("/mine") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val result = ratingService.getMyRatings(momId)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error retrieving your ratings: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        post {
            try {
                val momId = call.getCurrentUserIdSafe()
                val request = call.receive<CreateRatingRequest>()

                if (request.productId.isBlank()) {
                    call.respondWithValidationError("Product ID is required")
                    return@post
                }

                val result = ratingService.createRating(
                    momId = momId,
                    productId = request.productId,
                    rating = request.rating,
                    title = request.title ?: "",
                    comment = request.comment ?: ""
                )
                call.respondWithMapping(result, statusCode = HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respondWithError("Error creating rating: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        put("/{id}") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val ratingId = call.parameters["id"] ?: run {
                    call.respondWithValidationError("Rating ID is required")
                    return@put
                }

                val request = call.receive<UpdateRatingRequest>()
                val result = ratingService.updateRating(
                    ratingId = ratingId,
                    momId = momId,
                    rating = request.rating,
                    title = request.title,
                    comment = request.comment
                )
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error updating rating: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        delete("/{id}") {
            try {
                val momId = call.getCurrentUserIdSafe()
                val ratingId = call.parameters["id"] ?: run {
                    call.respondWithValidationError("Rating ID is required")
                    return@delete
                }

                val result = ratingService.deleteRating(ratingId, momId)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error deleting rating: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }
    }
}

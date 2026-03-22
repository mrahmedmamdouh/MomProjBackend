package com.evelolvetech.routes.admin

import com.evelolvetech.auth.adminRoute
import com.evelolvetech.data.requests.CreateSkuOfferRequest
import com.evelolvetech.data.requests.UpdateSkuOfferRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.service.mom.ecommerce.SkuOfferService
import com.evelolvetech.util.Constants
import com.evelolvetech.util.HttpStatusMapper
import com.evelolvetech.util.respondWithMapping
import com.evelolvetech.util.respondWithValidationError
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminSkuOfferRoutes(skuOfferService: SkuOfferService, gson: Gson) {
    route("/api/admin/sku-offers") {
        adminRoute {
            post {
                val request = try {
                    val jsonString = call.receiveText()
                    gson.fromJson(jsonString, CreateSkuOfferRequest::class.java)
                } catch (e: Exception) {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@post
                }

                val result = skuOfferService.createSkuOfferForAdmin(request)
                call.respond(
                    HttpStatusMapper.mapToHttpStatus(result),
                    result
                )
            }

            put("/{id}") {
                val offerId = call.parameters["id"] ?: run {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@put
                }

                val request = try {
                    val jsonString = call.receiveText()
                    gson.fromJson(jsonString, UpdateSkuOfferRequest::class.java)
                } catch (e: Exception) {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@put
                }

                val result = skuOfferService.updateSkuOfferForAdmin(offerId, request)
                call.respondWithMapping(result)
            }

            delete("/{id}") {
                val offerId = call.parameters["id"] ?: run {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@delete
                }

                val result = skuOfferService.deleteSkuOfferForAdmin(offerId)
                call.respondWithMapping(result)
            }
        }
    }
}

package com.evelolvetech.routes.mom.ecommerce

import com.evelolvetech.auth.momRoute
import com.evelolvetech.auth.getCurrentUserIdSafe
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.service.mom.ecommerce.SkuOfferService
import com.evelolvetech.util.Constants
import com.evelolvetech.util.HttpStatusMapper
import com.evelolvetech.util.respondWithMapping
import com.evelolvetech.util.respondWithValidationError
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.skuOfferRoutes(skuOfferService: SkuOfferService, momService: MomService) {
    route("/api/sku-offers") {
        momRoute(momService) {
            get {
                val userId = call.getCurrentUserIdSafe()
                
                val result = skuOfferService.getActiveSkuOffersForMom(userId)
                call.respondWithMapping(result)
            }

            get("/{id}") {
                val userId = call.getCurrentUserIdSafe()
                
                val offerId = call.parameters["id"] ?: run {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@get
                }

                val result = skuOfferService.getSkuOfferByIdForMom(userId, offerId)
                call.respondWithMapping(result)
            }

            get("/sku/{skuId}") {
                val userId = call.getCurrentUserIdSafe()
                
                val skuId = call.parameters["skuId"] ?: run {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@get
                }

                val result = skuOfferService.getSkuOffersBySkuIdForMom(userId, skuId)
                call.respondWithMapping(result)
            }

            get("/seller/{sellerId}") {
                val userId = call.getCurrentUserIdSafe()
                
                val sellerId = call.parameters["sellerId"] ?: run {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@get
                }

                val result = skuOfferService.getSkuOffersBySellerIdForMom(userId, sellerId)
                call.respondWithMapping(result)
            }

            get("/sku/{skuId}/best") {
                val userId = call.getCurrentUserIdSafe()
                
                val skuId = call.parameters["skuId"] ?: run {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@get
                }

                val result = skuOfferService.getBestOfferForSkuForMom(userId, skuId)
                call.respondWithMapping(result)
            }
        }
    }
}
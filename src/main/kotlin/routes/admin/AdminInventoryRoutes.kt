package com.evelolvetech.routes.admin

import com.evelolvetech.auth.adminRoute
import com.evelolvetech.data.requests.CreateInventoryRequest
import com.evelolvetech.data.requests.ReleaseInventoryRequest
import com.evelolvetech.data.requests.ReserveInventoryRequest
import com.evelolvetech.data.requests.UpdateInventoryRequest
import com.evelolvetech.service.mom.ecommerce.InventoryService
import com.evelolvetech.util.respondWithError
import com.evelolvetech.util.respondWithMapping
import com.evelolvetech.util.respondWithValidationError
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.adminInventoryRoutes(inventoryService: InventoryService) {

    adminRoute("/api/admin/inventory") {

        get("/{skuId}") {
            try {
                val skuId = call.parameters["skuId"] ?: run {
                    call.respondWithValidationError("SKU ID is required")
                    return@get
                }

                val result = inventoryService.getInventoryBySkuId(skuId)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error retrieving inventory: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        get("/low-stock") {
            try {
                val threshold = call.request.queryParameters["threshold"]?.toIntOrNull() ?: 10
                val result = inventoryService.getLowStockItems(threshold)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error retrieving low stock items: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        post {
            try {
                val request = call.receive<CreateInventoryRequest>()

                if (request.skuId.isBlank()) {
                    call.respondWithValidationError("SKU ID is required")
                    return@post
                }

                val result = inventoryService.createInventory(request.skuId, request.onHand)
                call.respondWithMapping(result, statusCode = HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respondWithError("Error creating inventory: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        put("/{skuId}") {
            try {
                val skuId = call.parameters["skuId"] ?: run {
                    call.respondWithValidationError("SKU ID is required")
                    return@put
                }

                val request = call.receive<UpdateInventoryRequest>()
                val result = inventoryService.updateStock(skuId, request.onHand, request.reserved)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error updating inventory: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        post("/reserve") {
            try {
                val request = call.receive<ReserveInventoryRequest>()

                if (request.skuId.isBlank()) {
                    call.respondWithValidationError("SKU ID is required")
                    return@post
                }
                if (request.quantity <= 0) {
                    call.respondWithValidationError("Quantity must be greater than zero")
                    return@post
                }

                val result = inventoryService.reserveStock(request.skuId, request.quantity)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error reserving inventory: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }

        post("/release") {
            try {
                val request = call.receive<ReleaseInventoryRequest>()

                if (request.skuId.isBlank()) {
                    call.respondWithValidationError("SKU ID is required")
                    return@post
                }
                if (request.quantity <= 0) {
                    call.respondWithValidationError("Quantity must be greater than zero")
                    return@post
                }

                val result = inventoryService.releaseStock(request.skuId, request.quantity)
                call.respondWithMapping(result)
            } catch (e: Exception) {
                call.respondWithError("Error releasing inventory: ${e.message}", HttpStatusCode.InternalServerError)
            }
        }
    }
}

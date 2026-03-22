package com.evelolvetech.routes.mom.ecommerce

import com.evelolvetech.auth.momRoute
import com.evelolvetech.auth.getCurrentUserIdSafe
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.service.mom.MomService
import com.evelolvetech.service.mom.ecommerce.ProductService
import com.evelolvetech.util.Constants
import com.evelolvetech.util.HttpStatusMapper
import com.evelolvetech.util.respondWithMapping
import com.evelolvetech.util.respondWithValidationError
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRoutes(productService: ProductService, momService: MomService) {
    route("/api/products") {
        momRoute(momService) {
            get {
                val userId = call.getCurrentUserIdSafe()
                
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20

                val result = productService.getAllProductsForMom(userId, page, size)
                call.respondWithMapping(result)
            }
            
            get("/{id}") {
                val userId = call.getCurrentUserIdSafe()
                
                val productId = call.parameters["id"] ?: run {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@get
                }

                val result = productService.getProductByIdForMom(userId, productId)
                call.respondWithMapping(result)
            }

            get("/category/{categoryId}") {
                val userId = call.getCurrentUserIdSafe()
                
                val categoryId = call.parameters["categoryId"] ?: run {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@get
                }

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20

                val result = productService.getProductsByCategoryForMom(userId, categoryId, page, size)
                call.respondWithMapping(result)
            }

            get("/search") {
                val userId = call.getCurrentUserIdSafe()
                
                val query = call.request.queryParameters["query"] ?: run {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@get
                }

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20

                val result = productService.searchProductsForMom(userId, query, page, size)
                call.respondWithMapping(result)
            }
        }
    }
}
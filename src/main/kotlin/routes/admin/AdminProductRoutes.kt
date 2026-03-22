package com.evelolvetech.routes.admin

import com.evelolvetech.auth.adminRoute
import com.evelolvetech.data.requests.CreateProductRequest
import com.evelolvetech.data.requests.UpdateProductRequest
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.service.mom.ecommerce.ProductService
import com.evelolvetech.util.Constants
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminProductRoutes(productService: ProductService, gson: Gson) {
    route("/api/admin/products") {
        adminRoute {
            post {
                val request = try {
                    val jsonString = call.receiveText()
                    gson.fromJson(jsonString, CreateProductRequest::class.java)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = Constants.INVALID_REQUEST_DATA
                        )
                    )
                    return@post
                }

                when (productService.validateCreateProductRequest(request)) {
                    is ProductService.ValidationEvent.ErrorFieldEmpty -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = Constants.FIELDS_BLANK
                            )
                        )
                    }

                    is ProductService.ValidationEvent.ErrorInvalidSeller -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Invalid seller ID"
                            )
                        )
                    }

                    is ProductService.ValidationEvent.ErrorNoCategories -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "At least one category is required"
                            )
                        )
                    }

                    is ProductService.ValidationEvent.ErrorInvalidCategories -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Invalid category ID(s)"
                            )
                        )
                    }

                    is ProductService.ValidationEvent.ErrorInvalidSessions -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Minimum sessions must be non-negative"
                            )
                        )
                    }

                    is ProductService.ValidationEvent.Success -> {
                        val isSuccessful = productService.createProduct(request)
                        if (isSuccessful) {
                            call.respond(
                                HttpStatusCode.OK,
                                BasicApiResponse<Unit>(
                                    success = true,
                                    message = Constants.SUCCESS
                                )
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.OK,
                                BasicApiResponse<Unit>(
                                    success = false,
                                    message = Constants.UNKNOWN_ERROR
                                )
                            )
                        }
                    }
                }
            }

            put("/{id}") {
                val productId = call.parameters["id"] ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = Constants.INVALID_REQUEST_DATA
                        )
                    )
                    return@put
                }

                val request = try {
                    val jsonString = call.receiveText()
                    gson.fromJson(jsonString, UpdateProductRequest::class.java)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = Constants.INVALID_REQUEST_DATA
                        )
                    )
                    return@put
                }

                when (productService.validateUpdateProductRequest(request)) {
                    is ProductService.ValidationEvent.ErrorFieldEmpty -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = Constants.FIELDS_BLANK
                            )
                        )
                    }

                    is ProductService.ValidationEvent.ErrorInvalidSeller -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Invalid seller ID"
                            )
                        )
                    }

                    is ProductService.ValidationEvent.ErrorInvalidSessions -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = "Minimum sessions must be non-negative"
                            )
                        )
                    }

                    is ProductService.ValidationEvent.Success -> {
                        val isSuccessful = productService.updateProduct(productId, request)
                        if (isSuccessful) {
                            call.respond(
                                HttpStatusCode.OK,
                                BasicApiResponse<Unit>(
                                    success = true,
                                    message = Constants.SUCCESS
                                )
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.OK,
                                BasicApiResponse<Unit>(
                                    success = false,
                                    message = Constants.UNKNOWN_ERROR
                                )
                            )
                        }
                    }

                    else -> {
                        call.respond(
                            HttpStatusCode.OK,
                            BasicApiResponse<Unit>(
                                success = false,
                                message = Constants.UNKNOWN_ERROR
                            )
                        )
                    }
                }
            }

            delete("/{id}") {
                val productId = call.parameters["id"] ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = Constants.INVALID_REQUEST_DATA
                        )
                    )
                    return@delete
                }

                val isSuccessful = productService.deleteProduct(productId)
                if (isSuccessful) {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse<Unit>(
                            success = true,
                            message = Constants.SUCCESS
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse<Unit>(
                            success = false,
                            message = Constants.UNKNOWN_ERROR
                        )
                    )
                }
            }
        }
    }
}

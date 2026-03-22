package com.evelolvetech.routes.admin

import com.evelolvetech.auth.adminRoute
import com.evelolvetech.data.requests.CreateCategoryRequest
import com.evelolvetech.data.requests.UpdateCategoryRequest
import com.evelolvetech.service.mom.ecommerce.CategoryService
import com.evelolvetech.util.Constants
import com.evelolvetech.util.respondWithValidationError
import com.evelolvetech.util.respondWithCreated
import com.evelolvetech.util.respondWithError
import com.evelolvetech.util.respondWithSuccess
import com.evelolvetech.util.respondWithConflict
import io.ktor.http.*
import com.google.gson.Gson
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.adminCategoryRoutes(categoryService: CategoryService, gson: Gson) {
    route("/api/admin/categories") {
        adminRoute {
            post {
                val request = try {
                    val jsonString = call.receiveText()
                    gson.fromJson(jsonString, CreateCategoryRequest::class.java)
                } catch (e: Exception) {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@post
                }

                when (categoryService.validateCreateCategoryRequest(request)) {
                    is CategoryService.ValidationEvent.ErrorFieldEmpty -> {
                        call.respondWithValidationError(Constants.FIELDS_BLANK)
                    }

                    is CategoryService.ValidationEvent.ErrorDuplicateName -> {
                        call.respondWithConflict("Category with this name already exists")
                    }

                    is CategoryService.ValidationEvent.ErrorDuplicateSlug -> {
                        call.respondWithConflict("Category with this slug already exists")
                    }

                    is CategoryService.ValidationEvent.Success -> {
                        val createdCategory = categoryService.createCategory(request)
                        if (createdCategory != null) {
                            call.respondWithCreated(
                                data = createdCategory,
                                message = "Category created successfully"
                            )
                        } else {
                            call.respondWithError(Constants.UNKNOWN_ERROR, HttpStatusCode.InternalServerError)
                        }
                    }
                }
            }

            put("/{id}") {
                val categoryId = call.parameters["id"] ?: run {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@put
                }

                val request = try {
                    val jsonString = call.receiveText()
                    gson.fromJson(jsonString, UpdateCategoryRequest::class.java)
                } catch (e: Exception) {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@put
                }

                val isSuccessful = categoryService.updateCategory(categoryId, request)
                if (isSuccessful) {
                    call.respondWithSuccess(message = Constants.SUCCESS)
                } else {
                    call.respondWithError(Constants.UNKNOWN_ERROR)
                }
            }

            delete("/{id}") {
                val categoryId = call.parameters["id"] ?: run {
                    call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                    return@delete
                }

                val isSuccessful = categoryService.deleteCategory(categoryId)
                if (isSuccessful) {
                    call.respondWithSuccess(message = Constants.SUCCESS)
                } else {
                    call.respondWithError(Constants.UNKNOWN_ERROR)
                }
            }
        }
    }
}

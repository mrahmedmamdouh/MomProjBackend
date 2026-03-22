package com.evelolvetech.routes.mom.ecommerce

import com.evelolvetech.service.mom.ecommerce.CategoryService
import com.evelolvetech.util.Constants
import com.evelolvetech.util.respondWithData
import com.evelolvetech.util.respondWithValidationError
import com.evelolvetech.util.respondWithNotFound
import io.ktor.server.routing.*

fun Route.categoryRoutes(categoryService: CategoryService) {
    route("/api/categories") {
        get {
            val categories = categoryService.getAllCategories()
            call.respondWithData(categories)
        }

        get("/{id}") {
            val categoryId = call.parameters["id"] ?: run {
                call.respondWithValidationError(Constants.INVALID_REQUEST_DATA)
                return@get
            }

            val category = categoryService.getCategoryById(categoryId)
            if (category != null) {
                call.respondWithData(category)
            } else {
                call.respondWithNotFound(Constants.NOT_FOUND)
            }
        }
    }
}
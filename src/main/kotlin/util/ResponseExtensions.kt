package com.evelolvetech.util

import com.evelolvetech.data.responses.BasicApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun ApplicationCall.respondWithSuccess(
    data: Any? = null,
    message: String? = null,
    statusCode: HttpStatusCode = HttpStatusCode.OK
) {
    respond(
        statusCode,
        BasicApiResponse(
            success = true,
            message = message,
            data = data
        )
    )
}

suspend fun ApplicationCall.respondWithError(
    message: String,
    statusCode: HttpStatusCode = HttpStatusCode.BadRequest,
    data: Any? = null
) {
    respond(
        statusCode,
        BasicApiResponse(
            success = false,
            message = message,
            data = data
        )
    )
}

suspend fun ApplicationCall.respondWithData(
    data: Any,
    message: String? = null,
    statusCode: HttpStatusCode = HttpStatusCode.OK
) {
    respond(
        statusCode,
        BasicApiResponse(
            success = true,
            message = message,
            data = data
        )
    )
}

suspend fun ApplicationCall.respondWithCreated(
    data: Any? = null,
    message: String? = "Resource created successfully"
) {
    respondWithSuccess(data, message, HttpStatusCode.Created)
}

suspend fun ApplicationCall.respondWithNotFound(
    message: String = "Resource not found"
) {
    respondWithError(message, HttpStatusCode.NotFound)
}

suspend fun ApplicationCall.respondWithUnauthorized(
    message: String = "Unauthorized access"
) {
    respondWithError(message, HttpStatusCode.Unauthorized)
}

suspend fun ApplicationCall.respondWithForbidden(
    message: String = "Access forbidden"
) {
    respondWithError(message, HttpStatusCode.Forbidden)
}

suspend fun ApplicationCall.respondWithConflict(
    message: String = "Resource conflict"
) {
    respondWithError(message, HttpStatusCode.Conflict)
}

suspend fun ApplicationCall.respondWithValidationError(
    message: String = "Validation failed"
) {
    respondWithError(message, HttpStatusCode.BadRequest)
}

/**
 * Responds with data using appropriate response pattern based on data type.
 * 
 * This function handles two different response patterns:
 * 1. BasicApiResponse objects: Forwards the response as-is with proper HTTP status mapping
 *    - Uses HttpStatusMapper to determine correct status code based on success/failure
 *    - Preserves the original BasicApiResponse structure
 *    - Used by service layers that return structured responses
 * 
 * 2. Regular data objects: Wraps data in BasicApiResponse with success=true
 *    - Creates new BasicApiResponse with provided message and status code
 *    - Used for simple data responses that need standard API format
 * 
 * @param data The response data (BasicApiResponse or any other object)
 * @param message Optional message for regular data objects (ignored for BasicApiResponse)
 * @param statusCode Default status code for regular data objects (ignored for BasicApiResponse)
 */
suspend fun ApplicationCall.respondWithMapping(
    data: Any,
    message: String? = null,
    statusCode: HttpStatusCode = HttpStatusCode.OK
) {
    when (data) {
        is BasicApiResponse<*> -> {
            val finalStatusCode = HttpStatusMapper.mapToHttpStatus(data, statusCode)
            respond(finalStatusCode, data)
        }
        else -> {
            respondWithData(data, message, statusCode)
        }
    }
}
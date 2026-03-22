package com.evelolvetech.util

import com.evelolvetech.data.responses.BasicApiResponse
import io.ktor.http.*

object HttpStatusMapper {

    fun <T> mapErrorToHttpStatus(result: BasicApiResponse<T>): HttpStatusCode {
        return when {
            result.success -> HttpStatusCode.OK
            result.message?.contains("Access denied", ignoreCase = true) == true -> HttpStatusCode.Forbidden
            result.message?.contains("not found", ignoreCase = true) == true -> HttpStatusCode.NotFound
            result.message?.contains("not authorized", ignoreCase = true) == true -> HttpStatusCode.Forbidden
            result.message?.contains("unauthorized", ignoreCase = true) == true -> HttpStatusCode.Forbidden
            result.message?.contains("already exists", ignoreCase = true) == true -> HttpStatusCode.Conflict
            result.message?.contains("invalid", ignoreCase = true) == true -> HttpStatusCode.BadRequest
            else -> HttpStatusCode.BadRequest
        }
    }
    
    fun <T> mapToHttpStatus(result: BasicApiResponse<T>, successStatusCode: HttpStatusCode = HttpStatusCode.OK): HttpStatusCode {
        return if (result.success) successStatusCode else mapErrorToHttpStatus(result)
    }
}

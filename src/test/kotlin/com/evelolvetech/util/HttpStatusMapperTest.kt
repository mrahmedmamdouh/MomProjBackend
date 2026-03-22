package com.evelolvetech.util

import com.evelolvetech.data.responses.BasicApiResponse
import io.ktor.http.*
import kotlin.test.*

class HttpStatusMapperTest {

    @Test
    fun `mapErrorToHttpStatus should return OK for successful response`() {
        val response = BasicApiResponse(
            success = true,
            data = "test data",
            message = "Success"
        )

        val status = HttpStatusMapper.mapErrorToHttpStatus(response)

        assertEquals(HttpStatusCode.OK, status)
    }

    @Test
    fun `mapErrorToHttpStatus should return Forbidden for access denied`() {
        val response = BasicApiResponse<Unit>(
            success = false,
            message = "Access denied"
        )

        val status = HttpStatusMapper.mapErrorToHttpStatus(response)

        assertEquals(HttpStatusCode.Forbidden, status)
    }

    @Test
    fun `mapErrorToHttpStatus should return Forbidden for not authorized`() {
        val response = BasicApiResponse<Unit>(
            success = false,
            message = "User not authorized"
        )

        val status = HttpStatusMapper.mapErrorToHttpStatus(response)

        assertEquals(HttpStatusCode.Forbidden, status)
    }

    @Test
    fun `mapErrorToHttpStatus should return Forbidden for unauthorized`() {
        val response = BasicApiResponse<Unit>(
            success = false,
            message = "Unauthorized access"
        )

        val status = HttpStatusMapper.mapErrorToHttpStatus(response)

        assertEquals(HttpStatusCode.Forbidden, status)
    }

    @Test
    fun `mapErrorToHttpStatus should return NotFound for not found messages`() {
        val response = BasicApiResponse<Unit>(
            success = false,
            message = "User not found"
        )

        val status = HttpStatusMapper.mapErrorToHttpStatus(response)

        assertEquals(HttpStatusCode.NotFound, status)
    }

    @Test
    fun `mapErrorToHttpStatus should return Conflict for already exists messages`() {
        val response = BasicApiResponse<Unit>(
            success = false,
            message = "User already exists"
        )

        val status = HttpStatusMapper.mapErrorToHttpStatus(response)

        assertEquals(HttpStatusCode.Conflict, status)
    }

    @Test
    fun `mapErrorToHttpStatus should return BadRequest for invalid messages`() {
        val response = BasicApiResponse<Unit>(
            success = false,
            message = "Invalid email format"
        )

        val status = HttpStatusMapper.mapErrorToHttpStatus(response)

        assertEquals(HttpStatusCode.BadRequest, status)
    }

    @Test
    fun `mapErrorToHttpStatus should return BadRequest for unknown error messages`() {
        val response = BasicApiResponse<Unit>(
            success = false,
            message = "Unknown error occurred"
        )

        val status = HttpStatusMapper.mapErrorToHttpStatus(response)

        assertEquals(HttpStatusCode.BadRequest, status)
    }

    @Test
    fun `mapErrorToHttpStatus should be case insensitive`() {
        val response1 = BasicApiResponse<Unit>(
            success = false,
            message = "ACCESS DENIED"
        )
        val response2 = BasicApiResponse<Unit>(
            success = false,
            message = "access denied"
        )
        val response3 = BasicApiResponse<Unit>(
            success = false,
            message = "Access Denied"
        )

        assertEquals(HttpStatusCode.Forbidden, HttpStatusMapper.mapErrorToHttpStatus(response1))
        assertEquals(HttpStatusCode.Forbidden, HttpStatusMapper.mapErrorToHttpStatus(response2))
        assertEquals(HttpStatusCode.Forbidden, HttpStatusMapper.mapErrorToHttpStatus(response3))
    }

    @Test
    fun `mapToHttpStatus should return success status for successful response`() {
        val response = BasicApiResponse(
            success = true,
            data = "test data",
            message = "Success"
        )

        val status = HttpStatusMapper.mapToHttpStatus(response, HttpStatusCode.Created)

        assertEquals(HttpStatusCode.Created, status)
    }

    @Test
    fun `mapToHttpStatus should return error status for failed response`() {
        val response = BasicApiResponse<Unit>(
            success = false,
            message = "User not found"
        )

        val status = HttpStatusMapper.mapToHttpStatus(response, HttpStatusCode.Created)

        assertEquals(HttpStatusCode.NotFound, status)
    }

    @Test
    fun `mapToHttpStatus should default to OK for successful response`() {
        val response = BasicApiResponse(
            success = true,
            data = "test data",
            message = "Success"
        )

        val status = HttpStatusMapper.mapToHttpStatus(response)

        assertEquals(HttpStatusCode.OK, status)
    }

    @Test
    fun `HttpStatusMapper should be accessible as object`() {
        assertNotNull(HttpStatusMapper)
    }
}

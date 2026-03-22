package com.evelolvetech.util

import kotlin.test.*

class ConstantsTest {

    @Test
    fun `Constants should have correct database configuration`() {
        assertEquals("mom_project", Constants.DATABASE_NAME)
        assertEquals("http://localhost:8080/", Constants.BASE_URL)
        assertEquals("build/resources/main/static/profile_pictures", Constants.PROFILE_PICTURE_PATH)
    }

    @Test
    fun `Constants should have correct validation messages`() {
        assertEquals("The fields may not be empty.", Constants.FIELDS_BLANK)
        assertEquals("Invalid email format.", Constants.INVALID_EMAIL)
        assertEquals("Password must be at least 6 characters long.", Constants.PASSWORD_TOO_SHORT)
    }

    @Test
    fun `Constants should have correct authentication messages`() {
        assertEquals("Invalid credentials, please try again.", Constants.INVALID_CREDENTIALS)
        assertEquals("Unauthorized access.", Constants.UNAUTHORIZED)
        assertEquals("Unauthorized access.", Constants.UNAUTHORIZED_ACCESS)
    }

    @Test
    fun `Constants should have correct user management messages`() {
        assertEquals("A user with this email already exists.", Constants.USER_ALREADY_EXISTS)
        assertEquals("A doctor with this email already exists.", Constants.DOCTOR_ALREADY_EXISTS)
        assertEquals("User not found.", Constants.USER_NOT_FOUND)
    }

    @Test
    fun `Constants should have correct error messages`() {
        assertEquals("An unknown error occurred.", Constants.UNKNOWN_ERROR)
        assertEquals("Resource not found.", Constants.NOT_FOUND)
        assertEquals("Internal server error.", Constants.INTERNAL_SERVER_ERROR)
        assertEquals("Invalid request data.", Constants.INVALID_REQUEST_DATA)
    }

    @Test
    fun `Constants should have correct success messages`() {
        assertEquals("Operation completed successfully.", Constants.SUCCESS)
    }

    @Test
    fun `Constants should have correct e-commerce messages`() {
        assertEquals("Insufficient sessions to purchase this product.", Constants.INSUFFICIENT_SESSIONS)
        assertEquals("Product not found.", Constants.PRODUCT_NOT_FOUND)
        assertEquals("Cart item not found.", Constants.CART_ITEM_NOT_FOUND)
        assertEquals("Order not found.", Constants.ORDER_NOT_FOUND)
        assertEquals("Payment processing failed.", Constants.PAYMENT_FAILED)
        assertEquals("Insufficient inventory.", Constants.INSUFFICIENT_INVENTORY)
    }

    @Test
    fun `Constants should be accessible as object`() {
        assertNotNull(Constants)
    }

    @Test
    fun `Constants should have non-empty values`() {
        assertTrue(Constants.DATABASE_NAME.isNotEmpty())
        assertTrue(Constants.BASE_URL.isNotEmpty())
        assertTrue(Constants.PROFILE_PICTURE_PATH.isNotEmpty())
        assertTrue(Constants.FIELDS_BLANK.isNotEmpty())
        assertTrue(Constants.INVALID_CREDENTIALS.isNotEmpty())
        assertTrue(Constants.USER_ALREADY_EXISTS.isNotEmpty())
        assertTrue(Constants.UNKNOWN_ERROR.isNotEmpty())
        assertTrue(Constants.INVALID_EMAIL.isNotEmpty())
        assertTrue(Constants.PASSWORD_TOO_SHORT.isNotEmpty())
        assertTrue(Constants.UNAUTHORIZED.isNotEmpty())
        assertTrue(Constants.UNAUTHORIZED_ACCESS.isNotEmpty())
        assertTrue(Constants.NOT_FOUND.isNotEmpty())
        assertTrue(Constants.USER_NOT_FOUND.isNotEmpty())
        assertTrue(Constants.INTERNAL_SERVER_ERROR.isNotEmpty())
        assertTrue(Constants.SUCCESS.isNotEmpty())
        assertTrue(Constants.DOCTOR_ALREADY_EXISTS.isNotEmpty())
        assertTrue(Constants.INSUFFICIENT_SESSIONS.isNotEmpty())
        assertTrue(Constants.PRODUCT_NOT_FOUND.isNotEmpty())
        assertTrue(Constants.CART_ITEM_NOT_FOUND.isNotEmpty())
        assertTrue(Constants.ORDER_NOT_FOUND.isNotEmpty())
        assertTrue(Constants.PAYMENT_FAILED.isNotEmpty())
        assertTrue(Constants.INSUFFICIENT_INVENTORY.isNotEmpty())
        assertTrue(Constants.INVALID_REQUEST_DATA.isNotEmpty())
    }
}

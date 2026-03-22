package com.evelolvetech.data.repository

import com.evelolvetech.data.models.Category
import com.evelolvetech.data.models.Mom
import com.evelolvetech.data.models.MomAuth
import com.evelolvetech.data.models.User
import com.evelolvetech.data.models.UserType
import kotlin.test.*

class RepositoryTest {

    @Test
    fun `User model should have correct properties`() {
        val user = User(
            id = "user123",
            email = "test@example.com",
            password = "hashedPassword",
            userType = UserType.MOM,
            createdAt = System.currentTimeMillis()
        )

        assertEquals("user123", user.id)
        assertEquals("test@example.com", user.email)
        assertEquals("hashedPassword", user.password)
        assertEquals(UserType.MOM, user.userType)
        assertTrue(user.createdAt > 0)
    }

    @Test
    fun `Mom model should have correct properties`() {
        val mom = Mom(
            id = "mom123",
            authUid = "auth123",
            fullName = "Jane Doe",
            email = "mom@example.com",
            phone = "+1234567890",
            maritalStatus = "MARRIED",
            photoUrl = "http://example.com/photo.jpg",
            numberOfSessions = 5,
            isAuthorized = true,
            nidId = "nid123",
            nidRef = "nidref123",
            createdAt = System.currentTimeMillis()
        )

        assertEquals("mom123", mom.id)
        assertEquals("auth123", mom.authUid)
        assertEquals("Jane Doe", mom.fullName)
        assertEquals("mom@example.com", mom.email)
        assertEquals("+1234567890", mom.phone)
        assertEquals("MARRIED", mom.maritalStatus)
        assertEquals("http://example.com/photo.jpg", mom.photoUrl)
        assertEquals(5, mom.numberOfSessions)
        assertTrue(mom.isAuthorized)
        assertEquals("nid123", mom.nidId)
        assertEquals("nidref123", mom.nidRef)
        assertTrue(mom.createdAt > 0)
    }

    @Test
    fun `Category model should have correct properties`() {
        val category = Category(
            id = "cat123",
            name = "Electronics",
            slug = "electronics",
            createdAt = System.currentTimeMillis()
        )

        assertEquals("cat123", category.id)
        assertEquals("Electronics", category.name)
        assertEquals("electronics", category.slug)
        assertTrue(category.createdAt > 0)
    }

    @Test
    fun `MomAuth model should have correct properties`() {
        val momAuth = MomAuth(
            id = "auth123",
            uid = "uid123",
            momId = "mom123",
            createdAt = System.currentTimeMillis()
        )

        assertEquals("auth123", momAuth.id)
        assertEquals("uid123", momAuth.uid)
        assertEquals("mom123", momAuth.momId)
        assertTrue(momAuth.createdAt > 0)
    }

    @Test
    fun `UserType enum should have correct values`() {
        assertEquals("MOM", UserType.MOM.name)
        assertEquals("DOCTOR", UserType.DOCTOR.name)
        assertEquals("ADMIN", UserType.ADMIN.name)
    }

    @Test
    fun `Repository classes should be accessible`() {
        assertNotNull(com.evelolvetech.data.repository.api.auth.UserRepository::class)
        assertNotNull(com.evelolvetech.data.repository.api.mom.MomRepository::class)
        assertNotNull(com.evelolvetech.data.repository.api.mom.ecommerce.CategoryRepository::class)
    }
}

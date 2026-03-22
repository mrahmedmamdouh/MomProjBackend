package com.evelolvetech.mocks

import com.evelolvetech.data.models.User
import com.evelolvetech.data.models.UserType
import com.evelolvetech.data.repository.api.auth.UserRepository
import com.mongodb.client.ClientSession

class MockUserRepository : UserRepository {
    val createdUsers = mutableListOf<User>()

    override suspend fun createUserEntry(user: User): Boolean {
        createdUsers.add(user)
        return true
    }

    override suspend fun createUserEntry(user: User, session: ClientSession): Boolean {
        createdUsers.add(user)
        return true
    }

    override suspend fun getUserByEmail(email: String): User? {
        return when (email) {
            "test@example.com" -> User(
                id = "test-user-id",
                email = email,
                password = "hashed-password",
                userType = UserType.MOM
            )
            "existing@example.com" -> User(
                id = "existing-user-id", 
                email = email,
                password = "hashed-password",
                userType = UserType.MOM
            )
            else -> null
        }
    }
    
    override suspend fun doesEmailExist(email: String): Boolean {
        return email == "existing@example.com"
    }
    override suspend fun deleteUser(id: String): Boolean = true
    override suspend fun deleteUser(id: String, session: ClientSession): Boolean = true
}

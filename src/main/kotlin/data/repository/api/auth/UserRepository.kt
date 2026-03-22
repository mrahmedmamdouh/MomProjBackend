package com.evelolvetech.data.repository.api.auth

import com.evelolvetech.data.models.User
import com.mongodb.client.ClientSession

interface UserRepository {
    suspend fun createUserEntry(user: User): Boolean
    suspend fun createUserEntry(user: User, session: ClientSession): Boolean
    suspend fun getUserByEmail(email: String): User?
    suspend fun doesEmailExist(email: String): Boolean
    suspend fun deleteUser(id: String): Boolean
    suspend fun deleteUser(id: String, session: ClientSession): Boolean
}

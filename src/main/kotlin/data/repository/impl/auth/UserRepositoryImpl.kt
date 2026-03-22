package com.evelolvetech.data.repository.impl.auth

import com.evelolvetech.data.models.User
import com.evelolvetech.data.repository.api.auth.UserRepository
import com.mongodb.client.ClientSession
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class UserRepositoryImpl(
    db: MongoDatabase
) : UserRepository {

    private val users: MongoCollection<User> = db.getCollection<User>()

    override suspend fun createUserEntry(user: User): Boolean {
        return try {
            users.insertOne(user)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun createUserEntry(user: User, session: ClientSession): Boolean {
        return try {
            users.insertOne(session, user)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return users.findOne(User::email eq email)
    }

    override suspend fun doesEmailExist(email: String): Boolean {
        return users.findOne(User::email eq email) != null
    }

    override suspend fun deleteUser(id: String): Boolean {
        return try {
            val result = users.deleteOne(User::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteUser(id: String, session: ClientSession): Boolean {
        return try {
            val result = users.deleteOne(session, User::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }
}

package com.evelolvetech.data.repository.impl.circle

import com.evelolvetech.data.models.CircleMember
import com.evelolvetech.data.models.CircleStatus
import com.evelolvetech.data.models.SupportCircle
import com.evelolvetech.data.repository.api.circle.SupportCircleRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.*

class SupportCircleRepositoryImpl(
    db: MongoDatabase
) : SupportCircleRepository {

    private val circles: MongoCollection<SupportCircle> = db.getCollection<SupportCircle>("support_circles")

    override suspend fun createCircle(circle: SupportCircle): Boolean {
        return try {
            circles.insertOne(circle)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getCircleById(id: String): SupportCircle? {
        return circles.findOne(SupportCircle::id eq id)
    }

    override suspend fun getCirclesByStatus(status: String, page: Int, size: Int): List<SupportCircle> {
        return circles.find(SupportCircle::status eq status)
            .sort(descending(SupportCircle::createdAt))
            .skip(page * size).limit(size).toList()
    }

    override suspend fun getCirclesByClusterId(clusterId: String): List<SupportCircle> {
        return circles.find(SupportCircle::clusterId eq clusterId).toList()
    }

    override suspend fun getCirclesByMomId(momId: String): List<SupportCircle> {
        return circles.find(SupportCircle::members / CircleMember::momId eq momId).toList()
    }

    override suspend fun updateCircle(id: String, circle: SupportCircle): Boolean {
        return try {
            val result = circles.replaceOne(SupportCircle::id eq id, circle)
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateCircleStatus(id: String, status: String): Boolean {
        return try {
            val result = circles.updateOne(
                SupportCircle::id eq id,
                combine(
                    setValue(SupportCircle::status, status),
                    setValue(SupportCircle::updatedAt, System.currentTimeMillis())
                )
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun addMemberToCircle(circleId: String, member: CircleMember): Boolean {
        return try {
            val result = circles.updateOne(
                SupportCircle::id eq circleId,
                combine(
                    push(SupportCircle::members, member),
                    setValue(SupportCircle::updatedAt, System.currentTimeMillis())
                )
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removeMemberFromCircle(circleId: String, momId: String): Boolean {
        return try {
            val result = circles.updateOne(
                SupportCircle::id eq circleId,
                combine(
                    pull(SupportCircle::members, CircleMember::momId eq momId),
                    setValue(SupportCircle::updatedAt, System.currentTimeMillis())
                )
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getActiveCircles(page: Int, size: Int): List<SupportCircle> {
        return circles.find(SupportCircle::status eq CircleStatus.ACTIVE.name)
            .sort(descending(SupportCircle::createdAt))
            .skip(page * size).limit(size).toList()
    }

    override suspend fun searchCircles(query: String, page: Int, size: Int): List<SupportCircle> {
        val regex = Regex(query, RegexOption.IGNORE_CASE)
        return circles.find(
            or(
                SupportCircle::name regex regex,
                SupportCircle::description regex regex
            )
        ).skip(page * size).limit(size).toList()
    }

    override suspend fun deleteCircle(id: String): Boolean {
        return try {
            circles.deleteOne(SupportCircle::id eq id).deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }
}

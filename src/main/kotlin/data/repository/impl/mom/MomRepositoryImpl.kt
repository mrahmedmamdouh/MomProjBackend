package com.evelolvetech.data.repository.impl.mom

import com.evelolvetech.data.models.Mom
import com.evelolvetech.data.models.MomAuth
import com.evelolvetech.data.models.MomCirclePreferences
import com.evelolvetech.data.models.MomLocation
import com.evelolvetech.data.repository.api.mom.MomRepository
import com.mongodb.client.ClientSession
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.*

class MomRepositoryImpl(
    db: MongoDatabase
) : MomRepository {

    private val moms: MongoCollection<Mom> = db.getCollection<Mom>()
    private val momAuth: MongoCollection<MomAuth> = db.getCollection<MomAuth>()

    override suspend fun createMom(mom: Mom): Boolean {
        return try {
            moms.insertOne(mom)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun createMom(mom: Mom, session: ClientSession): Boolean {
        return try {
            moms.insertOne(session, mom)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getMomById(id: String): Mom? {
        return moms.findOne(Mom::id eq id)
    }

    override suspend fun getMomByEmail(email: String): Mom? {
        return moms.findOne(Mom::email eq email)
    }

    override suspend fun getMomByAuthUid(authUid: String): Mom? {
        return moms.findOne(Mom::authUid eq authUid)
    }

    override suspend fun updateMom(
        id: String,
        fullName: String?,
        phone: String?,
        maritalStatus: String?,
        photoUrl: String?
    ): Boolean {
        return try {
            val updates = mutableListOf<Bson>()
            fullName?.let { updates.add(Updates.set(Mom::fullName.name, it)) }
            phone?.let { updates.add(Updates.set(Mom::phone.name, it)) }
            maritalStatus?.let { updates.add(Updates.set(Mom::maritalStatus.name, it)) }
            photoUrl?.let { updates.add(Updates.set(Mom::photoUrl.name, it)) }

            if (updates.isNotEmpty()) {
                val result = moms.updateOne(
                    Mom::id eq id,
                    Updates.combine(updates)
                )
                result.modifiedCount > 0
            } else false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteMom(id: String): Boolean {
        return try {
            val result = moms.deleteOne(Mom::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteMom(id: String, session: ClientSession): Boolean {
        return try {
            val result = moms.deleteOne(session, Mom::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun doesEmailExist(email: String): Boolean {
        return moms.findOne(Mom::email eq email) != null
    }

    override suspend fun createMomAuth(momAuth: MomAuth): Boolean {
        return try {
            this.momAuth.insertOne(momAuth)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun createMomAuth(momAuth: MomAuth, session: ClientSession): Boolean {
        return try {
            this.momAuth.insertOne(session, momAuth)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getMomAuthByUid(uid: String): MomAuth? {
        return momAuth.findOne(MomAuth::uid eq uid)
    }

    override suspend fun deleteMomAuth(uid: String): Boolean {
        return try {
            val result = momAuth.deleteOne(MomAuth::uid eq uid)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateMomSessions(id: String, sessions: Int): Boolean {
        return try {
            val result = moms.updateOne(
                Mom::id eq id,
                Updates.set(Mom::numberOfSessions.name, sessions)
            )
            result.matchedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateMomAuthorization(id: String, isAuthorized: Boolean): Boolean {
        return try {
            val result = moms.updateOne(
                Mom::id eq id,
                Updates.set(Mom::isAuthorized.name, isAuthorized)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateMomPersona(
        id: String,
        age: Int?,
        interests: List<String>?,
        pregnancyStage: String?,
        location: MomLocation?,
        culturalBackground: String?,
        circlePreferences: MomCirclePreferences?
    ): Boolean {
        return try {
            val updates = mutableListOf<Bson>()
            age?.let { updates.add(Updates.set(Mom::age.name, it)) }
            interests?.let { updates.add(Updates.set(Mom::interests.name, it)) }
            pregnancyStage?.let { updates.add(Updates.set(Mom::pregnancyStage.name, it)) }
            location?.let { updates.add(Updates.set(Mom::location.name, it)) }
            culturalBackground?.let { updates.add(Updates.set(Mom::culturalBackground.name, it)) }
            circlePreferences?.let { updates.add(Updates.set(Mom::circlePreferences.name, it)) }

            if (updates.isEmpty()) return false

            val result = moms.updateOne(
                Mom::id eq id,
                Updates.combine(updates)
            )
            result.matchedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateMomCluster(id: String, clusterId: String, personaComplete: Boolean): Boolean {
        return try {
            val result = moms.updateOne(
                Mom::id eq id,
                Updates.combine(
                    Updates.set(Mom::clusterId.name, clusterId),
                    Updates.set(Mom::personaComplete.name, personaComplete)
                )
            )
            result.matchedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getMomsByClusterId(clusterId: String): List<Mom> {
        return moms.find(Mom::clusterId eq clusterId).toList()
    }

    override suspend fun getMomsWithCompletePersona(page: Int, size: Int): List<Mom> {
        return moms.find(Mom::personaComplete eq true)
            .skip(page * size)
            .limit(size)
            .toList()
    }

    override suspend fun getMomsByCity(city: String): List<Mom> {
        return moms.find(Mom::location / MomLocation::city regex Regex(city, RegexOption.IGNORE_CASE))
            .toList()
    }

}

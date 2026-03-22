package com.evelolvetech.mocks

import com.evelolvetech.data.models.Mom
import com.evelolvetech.data.models.MomAuth
import com.evelolvetech.data.models.MomCirclePreferences
import com.evelolvetech.data.models.MomLocation
import com.evelolvetech.data.repository.api.mom.MomRepository
import com.mongodb.client.ClientSession

class MockMomRepository : MomRepository {
    val moms = mutableMapOf<String, Mom>()
    val createdMoms = mutableListOf<Mom>()
    val createdMomAuths = mutableListOf<MomAuth>()
    val updateCalls = mutableListOf<Pair<String, Any?>>()
    val updateSessionsCalls = mutableListOf<Pair<String, Int>>()
    var updateResult = false
    var updateSessionsResult = false

    override suspend fun createMom(mom: Mom): Boolean {
        createdMoms.add(mom)
        moms[mom.id] = mom
        return true
    }

    override suspend fun createMom(mom: Mom, session: ClientSession): Boolean {
        createdMoms.add(mom)
        moms[mom.id] = mom
        return true
    }

    override suspend fun getMomById(id: String): Mom? = moms[id]

    override suspend fun getMomByEmail(email: String): Mom? =
        moms.values.find { it.email == email }

    override suspend fun getMomByAuthUid(authUid: String): Mom? =
        moms.values.find { it.authUid == authUid }

    override suspend fun updateMom(
        id: String,
        fullName: String?,
        phone: String?,
        maritalStatus: String?,
        photoUrl: String?
    ): Boolean {
        updateCalls.add(id to null)
        return updateResult
    }

    override suspend fun deleteMom(id: String): Boolean = true

    override suspend fun deleteMom(id: String, session: ClientSession): Boolean = true

    override suspend fun doesEmailExist(email: String): Boolean =
        moms.values.any { it.email == email }

    override suspend fun createMomAuth(momAuth: MomAuth): Boolean {
        createdMomAuths.add(momAuth)
        return true
    }

    override suspend fun createMomAuth(momAuth: MomAuth, session: ClientSession): Boolean {
        createdMomAuths.add(momAuth)
        return true
    }

    override suspend fun getMomAuthByUid(uid: String): MomAuth? = null

    override suspend fun updateMomSessions(id: String, sessions: Int): Boolean {
        updateSessionsCalls.add(id to sessions)
        moms[id]?.let { mom ->
            val updatedMom = mom.copy(numberOfSessions = sessions)
            moms[id] = updatedMom
        }
        return updateSessionsResult
    }

    override suspend fun deleteMomAuth(uid: String): Boolean = true

    override suspend fun updateMomAuthorization(id: String, isAuthorized: Boolean): Boolean {
        moms[id]?.let { mom ->
            val updatedMom = mom.copy(isAuthorized = isAuthorized)
            moms[id] = updatedMom
        }
        return true
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
        val mom = moms[id] ?: return false
        moms[id] = mom.copy(
            age = age ?: mom.age,
            interests = interests ?: mom.interests,
            pregnancyStage = pregnancyStage ?: mom.pregnancyStage,
            location = location ?: mom.location,
            culturalBackground = culturalBackground ?: mom.culturalBackground,
            circlePreferences = circlePreferences ?: mom.circlePreferences
        )
        return true
    }

    override suspend fun updateMomCluster(id: String, clusterId: String, personaComplete: Boolean): Boolean {
        val mom = moms[id] ?: return false
        moms[id] = mom.copy(clusterId = clusterId, personaComplete = personaComplete)
        return true
    }

    override suspend fun getMomsByClusterId(clusterId: String): List<Mom> {
        return moms.values.filter { it.clusterId == clusterId }
    }

    override suspend fun getMomsWithCompletePersona(page: Int, size: Int): List<Mom> {
        return moms.values.filter { it.personaComplete }
            .drop(page * size).take(size)
    }

    override suspend fun getMomsByCity(city: String): List<Mom> {
        return moms.values.filter { it.location?.city?.contains(city, ignoreCase = true) == true }
    }
}

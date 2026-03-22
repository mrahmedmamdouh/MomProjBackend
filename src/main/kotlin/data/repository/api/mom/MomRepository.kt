package com.evelolvetech.data.repository.api.mom

import com.evelolvetech.data.models.Mom
import com.evelolvetech.data.models.MomAuth
import com.mongodb.client.ClientSession

interface MomRepository {
    suspend fun createMom(mom: Mom): Boolean
    suspend fun createMom(mom: Mom, session: ClientSession): Boolean
    suspend fun getMomById(id: String): Mom?
    suspend fun getMomByEmail(email: String): Mom?
    suspend fun getMomByAuthUid(authUid: String): Mom?
    suspend fun updateMom(
        id: String,
        fullName: String?,
        phone: String?,
        maritalStatus: String?,
        photoUrl: String?
    ): Boolean

    suspend fun updateMomAuthorization(id: String, isAuthorized: Boolean): Boolean
    suspend fun deleteMom(id: String): Boolean
    suspend fun deleteMom(id: String, session: ClientSession): Boolean
    suspend fun doesEmailExist(email: String): Boolean
    suspend fun createMomAuth(momAuth: MomAuth): Boolean
    suspend fun createMomAuth(momAuth: MomAuth, session: ClientSession): Boolean
    suspend fun getMomAuthByUid(uid: String): MomAuth?
    suspend fun deleteMomAuth(uid: String): Boolean
    suspend fun updateMomSessions(id: String, sessions: Int): Boolean
    suspend fun updateMomPersona(
        id: String,
        age: Int?,
        interests: List<String>?,
        pregnancyStage: String?,
        location: com.evelolvetech.data.models.MomLocation?,
        culturalBackground: String?,
        circlePreferences: com.evelolvetech.data.models.MomCirclePreferences?
    ): Boolean
    suspend fun updateMomCluster(id: String, clusterId: String, personaComplete: Boolean): Boolean
    suspend fun getMomsByClusterId(clusterId: String): List<Mom>
    suspend fun getMomsWithCompletePersona(page: Int = 0, size: Int = 100): List<Mom>
    suspend fun getMomsByCity(city: String): List<Mom>
}

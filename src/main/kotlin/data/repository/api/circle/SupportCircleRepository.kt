package com.evelolvetech.data.repository.api.circle

import com.evelolvetech.data.models.SupportCircle

interface SupportCircleRepository {
    suspend fun createCircle(circle: SupportCircle): Boolean
    suspend fun getCircleById(id: String): SupportCircle?
    suspend fun getCirclesByStatus(status: String, page: Int = 0, size: Int = 20): List<SupportCircle>
    suspend fun getCirclesByClusterId(clusterId: String): List<SupportCircle>
    suspend fun getCirclesByMomId(momId: String): List<SupportCircle>
    suspend fun updateCircle(id: String, circle: SupportCircle): Boolean
    suspend fun updateCircleStatus(id: String, status: String): Boolean
    suspend fun addMemberToCircle(circleId: String, member: com.evelolvetech.data.models.CircleMember): Boolean
    suspend fun removeMemberFromCircle(circleId: String, momId: String): Boolean
    suspend fun getActiveCircles(page: Int = 0, size: Int = 20): List<SupportCircle>
    suspend fun searchCircles(query: String, page: Int = 0, size: Int = 20): List<SupportCircle>
    suspend fun deleteCircle(id: String): Boolean
}

package com.evelolvetech.data.repository.api.admin

import com.evelolvetech.data.models.admin.*

interface AuditLogRepository {
    suspend fun log(entry: AuditLog): Boolean
    suspend fun getByAdmin(adminId: String, page: Int = 0, size: Int = 50): List<AuditLog>
    suspend fun getByTarget(targetType: String, targetId: String): List<AuditLog>
    suspend fun getAll(page: Int = 0, size: Int = 50): List<AuditLog>
    suspend fun getByDateRange(from: Long, to: Long, page: Int = 0, size: Int = 50): List<AuditLog>
    suspend fun countAll(): Long
}

interface SystemConfigRepository {
    suspend fun getConfig(): SystemConfig
    suspend fun updateConfig(config: SystemConfig): Boolean
}

interface AdminAlertRepository {
    suspend fun createAlert(alert: AdminAlert): Boolean
    suspend fun getUnread(page: Int = 0, size: Int = 50): List<AdminAlert>
    suspend fun getAll(page: Int = 0, size: Int = 50): List<AdminAlert>
    suspend fun markRead(id: String): Boolean
    suspend fun resolve(id: String, adminId: String): Boolean
    suspend fun countUnread(): Long
    suspend fun deleteOlderThan(timestamp: Long): Long
}

interface EmergencyResourceRepository {
    suspend fun create(resource: EmergencyResource): Boolean
    suspend fun getById(id: String): EmergencyResource?
    suspend fun getActive(country: String = "EG"): List<EmergencyResource>
    suspend fun getAll(): List<EmergencyResource>
    suspend fun update(id: String, resource: EmergencyResource): Boolean
    suspend fun delete(id: String): Boolean
}

interface ContentReportRepository {
    suspend fun create(report: ContentReport): Boolean
    suspend fun getById(id: String): ContentReport?
    suspend fun getPending(page: Int = 0, size: Int = 50): List<ContentReport>
    suspend fun getAll(page: Int = 0, size: Int = 50): List<ContentReport>
    suspend fun updateStatus(id: String, status: String, reviewedBy: String, action: String): Boolean
    suspend fun countPending(): Long
}

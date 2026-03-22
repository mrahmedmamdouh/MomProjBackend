package com.evelolvetech.service.admin

import com.evelolvetech.data.models.admin.*
import com.evelolvetech.data.repository.api.admin.*
import com.evelolvetech.data.repository.api.doctor.DoctorRepository
import com.evelolvetech.data.repository.api.mom.MomRepository
import com.evelolvetech.data.repository.api.session.GroupSessionRepository
import com.evelolvetech.data.repository.api.session.SessionBookingRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.OrderRepository
import com.evelolvetech.data.responses.BasicApiResponse

class AdminPortalService(
    private val auditLogRepo: AuditLogRepository,
    private val configRepo: SystemConfigRepository,
    private val alertRepo: AdminAlertRepository,
    private val emergencyRepo: EmergencyResourceRepository,
    private val contentReportRepo: ContentReportRepository,
    private val doctorRepo: DoctorRepository,
    private val momRepo: MomRepository,
    private val sessionRepo: GroupSessionRepository,
    private val bookingRepo: SessionBookingRepository
) {

    // === AUDIT TRAIL ===
    suspend fun logAction(adminId: String, adminEmail: String, action: String, targetType: String, targetId: String, details: String = "", previousValue: String = "", newValue: String = "") {
        auditLogRepo.log(AuditLog(adminId = adminId, adminEmail = adminEmail, action = action, targetType = targetType, targetId = targetId, details = details, previousValue = previousValue, newValue = newValue))
    }

    suspend fun getAuditLogs(page: Int = 0, size: Int = 50) = BasicApiResponse(success = true, data = auditLogRepo.getAll(page, size))
    suspend fun getAuditLogsByAdmin(adminId: String, page: Int = 0) = BasicApiResponse(success = true, data = auditLogRepo.getByAdmin(adminId, page))
    suspend fun getAuditLogsByTarget(targetType: String, targetId: String) = BasicApiResponse(success = true, data = auditLogRepo.getByTarget(targetType, targetId))

    // === SYSTEM CONFIG ===
    suspend fun getConfig() = BasicApiResponse(success = true, data = configRepo.getConfig())
    suspend fun updateConfig(config: SystemConfig, adminId: String): BasicApiResponse<SystemConfig> {
        val old = configRepo.getConfig()
        configRepo.updateConfig(config.copy(updatedBy = adminId))
        logAction(adminId, "", "UPDATE_CONFIG", "SYSTEM", "system_config", "System config updated", old.toString(), config.toString())
        return BasicApiResponse(success = true, data = configRepo.getConfig(), message = "Configuration updated")
    }

    // === ADMIN ALERTS ===
    suspend fun getAlerts(page: Int = 0, size: Int = 50) = BasicApiResponse(success = true, data = alertRepo.getAll(page, size))
    suspend fun getUnreadAlerts(page: Int = 0) = BasicApiResponse(success = true, data = alertRepo.getUnread(page))
    suspend fun getUnreadAlertCount() = BasicApiResponse(success = true, data = mapOf("count" to alertRepo.countUnread()))
    suspend fun markAlertRead(id: String) = if (alertRepo.markRead(id)) BasicApiResponse<Unit>(success = true, message = "Marked as read") else BasicApiResponse(success = false, message = "Alert not found")
    suspend fun resolveAlert(id: String, adminId: String) = if (alertRepo.resolve(id, adminId)) BasicApiResponse<Unit>(success = true, message = "Alert resolved") else BasicApiResponse(success = false, message = "Alert not found")

    suspend fun raiseAlert(type: String, severity: String, title: String, message: String, targetType: String = "", targetId: String = "") {
        alertRepo.createAlert(AdminAlert(type = type, severity = severity, title = title, message = message, targetType = targetType, targetId = targetId))
    }

    // === DOCTOR VERIFICATION (Tier 1) ===
    suspend fun getPendingDoctors(page: Int = 0, size: Int = 50): BasicApiResponse<List<Map<String, Any?>>> {
        val doctors = doctorRepo.getUnauthorizedDoctors(page, size)
        return BasicApiResponse(success = true, data = doctors.map {
            mapOf("id" to it.id, "name" to it.name, "email" to it.email, "phone" to it.phone, "specialization" to it.specialization, "isAuthorized" to it.isAuthorized, "nidId" to it.nidId, "createdAt" to it.createdAt)
        })
    }

    suspend fun approveDoctor(doctorId: String, adminId: String): BasicApiResponse<Unit> {
        val doctor = doctorRepo.getDoctorById(doctorId) ?: return BasicApiResponse(success = false, message = "Doctor not found")
        doctorRepo.updateDoctorAuthorization(doctorId, true)
        logAction(adminId, "", "APPROVE_DOCTOR", "DOCTOR", doctorId, "Approved doctor: ${doctor.name}", "isAuthorized=false", "isAuthorized=true")
        return BasicApiResponse(success = true, message = "Doctor ${doctor.name} approved")
    }

    suspend fun rejectDoctor(doctorId: String, adminId: String, reason: String): BasicApiResponse<Unit> {
        val doctor = doctorRepo.getDoctorById(doctorId) ?: return BasicApiResponse(success = false, message = "Doctor not found")
        doctorRepo.updateDoctorAuthorization(doctorId, false)
        logAction(adminId, "", "REJECT_DOCTOR", "DOCTOR", doctorId, "Rejected: $reason")
        return BasicApiResponse(success = true, message = "Doctor ${doctor.name} rejected: $reason")
    }

    suspend fun revokeDoctor(doctorId: String, adminId: String, reason: String): BasicApiResponse<Unit> {
        val doctor = doctorRepo.getDoctorById(doctorId) ?: return BasicApiResponse(success = false, message = "Doctor not found")
        doctorRepo.updateDoctorAuthorization(doctorId, false)
        logAction(adminId, "", "REVOKE_DOCTOR", "DOCTOR", doctorId, "Revoked: $reason", "isAuthorized=true", "isAuthorized=false")
        return BasicApiResponse(success = true, message = "Doctor ${doctor.name} authorization revoked")
    }

    // === MOM MANAGEMENT (Tier 1) ===
    suspend fun overrideMomSessions(momId: String, newCount: Int, adminId: String, reason: String): BasicApiResponse<Unit> {
        val mom = momRepo.getMomById(momId) ?: return BasicApiResponse(success = false, message = "Mom not found")
        val oldCount = mom.numberOfSessions
        momRepo.updateMomSessions(momId, newCount)
        val config = configRepo.getConfig()
        if (newCount >= config.sessionThreshold && !mom.isAuthorized) {
            momRepo.updateMomAuthorization(momId, true)
        }
        logAction(adminId, "", "OVERRIDE_MOM_SESSIONS", "MOM", momId, reason, "sessions=$oldCount", "sessions=$newCount")
        return BasicApiResponse(success = true, message = "Session count updated from $oldCount to $newCount")
    }

    suspend fun suspendMom(momId: String, adminId: String, reason: String): BasicApiResponse<Unit> {
        momRepo.updateMomAuthorization(momId, false)
        logAction(adminId, "", "SUSPEND_MOM", "MOM", momId, reason)
        return BasicApiResponse(success = true, message = "Mom account suspended")
    }

    // === SESSION OVERSIGHT (Tier 1) ===
    suspend fun forceCompleteSession(sessionId: String, adminId: String): BasicApiResponse<Unit> {
        val session = sessionRepo.getSessionById(sessionId) ?: return BasicApiResponse(success = false, message = "Session not found")
        sessionRepo.updateSessionStatus(sessionId, "COMPLETED")
        logAction(adminId, "", "FORCE_COMPLETE_SESSION", "SESSION", sessionId, "Admin force-completed session: ${session.title}")
        return BasicApiResponse(success = true, message = "Session force-completed")
    }

    suspend fun forceCancelSession(sessionId: String, adminId: String, reason: String): BasicApiResponse<Unit> {
        val session = sessionRepo.getSessionById(sessionId) ?: return BasicApiResponse(success = false, message = "Session not found")
        sessionRepo.updateSessionStatus(sessionId, "CANCELLED")
        val bookings = bookingRepo.getBookingsBySessionId(sessionId)
        bookings.filter { it.status == "CONFIRMED" }.forEach { bookingRepo.updateBookingStatus(it.id, "CANCELLED") }
        logAction(adminId, "", "FORCE_CANCEL_SESSION", "SESSION", sessionId, "Reason: $reason. ${bookings.size} bookings cancelled.")
        return BasicApiResponse(success = true, message = "Session cancelled with ${bookings.size} bookings")
    }

    suspend fun overrideAttendance(bookingId: String, attended: Boolean, adminId: String, reason: String): BasicApiResponse<Unit> {
        bookingRepo.markAttendance(bookingId, attended, null, null)
        logAction(adminId, "", "OVERRIDE_ATTENDANCE", "BOOKING", bookingId, reason, "", "attended=$attended")
        return BasicApiResponse(success = true, message = "Attendance overridden")
    }

    // === EMERGENCY RESOURCES (Tier 4) ===
    suspend fun getEmergencyResources() = BasicApiResponse(success = true, data = emergencyRepo.getAll())
    suspend fun getActiveEmergencyResources(country: String = "EG") = BasicApiResponse(success = true, data = emergencyRepo.getActive(country))
    suspend fun createEmergencyResource(resource: EmergencyResource, adminId: String): BasicApiResponse<Unit> {
        emergencyRepo.create(resource)
        logAction(adminId, "", "CREATE_EMERGENCY_RESOURCE", "EMERGENCY", resource.id, "Created: ${resource.name}")
        return BasicApiResponse(success = true, message = "Emergency resource created")
    }
    suspend fun updateEmergencyResource(id: String, resource: EmergencyResource, adminId: String): BasicApiResponse<Unit> {
        emergencyRepo.update(id, resource)
        logAction(adminId, "", "UPDATE_EMERGENCY_RESOURCE", "EMERGENCY", id, "Updated: ${resource.name}")
        return BasicApiResponse(success = true, message = "Emergency resource updated")
    }
    suspend fun deleteEmergencyResource(id: String, adminId: String): BasicApiResponse<Unit> {
        emergencyRepo.delete(id)
        logAction(adminId, "", "DELETE_EMERGENCY_RESOURCE", "EMERGENCY", id, "Deleted")
        return BasicApiResponse(success = true, message = "Emergency resource deleted")
    }

    // === CONTENT MODERATION (Tier 2) ===
    suspend fun getPendingReports(page: Int = 0) = BasicApiResponse(success = true, data = contentReportRepo.getPending(page))
    suspend fun getAllReports(page: Int = 0) = BasicApiResponse(success = true, data = contentReportRepo.getAll(page))
    suspend fun resolveReport(id: String, action: String, adminId: String): BasicApiResponse<Unit> {
        contentReportRepo.updateStatus(id, "RESOLVED", adminId, action)
        logAction(adminId, "", "RESOLVE_CONTENT_REPORT", "REPORT", id, "Action: $action")
        return BasicApiResponse(success = true, message = "Report resolved")
    }
    suspend fun dismissReport(id: String, adminId: String): BasicApiResponse<Unit> {
        contentReportRepo.updateStatus(id, "DISMISSED", adminId, "dismissed")
        logAction(adminId, "", "DISMISS_CONTENT_REPORT", "REPORT", id, "Dismissed")
        return BasicApiResponse(success = true, message = "Report dismissed")
    }
    suspend fun getPendingReportCount() = BasicApiResponse(success = true, data = mapOf("count" to contentReportRepo.countPending()))
}

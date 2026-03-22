package com.evelolvetech.data.models.admin

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

// === AUDIT TRAIL ===
@Serializable
data class AuditLog(
    @BsonId
    val id: String = ObjectId().toString(),
    val adminId: String,
    val adminEmail: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val details: String = "",
    val previousValue: String = "",
    val newValue: String = "",
    val ipAddress: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// === SYSTEM CONFIG ===
@Serializable
data class SystemConfig(
    @BsonId
    val id: String = "system_config",
    val sessionThreshold: Int = 8,
    val maxSessionCapacity: Int = 7,
    val minSessionCapacity: Int = 2,
    val accessTokenExpiryMinutes: Long = 30,
    val refreshTokenExpiryDays: Long = 30,
    val streamingTokenExpiryHours: Long = 6,
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "",
    val featuresEnabled: FeaturesEnabled = FeaturesEnabled(),
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = ""
)

@Serializable
data class FeaturesEnabled(
    val ecommerce: Boolean = true,
    val streaming: Boolean = true,
    val personaBuilding: Boolean = true,
    val circleMatching: Boolean = true,
    val sessionBooking: Boolean = true,
    val doctorRegistration: Boolean = true,
    val momRegistration: Boolean = true,
    val productRatings: Boolean = true,
    val sessionFeedback: Boolean = true
)

@Serializable
data class NotificationSettings(
    val sessionReminder24h: Boolean = true,
    val sessionReminder1h: Boolean = true,
    val milestoneAlerts: Boolean = true,
    val newSessionAnnouncements: Boolean = true,
    val inactivityReminder7d: Boolean = true,
    val lowStockAlert: Boolean = true,
    val doctorPendingReviewAlert48h: Boolean = true
)

// === ADMIN NOTIFICATIONS ===
@Serializable
data class AdminAlert(
    @BsonId
    val id: String = ObjectId().toString(),
    val type: String,
    val severity: String = "INFO",
    val title: String,
    val message: String,
    val targetType: String = "",
    val targetId: String = "",
    val isRead: Boolean = false,
    val isResolved: Boolean = false,
    val resolvedBy: String = "",
    val resolvedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AlertType {
    DOCTOR_PENDING_REVIEW,
    SESSION_ZERO_BOOKINGS,
    LOW_STOCK,
    STREAMING_USAGE_HIGH,
    USER_REPORT,
    SYSTEM_ERROR,
    SESSION_CANCELLED,
    MOM_MILESTONE
}

enum class AlertSeverity { INFO, WARNING, CRITICAL }

// === EMERGENCY RESOURCES ===
@Serializable
data class EmergencyResource(
    @BsonId
    val id: String = ObjectId().toString(),
    val name: String,
    val nameAr: String = "",
    val phone: String,
    val description: String = "",
    val descriptionAr: String = "",
    val country: String = "EG",
    val region: String = "",
    val category: String = "MENTAL_HEALTH",
    val isActive: Boolean = true,
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

// === CONTENT MODERATION ===
@Serializable
data class ContentReport(
    @BsonId
    val id: String = ObjectId().toString(),
    val reporterType: String,
    val reporterId: String,
    val reporterName: String,
    val contentType: String,
    val contentId: String,
    val contentPreview: String = "",
    val reason: String,
    val details: String = "",
    val status: String = "PENDING",
    val reviewedBy: String = "",
    val reviewedAt: Long? = null,
    val action: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class ReportStatus { PENDING, REVIEWING, RESOLVED, DISMISSED }
enum class ContentType { SESSION_FEEDBACK, PRODUCT_REVIEW, PROFILE, MESSAGE }

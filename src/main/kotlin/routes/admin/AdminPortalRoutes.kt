package com.evelolvetech.routes.admin

import com.evelolvetech.auth.adminRoute
import com.evelolvetech.auth.getCurrentUserIdSafe
import com.evelolvetech.data.models.admin.*
import com.evelolvetech.data.models.venue.ApprovedVenue
import com.evelolvetech.service.admin.AdminPortalService
import com.evelolvetech.service.admin.VenueService
import com.evelolvetech.service.admin.AdminAnalyticsService
import com.evelolvetech.util.respondWithError
import com.evelolvetech.util.respondWithMapping
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.adminPortalRoutes(
    portalService: AdminPortalService,
    venueService: VenueService,
    analyticsService: AdminAnalyticsService
) {
    // === TIER 1: DOCTOR VERIFICATION ===
    adminRoute("/api/admin/doctors") {
        get("/pending") {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
            call.respondWithMapping(portalService.getPendingDoctors(page))
        }
        post("/{id}/approve") {
            val adminId = call.getCurrentUserIdSafe()
            val doctorId = call.parameters["id"]!!
            call.respondWithMapping(portalService.approveDoctor(doctorId, adminId))
        }
        post("/{id}/reject") {
            val adminId = call.getCurrentUserIdSafe()
            val doctorId = call.parameters["id"]!!
            data class RejectRequest(val reason: String)
            val req = call.receive<RejectRequest>()
            call.respondWithMapping(portalService.rejectDoctor(doctorId, adminId, req.reason))
        }
        post("/{id}/revoke") {
            val adminId = call.getCurrentUserIdSafe()
            val doctorId = call.parameters["id"]!!
            data class RevokeRequest(val reason: String)
            val req = call.receive<RevokeRequest>()
            call.respondWithMapping(portalService.revokeDoctor(doctorId, adminId, req.reason))
        }
    }

    // === TIER 1: MOM MANAGEMENT ===
    adminRoute("/api/admin/moms") {
        post("/{id}/override-sessions") {
            val adminId = call.getCurrentUserIdSafe()
            val momId = call.parameters["id"]!!
            data class OverrideRequest(val sessions: Int, val reason: String)
            val req = call.receive<OverrideRequest>()
            call.respondWithMapping(portalService.overrideMomSessions(momId, req.sessions, adminId, req.reason))
        }
        post("/{id}/suspend") {
            val adminId = call.getCurrentUserIdSafe()
            val momId = call.parameters["id"]!!
            data class SuspendRequest(val reason: String)
            val req = call.receive<SuspendRequest>()
            call.respondWithMapping(portalService.suspendMom(momId, adminId, req.reason))
        }
    }

    // === TIER 1: SESSION OVERSIGHT ===
    adminRoute("/api/admin/sessions") {
        post("/{id}/force-complete") {
            val adminId = call.getCurrentUserIdSafe()
            call.respondWithMapping(portalService.forceCompleteSession(call.parameters["id"]!!, adminId))
        }
        post("/{id}/force-cancel") {
            val adminId = call.getCurrentUserIdSafe()
            data class CancelRequest(val reason: String)
            val req = call.receive<CancelRequest>()
            call.respondWithMapping(portalService.forceCancelSession(call.parameters["id"]!!, adminId, req.reason))
        }
        post("/bookings/{id}/override-attendance") {
            val adminId = call.getCurrentUserIdSafe()
            data class AttendanceRequest(val attended: Boolean, val reason: String)
            val req = call.receive<AttendanceRequest>()
            call.respondWithMapping(portalService.overrideAttendance(call.parameters["id"]!!, req.attended, adminId, req.reason))
        }
    }

    // === TIER 1+2: VENUE MANAGEMENT ===
    adminRoute("/api/admin/venues") {
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
            call.respondWithMapping(venueService.getAllVenues(page))
        }
        get("/active") {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
            call.respondWithMapping(venueService.getActiveVenues(page))
        }
        get("/{id}") {
            call.respondWithMapping(venueService.getVenueById(call.parameters["id"]!!))
        }
        get("/city/{city}") {
            call.respondWithMapping(venueService.getVenuesByCity(call.parameters["city"]!!))
        }
        get("/type/{type}") {
            call.respondWithMapping(venueService.getVenuesByType(call.parameters["type"]!!))
        }
        get("/search") {
            val q = call.request.queryParameters["q"] ?: ""
            call.respondWithMapping(venueService.searchVenues(q))
        }
        get("/stats") {
            call.respondWithMapping(venueService.getVenueStats())
        }
        post {
            val adminId = call.getCurrentUserIdSafe()
            val venue = call.receive<ApprovedVenue>()
            call.respondWithMapping(venueService.createVenue(venue, adminId), statusCode = HttpStatusCode.Created)
        }
        put("/{id}") {
            val adminId = call.getCurrentUserIdSafe()
            val venue = call.receive<ApprovedVenue>()
            call.respondWithMapping(venueService.updateVenue(call.parameters["id"]!!, venue, adminId))
        }
        post("/{id}/activate") {
            val adminId = call.getCurrentUserIdSafe()
            call.respondWithMapping(venueService.activateVenue(call.parameters["id"]!!, adminId))
        }
        post("/{id}/deactivate") {
            val adminId = call.getCurrentUserIdSafe()
            call.respondWithMapping(venueService.deactivateVenue(call.parameters["id"]!!, adminId))
        }
        get("/{id}/reviews") {
            call.respondWithMapping(venueService.getVenueReviews(call.parameters["id"]!!))
        }
    }

    // === PUBLIC: Venue browsing for doctors (not admin-only) ===
    route("/api/venues") {
        get {
            val city = call.request.queryParameters["city"]
            val type = call.request.queryParameters["type"]
            val result = if (city != null && type != null) venueService.getVenuesByCityAndType(city, type)
            else if (city != null) venueService.getVenuesByCity(city)
            else if (type != null) venueService.getVenuesByType(type)
            else venueService.getActiveVenues()
            call.respondWithMapping(result)
        }
        get("/{id}") {
            call.respondWithMapping(venueService.getVenueById(call.parameters["id"]!!))
        }
        get("/search") {
            val q = call.request.queryParameters["q"] ?: ""
            call.respondWithMapping(venueService.searchVenues(q))
        }
    }

    // === TIER 3: ANALYTICS ===
    adminRoute("/api/admin/analytics") {
        get("/overview") {
            call.respondWithMapping(analyticsService.getPlatformOverview())
        }
        get("/funnel") {
            call.respondWithMapping(analyticsService.getMomFunnel())
        }
        get("/doctors") {
            call.respondWithMapping(analyticsService.getDoctorPerformance())
        }
        get("/geographic") {
            call.respondWithMapping(analyticsService.getGeographicDistribution())
        }
        get("/sessions") {
            call.respondWithMapping(analyticsService.getSessionAnalytics())
        }
    }

    // === TIER 3: SYSTEM CONFIG ===
    adminRoute("/api/admin/config") {
        get {
            call.respondWithMapping(portalService.getConfig())
        }
        put {
            val adminId = call.getCurrentUserIdSafe()
            val config = call.receive<SystemConfig>()
            call.respondWithMapping(portalService.updateConfig(config, adminId))
        }
    }

    // === TIER 4: AUDIT TRAIL ===
    adminRoute("/api/admin/audit") {
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
            call.respondWithMapping(portalService.getAuditLogs(page))
        }
        get("/admin/{adminId}") {
            call.respondWithMapping(portalService.getAuditLogsByAdmin(call.parameters["adminId"]!!))
        }
        get("/target/{type}/{id}") {
            call.respondWithMapping(portalService.getAuditLogsByTarget(call.parameters["type"]!!, call.parameters["id"]!!))
        }
    }

    // === TIER 2: ALERTS ===
    adminRoute("/api/admin/alerts") {
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
            call.respondWithMapping(portalService.getAlerts(page))
        }
        get("/unread") {
            call.respondWithMapping(portalService.getUnreadAlerts())
        }
        get("/count") {
            call.respondWithMapping(portalService.getUnreadAlertCount())
        }
        post("/{id}/read") {
            call.respondWithMapping(portalService.markAlertRead(call.parameters["id"]!!))
        }
        post("/{id}/resolve") {
            val adminId = call.getCurrentUserIdSafe()
            call.respondWithMapping(portalService.resolveAlert(call.parameters["id"]!!, adminId))
        }
    }

    // === TIER 2: CONTENT MODERATION ===
    adminRoute("/api/admin/moderation") {
        get("/pending") {
            call.respondWithMapping(portalService.getPendingReports())
        }
        get("/all") {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
            call.respondWithMapping(portalService.getAllReports(page))
        }
        get("/count") {
            call.respondWithMapping(portalService.getPendingReportCount())
        }
        post("/{id}/resolve") {
            val adminId = call.getCurrentUserIdSafe()
            data class ResolveRequest(val action: String)
            val req = call.receive<ResolveRequest>()
            call.respondWithMapping(portalService.resolveReport(call.parameters["id"]!!, req.action, adminId))
        }
        post("/{id}/dismiss") {
            val adminId = call.getCurrentUserIdSafe()
            call.respondWithMapping(portalService.dismissReport(call.parameters["id"]!!, adminId))
        }
    }

    // === TIER 4: EMERGENCY RESOURCES ===
    adminRoute("/api/admin/emergency") {
        get {
            call.respondWithMapping(portalService.getEmergencyResources())
        }
        post {
            val adminId = call.getCurrentUserIdSafe()
            val resource = call.receive<EmergencyResource>()
            call.respondWithMapping(portalService.createEmergencyResource(resource, adminId))
        }
        put("/{id}") {
            val adminId = call.getCurrentUserIdSafe()
            val resource = call.receive<EmergencyResource>()
            call.respondWithMapping(portalService.updateEmergencyResource(call.parameters["id"]!!, resource, adminId))
        }
        delete("/{id}") {
            val adminId = call.getCurrentUserIdSafe()
            call.respondWithMapping(portalService.deleteEmergencyResource(call.parameters["id"]!!, adminId))
        }
    }

    // === PUBLIC: Emergency resources for app users ===
    route("/api/emergency-resources") {
        get {
            val country = call.request.queryParameters["country"] ?: "EG"
            call.respondWithMapping(portalService.getActiveEmergencyResources(country))
        }
    }
}

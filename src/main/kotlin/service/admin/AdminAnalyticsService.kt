package com.evelolvetech.service.admin

import com.evelolvetech.data.repository.api.doctor.DoctorRepository
import com.evelolvetech.data.repository.api.mom.MomRepository
import com.evelolvetech.data.repository.api.session.GroupSessionRepository
import com.evelolvetech.data.repository.api.session.SessionBookingRepository
import com.evelolvetech.data.repository.api.admin.VenueRepository
import com.evelolvetech.data.responses.BasicApiResponse

class AdminAnalyticsService(
    private val momRepo: MomRepository,
    private val doctorRepo: DoctorRepository,
    private val sessionRepo: GroupSessionRepository,
    private val bookingRepo: SessionBookingRepository,
    private val venueRepo: VenueRepository
) {

    suspend fun getPlatformOverview(): BasicApiResponse<Map<String, Any>> {
        val allMoms = momRepo.getMomsWithCompletePersona(0, 10000)
        val allDoctors = doctorRepo.getAllDoctors()
        val completedSessions = sessionRepo.getSessionsByStatus("COMPLETED", 0, 10000)
        val upcomingSessions = sessionRepo.getSessionsByStatus("OPEN_FOR_BOOKING", 0, 10000)
        val activeSessions = sessionRepo.getSessionsByStatus("IN_PROGRESS", 0, 100)

        val totalMoms = allMoms.size
        val authorizedMoms = allMoms.count { it.isAuthorized }
        val momsWithPersona = allMoms.count { it.personaComplete }
        val avgSessionsPerMom = if (totalMoms > 0) allMoms.map { it.numberOfSessions }.average() else 0.0

        return BasicApiResponse(success = true, data = mapOf(
            "moms" to mapOf(
                "total" to totalMoms,
                "authorized" to authorizedMoms,
                "withPersona" to momsWithPersona,
                "inCircles" to allMoms.count { it.clusterId != null },
                "avgSessions" to String.format("%.1f", avgSessionsPerMom)
            ),
            "doctors" to mapOf(
                "total" to allDoctors.size,
                "authorized" to allDoctors.count { it.isAuthorized },
                "pending" to allDoctors.count { !it.isAuthorized },
                "bySpecialization" to allDoctors.groupingBy { it.specialization }.eachCount()
            ),
            "sessions" to mapOf(
                "completed" to completedSessions.size,
                "active" to activeSessions.size,
                "upcoming" to upcomingSessions.size,
                "totalBookings" to completedSessions.sumOf { it.currentBookings },
                "byType" to completedSessions.groupingBy { it.sessionType }.eachCount()
            ),
            "venues" to mapOf(
                "totalActive" to venueRepo.countByStatus("ACTIVE")
            )
        ))
    }

    suspend fun getMomFunnel(): BasicApiResponse<Map<String, Any>> {
        val allMoms = momRepo.getMomsWithCompletePersona(0, 10000)
        val total = allMoms.size
        val withPersona = allMoms.count { it.personaComplete }
        val withFirstSession = allMoms.count { it.numberOfSessions >= 1 }
        val withFourSessions = allMoms.count { it.numberOfSessions >= 4 }
        val withEightSessions = allMoms.count { it.numberOfSessions >= 8 }

        fun pct(n: Int) = if (total > 0) String.format("%.1f", n.toDouble() / total * 100) else "0"

        return BasicApiResponse(success = true, data = mapOf(
            "funnel" to listOf(
                mapOf("stage" to "Registered", "count" to total, "pct" to "100"),
                mapOf("stage" to "Persona Complete", "count" to withPersona, "pct" to pct(withPersona)),
                mapOf("stage" to "First Session", "count" to withFirstSession, "pct" to pct(withFirstSession)),
                mapOf("stage" to "4 Sessions", "count" to withFourSessions, "pct" to pct(withFourSessions)),
                mapOf("stage" to "8 Sessions (Store)", "count" to withEightSessions, "pct" to pct(withEightSessions))
            )
        ))
    }

    suspend fun getDoctorPerformance(): BasicApiResponse<List<Map<String, Any>>> {
        val doctors = doctorRepo.getAuthorizedDoctors()
        val performance = doctors.map { doctor ->
            val sessions = sessionRepo.getSessionsByDoctorId(doctor.id, 0, 1000)
            val completed = sessions.filter { it.status == "COMPLETED" }
            val cancelled = sessions.count { it.status == "CANCELLED" }
            val totalBookings = sessions.sumOf { it.currentBookings }

            mapOf<String, Any>(
                "doctorId" to doctor.id,
                "name" to doctor.name,
                "specialization" to doctor.specialization,
                "totalSessions" to sessions.size,
                "completedSessions" to completed.size,
                "cancelledSessions" to cancelled,
                "totalBookings" to totalBookings,
                "avgBookings" to if (sessions.isNotEmpty()) String.format("%.1f", totalBookings.toDouble() / sessions.size) else "0",
                "completionRate" to if (sessions.isNotEmpty()) String.format("%.0f", completed.size.toDouble() / sessions.size * 100) else "0"
            )
        }.sortedByDescending { it["completedSessions"] as Int }

        return BasicApiResponse(success = true, data = performance)
    }

    suspend fun getGeographicDistribution(): BasicApiResponse<Map<String, Any>> {
        val moms = momRepo.getMomsWithCompletePersona(0, 10000)
        val momsByCity = moms.mapNotNull { it.location?.city }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .associate { it.key to it.value }

        val momsByStage = moms.mapNotNull { it.pregnancyStage }
            .groupingBy { it }.eachCount()

        val momsByCulture = moms.mapNotNull { it.culturalBackground }
            .groupingBy { it }.eachCount()

        return BasicApiResponse(success = true, data = mapOf(
            "momsByCity" to momsByCity,
            "momsByPregnancyStage" to momsByStage,
            "momsByCulture" to momsByCulture,
            "topInterests" to moms.flatMap { it.interests }
                .groupingBy { it }.eachCount()
                .entries.sortedByDescending { it.value }.take(10)
                .associate { it.key to it.value }
        ))
    }

    suspend fun getSessionAnalytics(): BasicApiResponse<Map<String, Any>> {
        val allSessions = sessionRepo.getSessionsByStatus("COMPLETED", 0, 10000)
        val avgCapacity = if (allSessions.isNotEmpty()) allSessions.map { it.currentBookings }.average() else 0.0
        val avgDuration = if (allSessions.isNotEmpty()) allSessions.map { it.durationMinutes }.average() else 0.0

        val byVenueType = allSessions.filter { it.sessionType == "PHYSICAL_MEETING" }
            .mapNotNull { it.venue?.venueType }
            .groupingBy { it }.eachCount()

        val byCity = allSessions.filter { it.sessionType == "PHYSICAL_MEETING" }
            .mapNotNull { it.venue?.city }
            .groupingBy { it }.eachCount()

        return BasicApiResponse(success = true, data = mapOf(
            "totalCompleted" to allSessions.size,
            "avgAttendance" to String.format("%.1f", avgCapacity),
            "avgDurationMinutes" to String.format("%.0f", avgDuration),
            "onlineVsPhysical" to allSessions.groupingBy { it.sessionType }.eachCount(),
            "physicalByVenueType" to byVenueType,
            "physicalByCity" to byCity
        ))
    }
}

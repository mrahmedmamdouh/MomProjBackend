package com.evelolvetech.service.streaming

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.evelolvetech.data.models.SessionStatus
import com.evelolvetech.data.repository.api.session.GroupSessionRepository
import com.evelolvetech.data.repository.api.session.SessionBookingRepository
import com.evelolvetech.data.repository.api.doctor.DoctorRepository
import com.evelolvetech.data.repository.api.mom.MomRepository
import com.evelolvetech.data.responses.BasicApiResponse
import java.util.*

data class RoomToken(
    val token: String,
    val serverUrl: String,
    val roomName: String,
    val participantIdentity: String,
    val participantName: String,
    val isHost: Boolean,
    val expiresAt: Long
)

data class RoomInfo(
    val roomName: String,
    val sessionId: String,
    val sessionTitle: String,
    val hostDoctorId: String,
    val hostDoctorName: String,
    val maxParticipants: Int,
    val isLive: Boolean,
    val serverUrl: String
)

class StreamingService(
    private val config: LiveKitConfig,
    private val sessionRepository: GroupSessionRepository,
    private val bookingRepository: SessionBookingRepository,
    private val doctorRepository: DoctorRepository,
    private val momRepository: MomRepository
) {

    fun getRoomName(sessionId: String): String = "${config.roomPrefix}session_$sessionId"

    suspend fun doctorStartLiveSession(doctorId: String, sessionId: String): BasicApiResponse<RoomToken> {
        val session = sessionRepository.getSessionById(sessionId)
            ?: return BasicApiResponse(success = false, message = "Session not found")

        if (session.doctorId != doctorId) {
            return BasicApiResponse(success = false, message = "Access denied — this is not your session")
        }

        if (session.sessionType != "ONLINE_VIDEO") {
            return BasicApiResponse(success = false, message = "This is a physical meetup, not a live session")
        }

        if (session.status !in listOf(
                SessionStatus.OPEN_FOR_BOOKING.name,
                SessionStatus.FULL.name,
                SessionStatus.SCHEDULED.name,
                SessionStatus.IN_PROGRESS.name
            )) {
            return BasicApiResponse(success = false, message = "Session cannot be started in status: ${session.status}")
        }

        val doctor = doctorRepository.getDoctorById(doctorId)
            ?: return BasicApiResponse(success = false, message = "Doctor not found")

        sessionRepository.updateSessionStatus(sessionId, SessionStatus.IN_PROGRESS.name)

        val roomName = getRoomName(sessionId)
        val token = generateToken(
            identity = "doctor_$doctorId",
            name = doctor.name,
            roomName = roomName,
            canPublish = true,
            canSubscribe = true,
            canPublishData = true,
            isHost = true,
            metadata = """{"role":"doctor","sessionId":"$sessionId","doctorId":"$doctorId"}"""
        )

        return BasicApiResponse(
            success = true,
            data = RoomToken(
                token = token,
                serverUrl = config.serverUrl,
                roomName = roomName,
                participantIdentity = "doctor_$doctorId",
                participantName = doctor.name,
                isHost = true,
                expiresAt = System.currentTimeMillis() + (config.tokenExpirySeconds * 1000)
            )
        )
    }

    suspend fun momJoinLiveSession(momId: String, sessionId: String): BasicApiResponse<RoomToken> {
        val session = sessionRepository.getSessionById(sessionId)
            ?: return BasicApiResponse(success = false, message = "Session not found")

        if (session.sessionType != "ONLINE_VIDEO") {
            return BasicApiResponse(success = false, message = "This is a physical meetup, not a live session")
        }

        val booking = bookingRepository.getBookingBySessionAndMom(sessionId, momId)
            ?: return BasicApiResponse(success = false, message = "You don't have a booking for this session. Please book first.")

        if (booking.status != "CONFIRMED") {
            return BasicApiResponse(success = false, message = "Your booking status is ${booking.status}, not CONFIRMED")
        }

        if (session.status != SessionStatus.IN_PROGRESS.name) {
            return BasicApiResponse(success = false, message = "The session hasn't started yet. The doctor will start it when ready.")
        }

        val mom = momRepository.getMomById(momId)
            ?: return BasicApiResponse(success = false, message = "Mom not found")

        val roomName = getRoomName(sessionId)
        val token = generateToken(
            identity = "mom_$momId",
            name = mom.fullName,
            roomName = roomName,
            canPublish = true,
            canSubscribe = true,
            canPublishData = true,
            isHost = false,
            metadata = """{"role":"mom","sessionId":"$sessionId","momId":"$momId","bookingId":"${booking.id}"}"""
        )

        return BasicApiResponse(
            success = true,
            data = RoomToken(
                token = token,
                serverUrl = config.serverUrl,
                roomName = roomName,
                participantIdentity = "mom_$momId",
                participantName = mom.fullName,
                isHost = false,
                expiresAt = System.currentTimeMillis() + (config.tokenExpirySeconds * 1000)
            )
        )
    }

    suspend fun doctorEndLiveSession(doctorId: String, sessionId: String): BasicApiResponse<Unit> {
        val session = sessionRepository.getSessionById(sessionId)
            ?: return BasicApiResponse(success = false, message = "Session not found")

        if (session.doctorId != doctorId) {
            return BasicApiResponse(success = false, message = "Access denied")
        }

        return BasicApiResponse(success = true, message = "Live session ended. You can now mark attendance and complete the session.")
    }

    suspend fun getSessionRoomInfo(sessionId: String): BasicApiResponse<RoomInfo> {
        val session = sessionRepository.getSessionById(sessionId)
            ?: return BasicApiResponse(success = false, message = "Session not found")

        if (session.sessionType != "ONLINE_VIDEO") {
            return BasicApiResponse(success = false, message = "Not a live session")
        }

        return BasicApiResponse(
            success = true,
            data = RoomInfo(
                roomName = getRoomName(sessionId),
                sessionId = sessionId,
                sessionTitle = session.title,
                hostDoctorId = session.doctorId,
                hostDoctorName = session.doctorName,
                maxParticipants = session.maxCapacity + 1,
                isLive = session.status == SessionStatus.IN_PROGRESS.name,
                serverUrl = config.serverUrl
            )
        )
    }

    private fun generateToken(
        identity: String,
        name: String,
        roomName: String,
        canPublish: Boolean,
        canSubscribe: Boolean,
        canPublishData: Boolean,
        isHost: Boolean,
        metadata: String = ""
    ): String {
        val now = Date()
        val expiry = Date(now.time + (config.tokenExpirySeconds * 1000))

        val videoGrant = mapOf(
            "roomJoin" to true,
            "room" to roomName,
            "canPublish" to canPublish,
            "canSubscribe" to canSubscribe,
            "canPublishData" to canPublishData,
            "canUpdateOwnMetadata" to false,
            "hidden" to false
        )

        return JWT.create()
            .withIssuer(config.apiKey)
            .withSubject(identity)
            .withIssuedAt(now)
            .withNotBefore(now)
            .withExpiresAt(expiry)
            .withJWTId(UUID.randomUUID().toString())
            .withClaim("name", name)
            .withClaim("video", videoGrant)
            .withClaim("metadata", metadata)
            .sign(Algorithm.HMAC256(config.apiSecret))
    }
}

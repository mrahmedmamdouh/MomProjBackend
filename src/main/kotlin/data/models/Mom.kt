package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Mom(
    @BsonId
    val id: String = ObjectId().toString(),
    val authUid: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val maritalStatus: String,
    val photoUrl: String,
    val numberOfSessions: Int = 0,
    val isAuthorized: Boolean = false,
    val nidId: String,
    val nidRef: String,
    val age: Int? = null,
    val dateOfBirth: Long? = null,
    val interests: List<String> = emptyList(),
    val pregnancyStage: String? = null,
    val location: MomLocation? = null,
    val culturalBackground: String? = null,
    val circlePreferences: MomCirclePreferences? = null,
    val personaComplete: Boolean = false,
    val clusterId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class MomLocation(
    val city: String,
    val state: String = "",
    val country: String = "EG",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

@Serializable
data class MomCirclePreferences(
    val preferredGroupSize: Int = 6,
    val preferredMeetingFrequency: String = "WEEKLY",
    val preferredMeetingTimes: List<String> = listOf("WEEKEND_MORNING"),
    val preferredSessionType: String = "ONLINE_VIDEO",
    val languagePreference: String = "ar"
)

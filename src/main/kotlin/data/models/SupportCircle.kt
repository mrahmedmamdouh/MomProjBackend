package com.evelolvetech.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

enum class CircleStatus {
    FORMING,
    ACTIVE,
    INACTIVE,
    DISBANDED
}

@Serializable
data class SupportCircle(
    @BsonId
    val id: String = ObjectId().toString(),
    val name: String,
    val description: String,
    val members: List<CircleMember> = emptyList(),
    val maxSize: Int = 7,
    val clusterId: String? = null,
    val assignedDoctorId: String? = null,
    val assignedDoctorRef: String? = null,
    val commonInterests: List<String> = emptyList(),
    val commonPregnancyStages: List<String> = emptyList(),
    val language: String = "ar",
    val location: MomLocation? = null,
    val status: String = CircleStatus.FORMING.name,
    val qualityMetrics: CircleQualityMetrics = CircleQualityMetrics(),
    val formationAlgorithm: String = "K_MEANS",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class CircleMember(
    val momId: String,
    val momRef: String,
    val name: String,
    val joinedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val compatibilityScore: Double = 0.0
)

@Serializable
data class CircleQualityMetrics(
    val averageCompatibility: Double = 0.0,
    val geographicCohesion: Double = 0.0,
    val interestOverlap: Double = 0.0,
    val stageAlignment: Double = 0.0,
    val overallScore: Double = 0.0
)

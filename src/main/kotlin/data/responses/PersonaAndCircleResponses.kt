package com.evelolvetech.data.responses

import com.evelolvetech.data.models.CircleQualityMetrics
import com.evelolvetech.data.models.SupportCircle
import kotlinx.serialization.Serializable

@Serializable
data class PersonaStatusResponse(
    val momId: String,
    val fields: List<PersonaFieldStatus>,
    val completedFields: Int,
    val totalFields: Int,
    val completionPercentage: Int,
    val isComplete: Boolean,
    val nextQuestion: String?,
    val clusterId: String?
)

@Serializable
data class PersonaFieldStatus(
    val field: String,
    val filled: Boolean,
    val currentValue: String? = null
)

@Serializable
data class CircleResponse(
    val id: String,
    val name: String,
    val description: String,
    val memberCount: Int,
    val maxSize: Int,
    val commonInterests: List<String>,
    val commonPregnancyStages: List<String>,
    val city: String?,
    val status: String,
    val qualityMetrics: CircleQualityMetricsResponse,
    val createdAt: Long
) {
    companion object {
        fun fromCircle(circle: SupportCircle) = CircleResponse(
            id = circle.id,
            name = circle.name,
            description = circle.description,
            memberCount = circle.members.size,
            maxSize = circle.maxSize,
            commonInterests = circle.commonInterests,
            commonPregnancyStages = circle.commonPregnancyStages,
            city = circle.location?.city,
            status = circle.status,
            qualityMetrics = CircleQualityMetricsResponse(
                averageCompatibility = circle.qualityMetrics.averageCompatibility,
                geographicCohesion = circle.qualityMetrics.geographicCohesion,
                interestOverlap = circle.qualityMetrics.interestOverlap,
                stageAlignment = circle.qualityMetrics.stageAlignment,
                overallScore = circle.qualityMetrics.overallScore
            ),
            createdAt = circle.createdAt
        )
    }
}

@Serializable
data class CircleQualityMetricsResponse(
    val averageCompatibility: Double,
    val geographicCohesion: Double,
    val interestOverlap: Double,
    val stageAlignment: Double,
    val overallScore: Double
)

@Serializable
data class CircleRecommendation(
    val circle: CircleResponse,
    val compatibilityScore: Double,
    val matchReasons: List<String>
)

@Serializable
data class FormationSummaryResponse(
    val totalMothers: Int,
    val assignedMothers: Int,
    val unassignedMothers: Int,
    val circlesFormed: Int,
    val averageCircleSize: Double,
    val averageQualityScore: Double,
    val clusters: List<ClusterSummary>
)

@Serializable
data class ClusterSummary(
    val clusterId: String,
    val memberCount: Int,
    val description: String
)

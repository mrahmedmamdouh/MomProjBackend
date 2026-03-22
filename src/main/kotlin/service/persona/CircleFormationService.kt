package com.evelolvetech.service.persona

import com.evelolvetech.data.models.*
import com.evelolvetech.data.repository.api.circle.SupportCircleRepository
import com.evelolvetech.data.repository.api.mom.MomRepository
import com.evelolvetech.data.responses.*

class CircleFormationService(
    private val circleRepository: SupportCircleRepository,
    private val momRepository: MomRepository,
    private val clusteringService: ClusteringService
) {

    suspend fun formCirclesFromCluster(clusterId: String, maxCircleSize: Int = 7): BasicApiResponse<List<CircleResponse>> {
        return try {
            val moms = momRepository.getMomsByClusterId(clusterId)
            if (moms.isEmpty()) {
                return BasicApiResponse(success = false, message = "No moms found in cluster: $clusterId")
            }

            val circles = mutableListOf<SupportCircle>()
            val chunks = moms.chunked(maxCircleSize)

            for ((index, chunk) in chunks.withIndex()) {
                if (chunk.size < 2) continue

                val commonInterests = chunk.flatMap { it.interests }
                    .groupingBy { it }.eachCount()
                    .entries.sortedByDescending { it.value }
                    .take(5).map { it.key }

                val commonStages = chunk.mapNotNull { it.pregnancyStage }.distinct()
                val topCity = chunk.mapNotNull { it.location?.city }
                    .groupingBy { it }.eachCount()
                    .maxByOrNull { it.value }?.key

                val members = chunk.map { mom ->
                    val compatibilityScores = chunk.filter { it.id != mom.id }
                        .map { other -> clusteringService.computeCompatibilityScore(mom, other) }
                    val avgCompatibility = if (compatibilityScores.isNotEmpty()) compatibilityScores.average() else 0.0

                    CircleMember(
                        momId = mom.id,
                        momRef = "/moms/${mom.id}",
                        name = mom.fullName,
                        compatibilityScore = avgCompatibility
                    )
                }

                val qualityMetrics = computeQualityMetrics(chunk)
                val name = generateCircleName(commonInterests, commonStages, topCity, index)

                val circle = SupportCircle(
                    name = name,
                    description = "Support circle for ${commonInterests.joinToString(", ")}-focused moms" +
                        (topCity?.let { " near $it" } ?: ""),
                    members = members,
                    maxSize = maxCircleSize,
                    clusterId = clusterId,
                    commonInterests = commonInterests,
                    commonPregnancyStages = commonStages,
                    location = topCity?.let { MomLocation(city = it) },
                    status = CircleStatus.ACTIVE.name,
                    qualityMetrics = qualityMetrics,
                    formationAlgorithm = "K_MEANS"
                )

                val created = circleRepository.createCircle(circle)
                if (created) circles.add(circle)
            }

            BasicApiResponse(
                success = true,
                data = circles.map { CircleResponse.fromCircle(it) },
                message = "Successfully formed ${circles.size} circle(s) from cluster $clusterId"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error forming circles: ${e.message}")
        }
    }

    suspend fun formAllCircles(numClusters: Int = 5, maxCircleSize: Int = 7): BasicApiResponse<FormationSummaryResponse> {
        return try {
            val clusterResults = clusteringService.clusterAllMoms(numClusters)

            val allCircles = mutableListOf<SupportCircle>()
            var totalAssigned = 0
            var totalUnassigned = 0

            for (cluster in clusterResults) {
                val result = formCirclesFromCluster(cluster.clusterId, maxCircleSize)
                if (result.success && result.data != null) {
                    totalAssigned += cluster.memberIds.size
                } else {
                    totalUnassigned += cluster.memberIds.size
                }
            }

            val activeCircles = circleRepository.getActiveCircles(0, 100)

            BasicApiResponse(
                success = true,
                data = FormationSummaryResponse(
                    totalMothers = totalAssigned + totalUnassigned,
                    assignedMothers = totalAssigned,
                    unassignedMothers = totalUnassigned,
                    circlesFormed = activeCircles.size,
                    averageCircleSize = if (activeCircles.isNotEmpty()) activeCircles.map { it.members.size }.average() else 0.0,
                    averageQualityScore = if (activeCircles.isNotEmpty()) activeCircles.map { it.qualityMetrics.overallScore }.average() else 0.0,
                    clusters = clusterResults.map {
                        ClusterSummary(clusterId = it.clusterId, memberCount = it.memberIds.size, description = it.description)
                    }
                ),
                message = "Circle formation complete"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error in circle formation: ${e.message}")
        }
    }

    suspend fun getRecommendedCircles(momId: String, maxRecommendations: Int = 5): BasicApiResponse<List<CircleRecommendation>> {
        return try {
            val mom = momRepository.getMomById(momId)
                ?: return BasicApiResponse(success = false, message = "Mom not found")

            val existingCircles = circleRepository.getCirclesByMomId(momId)
            val existingCircleIds = existingCircles.map { it.id }.toSet()

            val candidateCircles = if (mom.clusterId != null) {
                circleRepository.getCirclesByClusterId(mom.clusterId!!)
            } else {
                circleRepository.getActiveCircles(0, 50)
            }

            val recommendations = candidateCircles
                .filter { it.id !in existingCircleIds && it.status == CircleStatus.ACTIVE.name && it.members.size < it.maxSize }
                .map { circle ->
                    val circleMoms = circle.members.mapNotNull { member -> momRepository.getMomById(member.momId) }
                    val avgCompatibility = circleMoms.map { clusteringService.computeCompatibilityScore(mom, it) }.average()
                    val matchReasons = buildMatchReasons(mom, circle, circleMoms)

                    CircleRecommendation(
                        circle = CircleResponse.fromCircle(circle),
                        compatibilityScore = avgCompatibility,
                        matchReasons = matchReasons
                    )
                }
                .sortedByDescending { it.compatibilityScore }
                .take(maxRecommendations)

            BasicApiResponse(success = true, data = recommendations)
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error getting recommendations: ${e.message}")
        }
    }

    suspend fun getMyCircles(momId: String): BasicApiResponse<List<CircleResponse>> {
        return try {
            val circles = circleRepository.getCirclesByMomId(momId)
            BasicApiResponse(success = true, data = circles.map { CircleResponse.fromCircle(it) })
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving circles: ${e.message}")
        }
    }

    suspend fun joinCircle(momId: String, circleId: String): BasicApiResponse<CircleResponse> {
        return try {
            val mom = momRepository.getMomById(momId)
                ?: return BasicApiResponse(success = false, message = "Mom not found")

            val circle = circleRepository.getCircleById(circleId)
                ?: return BasicApiResponse(success = false, message = "Circle not found")

            if (circle.members.size >= circle.maxSize) {
                return BasicApiResponse(success = false, message = "Circle is full")
            }

            if (circle.members.any { it.momId == momId }) {
                return BasicApiResponse(success = false, message = "You are already a member of this circle")
            }

            val member = CircleMember(
                momId = momId,
                momRef = "/moms/$momId",
                name = mom.fullName,
                compatibilityScore = 0.0
            )

            circleRepository.addMemberToCircle(circleId, member)

            val updated = circleRepository.getCircleById(circleId)
            BasicApiResponse(
                success = true,
                data = updated?.let { CircleResponse.fromCircle(it) },
                message = "Successfully joined circle"
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error joining circle: ${e.message}")
        }
    }

    suspend fun leaveCircle(momId: String, circleId: String): BasicApiResponse<Unit> {
        return try {
            val circle = circleRepository.getCircleById(circleId)
                ?: return BasicApiResponse(success = false, message = "Circle not found")

            if (circle.members.none { it.momId == momId }) {
                return BasicApiResponse(success = false, message = "You are not a member of this circle")
            }

            circleRepository.removeMemberFromCircle(circleId, momId)
            BasicApiResponse(success = true, message = "Successfully left circle")
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error leaving circle: ${e.message}")
        }
    }

    private fun computeQualityMetrics(moms: List<Mom>): CircleQualityMetrics {
        if (moms.size < 2) return CircleQualityMetrics()

        var totalCompatibility = 0.0
        var pairCount = 0
        for (i in moms.indices) {
            for (j in i + 1 until moms.size) {
                totalCompatibility += clusteringService.computeCompatibilityScore(moms[i], moms[j])
                pairCount++
            }
        }
        val avgCompatibility = if (pairCount > 0) totalCompatibility / pairCount else 0.0

        val cities = moms.mapNotNull { it.location?.city }
        val geoCohesion = if (cities.isNotEmpty()) {
            val topCityCount = cities.groupingBy { it }.eachCount().values.maxOrNull() ?: 0
            topCityCount.toDouble() / cities.size * 100
        } else 0.0

        val allInterests = moms.flatMap { it.interests }.toSet()
        val interestOverlap = if (allInterests.isNotEmpty()) {
            val sharedByAll = allInterests.count { interest -> moms.all { it.interests.contains(interest) } }
            sharedByAll.toDouble() / allInterests.size * 100
        } else 0.0

        val stages = moms.mapNotNull { it.pregnancyStage }
        val stageAlignment = if (stages.isNotEmpty()) {
            val topStageCount = stages.groupingBy { it }.eachCount().values.maxOrNull() ?: 0
            topStageCount.toDouble() / stages.size * 100
        } else 0.0

        val overallScore = (avgCompatibility * 0.4 + geoCohesion * 0.2 + interestOverlap * 0.2 + stageAlignment * 0.2)

        return CircleQualityMetrics(
            averageCompatibility = avgCompatibility,
            geographicCohesion = geoCohesion,
            interestOverlap = interestOverlap,
            stageAlignment = stageAlignment,
            overallScore = overallScore
        )
    }

    private fun buildMatchReasons(mom: Mom, circle: SupportCircle, circleMoms: List<Mom>): List<String> {
        val reasons = mutableListOf<String>()

        val sharedInterests = mom.interests.intersect(circle.commonInterests.toSet())
        if (sharedInterests.isNotEmpty()) {
            reasons.add("Shared interests: ${sharedInterests.joinToString(", ")}")
        }

        if (mom.pregnancyStage != null && circle.commonPregnancyStages.contains(mom.pregnancyStage)) {
            reasons.add("Same pregnancy stage: ${mom.pregnancyStage!!.lowercase().replace("_", " ")}")
        }

        if (mom.location?.city != null && circle.location?.city == mom.location!!.city) {
            reasons.add("Same city: ${mom.location!!.city}")
        }

        if (mom.clusterId == circle.clusterId) {
            reasons.add("Same persona cluster — high overall compatibility")
        }

        val avgAge = circleMoms.mapNotNull { it.age }.average()
        if (!avgAge.isNaN() && mom.age != null && kotlin.math.abs(mom.age!! - avgAge) <= 5) {
            reasons.add("Similar age group (avg ${avgAge.toInt()})")
        }

        return reasons
    }

    private fun generateCircleName(interests: List<String>, stages: List<String>, city: String?, index: Int): String {
        val interestPart = when {
            interests.contains("yoga") -> "Mindful Mothers"
            interests.contains("fitness") -> "Strong Moms"
            interests.contains("nutrition") -> "Nourishing Circle"
            interests.contains("meditation") -> "Peaceful Hearts"
            interests.contains("mental_health") -> "Healing Together"
            interests.isNotEmpty() -> "${interests.first().replaceFirstChar { it.uppercase() }} Circle"
            else -> "Support Circle"
        }
        val locationPart = city ?: "Online"
        return "$interestPart — $locationPart #${index + 1}"
    }
}

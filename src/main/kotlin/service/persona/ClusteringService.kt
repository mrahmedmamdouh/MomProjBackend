package com.evelolvetech.service.persona

import com.evelolvetech.data.models.Mom
import com.evelolvetech.data.repository.api.mom.MomRepository
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

data class MomFeatureVector(
    val momId: String,
    val features: DoubleArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MomFeatureVector) return false
        return momId == other.momId
    }
    override fun hashCode(): Int = momId.hashCode()
}

data class ClusterResult(
    val clusterId: String,
    val memberIds: List<String>,
    val centroid: DoubleArray,
    val description: String
)

class ClusteringService(
    private val momRepository: MomRepository
) {

    companion object {
        private const val MAX_ITERATIONS = 100
        private const val CONVERGENCE_THRESHOLD = 0.001
        private const val AGE_WEIGHT = 1.0
        private const val STAGE_WEIGHT = 2.0
        private const val INTERESTS_WEIGHT = 1.5
        private const val LOCATION_WEIGHT = 1.0
        private const val CULTURE_WEIGHT = 0.8

        val ALL_INTERESTS = PersonaService.KNOWN_INTERESTS
        val ALL_STAGES = PersonaService.KNOWN_PREGNANCY_STAGES
    }

    suspend fun clusterAllMoms(numClusters: Int = 5): List<ClusterResult> {
        val moms = momRepository.getMomsWithCompletePersona(0, 10000)
        if (moms.size < numClusters) {
            return moms.mapIndexed { index, mom ->
                ClusterResult(
                    clusterId = "cluster_$index",
                    memberIds = listOf(mom.id),
                    centroid = extractFeatures(mom),
                    description = generateClusterDescription(listOf(mom))
                )
            }
        }

        val vectors = moms.map { MomFeatureVector(it.id, extractFeatures(it)) }
        val assignments = kMeans(vectors, numClusters)

        val clusters = assignments.entries.groupBy({ it.value }, { it.key })

        val results = clusters.map { (clusterId, memberVectors) ->
            val memberMoms = memberVectors.mapNotNull { vec -> moms.find { it.id == vec.momId } }
            val centroid = computeCentroid(memberVectors.map { it.features })
            ClusterResult(
                clusterId = "cluster_$clusterId",
                memberIds = memberVectors.map { it.momId },
                centroid = centroid,
                description = generateClusterDescription(memberMoms)
            )
        }

        for (result in results) {
            for (momId in result.memberIds) {
                momRepository.updateMomCluster(momId, result.clusterId, true)
            }
        }

        return results
    }

    suspend fun assignToCluster(momId: String): String? {
        val mom = momRepository.getMomById(momId) ?: return null
        if (!PersonaService(momRepository).isPersonaComplete(mom)) return null

        val allMoms = momRepository.getMomsWithCompletePersona(0, 10000)
        if (allMoms.size < 2) {
            val clusterId = "cluster_0"
            momRepository.updateMomCluster(momId, clusterId, true)
            return clusterId
        }

        val existingClusters = allMoms.filter { it.clusterId != null }
            .groupBy { it.clusterId!! }

        if (existingClusters.isEmpty()) {
            val clusterId = "cluster_0"
            momRepository.updateMomCluster(momId, clusterId, true)
            return clusterId
        }

        val momFeatures = extractFeatures(mom)
        var bestCluster = ""
        var bestDistance = Double.MAX_VALUE

        for ((clusterId, clusterMoms) in existingClusters) {
            val centroid = computeCentroid(clusterMoms.map { extractFeatures(it) })
            val distance = euclideanDistance(momFeatures, centroid)
            if (distance < bestDistance) {
                bestDistance = distance
                bestCluster = clusterId
            }
        }

        momRepository.updateMomCluster(momId, bestCluster, true)
        return bestCluster
    }

    fun extractFeatures(mom: Mom): DoubleArray {
        val features = mutableListOf<Double>()

        val normalizedAge = ((mom.age ?: 28) - 18).toDouble() / (55 - 18) * AGE_WEIGHT
        features.add(normalizedAge)

        val stageIndex = ALL_STAGES.indexOf(mom.pregnancyStage ?: "SECOND_TRIMESTER")
        val normalizedStage = (if (stageIndex >= 0) stageIndex else 1).toDouble() / (ALL_STAGES.size - 1) * STAGE_WEIGHT
        features.add(normalizedStage)

        for (interest in ALL_INTERESTS) {
            val hasInterest = if (mom.interests.contains(interest)) 1.0 else 0.0
            features.add(hasInterest * INTERESTS_WEIGHT)
        }

        val normalizedLat = ((mom.location?.latitude ?: 30.0) - 22.0) / (31.6 - 22.0) * LOCATION_WEIGHT
        val normalizedLon = ((mom.location?.longitude ?: 31.0) - 25.0) / (35.0 - 25.0) * LOCATION_WEIGHT
        features.add(normalizedLat)
        features.add(normalizedLon)

        val cultureHash = (mom.culturalBackground?.hashCode()?.toDouble() ?: 0.0).mod(10.0) / 10.0 * CULTURE_WEIGHT
        features.add(cultureHash)

        return features.toDoubleArray()
    }

    fun computeCompatibilityScore(mom1: Mom, mom2: Mom): Double {
        var score = 0.0
        var maxScore = 0.0

        maxScore += 20.0
        if (mom1.pregnancyStage != null && mom1.pregnancyStage == mom2.pregnancyStage) {
            score += 20.0
        }

        maxScore += 25.0
        if (mom1.interests.isNotEmpty() && mom2.interests.isNotEmpty()) {
            val shared = mom1.interests.intersect(mom2.interests.toSet())
            val union = mom1.interests.union(mom2.interests.toSet())
            score += (shared.size.toDouble() / union.size) * 25.0
        }

        maxScore += 15.0
        if (mom1.age != null && mom2.age != null) {
            val ageDiff = kotlin.math.abs(mom1.age!! - mom2.age!!)
            score += when {
                ageDiff <= 2 -> 15.0
                ageDiff <= 5 -> 10.0
                ageDiff <= 10 -> 5.0
                else -> 0.0
            }
        }

        maxScore += 15.0
        if (mom1.location?.city != null && mom1.location?.city == mom2.location?.city) {
            score += 15.0
        } else if (mom1.location?.country != null && mom1.location?.country == mom2.location?.country) {
            score += 7.0
        }

        maxScore += 10.0
        if (mom1.culturalBackground != null && mom1.culturalBackground == mom2.culturalBackground) {
            score += 10.0
        }

        maxScore += 15.0
        if (mom1.circlePreferences?.preferredSessionType == mom2.circlePreferences?.preferredSessionType) {
            score += 8.0
        }
        if (mom1.circlePreferences?.preferredMeetingFrequency == mom2.circlePreferences?.preferredMeetingFrequency) {
            score += 7.0
        }

        return if (maxScore > 0) (score / maxScore) * 100.0 else 0.0
    }

    private fun kMeans(vectors: List<MomFeatureVector>, k: Int): Map<MomFeatureVector, Int> {
        if (vectors.isEmpty()) return emptyMap()

        val dimensions = vectors[0].features.size
        var centroids = initializeCentroids(vectors, k)
        var assignments = mutableMapOf<MomFeatureVector, Int>()

        repeat(MAX_ITERATIONS) { iteration ->
            val newAssignments = mutableMapOf<MomFeatureVector, Int>()
            for (vector in vectors) {
                var bestCluster = 0
                var bestDistance = Double.MAX_VALUE
                for (i in centroids.indices) {
                    val distance = euclideanDistance(vector.features, centroids[i])
                    if (distance < bestDistance) {
                        bestDistance = distance
                        bestCluster = i
                    }
                }
                newAssignments[vector] = bestCluster
            }

            val newCentroids = Array(k) { clusterIndex ->
                val clusterMembers = newAssignments.filter { it.value == clusterIndex }.keys
                if (clusterMembers.isNotEmpty()) {
                    computeCentroid(clusterMembers.map { it.features })
                } else {
                    centroids[clusterIndex]
                }
            }

            val centroidShift = centroids.zip(newCentroids).sumOf { (old, new) -> euclideanDistance(old, new) }
            centroids = newCentroids
            assignments = newAssignments

            if (centroidShift < CONVERGENCE_THRESHOLD) return assignments
        }

        return assignments
    }

    private fun initializeCentroids(vectors: List<MomFeatureVector>, k: Int): Array<DoubleArray> {
        val shuffled = vectors.shuffled(Random(42))
        return Array(k.coerceAtMost(vectors.size)) { i ->
            shuffled[i].features.copyOf()
        }
    }

    private fun computeCentroid(featuresList: List<DoubleArray>): DoubleArray {
        if (featuresList.isEmpty()) return doubleArrayOf()
        val dimensions = featuresList[0].size
        val centroid = DoubleArray(dimensions)
        for (features in featuresList) {
            for (i in features.indices) {
                centroid[i] += features[i]
            }
        }
        for (i in centroid.indices) {
            centroid[i] /= featuresList.size
        }
        return centroid
    }

    private fun euclideanDistance(a: DoubleArray, b: DoubleArray): Double {
        if (a.size != b.size) return Double.MAX_VALUE
        var sum = 0.0
        for (i in a.indices) {
            sum += (a[i] - b[i]).pow(2)
        }
        return sqrt(sum)
    }

    private fun generateClusterDescription(moms: List<Mom>): String {
        if (moms.isEmpty()) return "Empty cluster"

        val avgAge = moms.mapNotNull { it.age }.average().let { if (it.isNaN()) 0.0 else it }
        val topInterests = moms.flatMap { it.interests }
            .groupingBy { it }
            .eachCount()
            .entries.sortedByDescending { it.value }
            .take(3)
            .map { it.key }
        val topStage = moms.mapNotNull { it.pregnancyStage }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: "mixed"
        val topCity = moms.mapNotNull { it.location?.city }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: "various"

        val interestDesc = if (topInterests.isNotEmpty()) topInterests.joinToString("/") else "general"
        val stageDesc = topStage.lowercase().replace("_", " ")

        return "${interestDesc}-focused $stageDesc moms near $topCity (avg age ${avgAge.toInt()})"
    }
}

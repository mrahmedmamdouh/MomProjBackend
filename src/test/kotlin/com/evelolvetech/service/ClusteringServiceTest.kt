package com.evelolvetech.service

import com.evelolvetech.data.models.Mom
import com.evelolvetech.data.models.MomCirclePreferences
import com.evelolvetech.data.models.MomLocation
import com.evelolvetech.mocks.MockMomRepository
import com.evelolvetech.service.persona.ClusteringService
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class ClusteringServiceTest {

    private val mockMomRepository = MockMomRepository()
    private val clusteringService = ClusteringService(mockMomRepository)

    private fun createCompleteMom(
        id: String,
        age: Int,
        interests: List<String>,
        stage: String,
        city: String,
        culture: String
    ): Mom {
        return Mom(
            id = id, authUid = "auth_$id", fullName = "Mom $id", email = "$id@test.com",
            phone = "+201234567890", maritalStatus = "SINGLE", photoUrl = "",
            nidId = "nid_$id", nidRef = "/nids/nid_$id",
            age = age, interests = interests, pregnancyStage = stage,
            location = MomLocation(city = city, country = "EG"),
            culturalBackground = culture,
            circlePreferences = MomCirclePreferences(),
            personaComplete = true
        )
    }

    @Test
    fun testFeatureExtraction() {
        val mom = createCompleteMom("m1", 28, listOf("yoga", "fitness"), "SECOND_TRIMESTER", "Cairo", "Egyptian")
        val features = clusteringService.extractFeatures(mom)

        assertTrue(features.isNotEmpty())
        assertTrue(features.size > 5)
    }

    @Test
    fun testCompatibilityScoreIdenticalMoms() {
        val mom1 = createCompleteMom("m1", 28, listOf("yoga", "fitness"), "SECOND_TRIMESTER", "Cairo", "Egyptian")
        val mom2 = createCompleteMom("m2", 28, listOf("yoga", "fitness"), "SECOND_TRIMESTER", "Cairo", "Egyptian")

        val score = clusteringService.computeCompatibilityScore(mom1, mom2)
        assertTrue(score > 80.0)
    }

    @Test
    fun testCompatibilityScoreVeryDifferentMoms() {
        val mom1 = createCompleteMom("m1", 20, listOf("yoga"), "FIRST_TRIMESTER", "Cairo", "Egyptian")
        val mom2 = createCompleteMom("m2", 45, listOf("cooking"), "POSTPARTUM", "Alexandria", "Sudanese")

        val score = clusteringService.computeCompatibilityScore(mom1, mom2)
        assertTrue(score < 30.0)
    }

    @Test
    fun testCompatibilityScoreSameStageBoost() {
        val mom1 = createCompleteMom("m1", 28, listOf("yoga"), "SECOND_TRIMESTER", "Cairo", "Egyptian")
        val momSameStage = createCompleteMom("m2", 35, listOf("cooking"), "SECOND_TRIMESTER", "Giza", "Egyptian")
        val momDiffStage = createCompleteMom("m3", 35, listOf("cooking"), "FIRST_TRIMESTER", "Giza", "Egyptian")

        val scoreSameStage = clusteringService.computeCompatibilityScore(mom1, momSameStage)
        val scoreDiffStage = clusteringService.computeCompatibilityScore(mom1, momDiffStage)

        assertTrue(scoreSameStage > scoreDiffStage)
    }

    @Test
    fun testCompatibilityScoreSharedInterestsBoost() {
        val mom1 = createCompleteMom("m1", 28, listOf("yoga", "fitness", "nutrition"), "SECOND_TRIMESTER", "Cairo", "Egyptian")
        val momShared = createCompleteMom("m2", 30, listOf("yoga", "fitness"), "SECOND_TRIMESTER", "Cairo", "Egyptian")
        val momNoShared = createCompleteMom("m3", 30, listOf("cooking", "reading"), "SECOND_TRIMESTER", "Cairo", "Egyptian")

        val scoreShared = clusteringService.computeCompatibilityScore(mom1, momShared)
        val scoreNoShared = clusteringService.computeCompatibilityScore(mom1, momNoShared)

        assertTrue(scoreShared > scoreNoShared)
    }

    @Test
    fun testCompatibilityScoreSameCityBoost() {
        val mom1 = createCompleteMom("m1", 28, listOf("yoga"), "SECOND_TRIMESTER", "Cairo", "Egyptian")
        val momSameCity = createCompleteMom("m2", 28, listOf("yoga"), "SECOND_TRIMESTER", "Cairo", "Egyptian")
        val momDiffCity = createCompleteMom("m3", 28, listOf("yoga"), "SECOND_TRIMESTER", "Alexandria", "Egyptian")

        val scoreSameCity = clusteringService.computeCompatibilityScore(mom1, momSameCity)
        val scoreDiffCity = clusteringService.computeCompatibilityScore(mom1, momDiffCity)

        assertTrue(scoreSameCity > scoreDiffCity)
    }

    @Test
    fun testCompatibilityScoreCloseAgeBoost() {
        val mom1 = createCompleteMom("m1", 28, listOf("yoga"), "SECOND_TRIMESTER", "Cairo", "Egyptian")
        val momCloseAge = createCompleteMom("m2", 29, listOf("yoga"), "SECOND_TRIMESTER", "Cairo", "Egyptian")
        val momFarAge = createCompleteMom("m3", 45, listOf("yoga"), "SECOND_TRIMESTER", "Cairo", "Egyptian")

        val scoreClose = clusteringService.computeCompatibilityScore(mom1, momCloseAge)
        val scoreFar = clusteringService.computeCompatibilityScore(mom1, momFarAge)

        assertTrue(scoreClose > scoreFar)
    }

    @Test
    fun testClusteringWithEnoughMoms() = runBlocking {
        mockMomRepository.moms.clear()

        for (i in 1..4) {
            val mom = createCompleteMom("yoga_$i", 26 + i, listOf("yoga", "fitness"), "SECOND_TRIMESTER", "Cairo", "Egyptian")
            mockMomRepository.moms[mom.id] = mom
        }
        for (i in 1..4) {
            val mom = createCompleteMom("cooking_$i", 30 + i, listOf("cooking", "nutrition"), "THIRD_TRIMESTER", "Alexandria", "Egyptian")
            mockMomRepository.moms[mom.id] = mom
        }

        val results = clusteringService.clusterAllMoms(2)

        assertTrue(results.isNotEmpty())
        assertTrue(results.size <= 2)

        val totalAssigned = results.sumOf { it.memberIds.size }
        assertEquals(8, totalAssigned)
    }

    @Test
    fun testClusteringFewerMomsThanClusters() = runBlocking {
        mockMomRepository.moms.clear()
        val mom = createCompleteMom("solo", 28, listOf("yoga"), "SECOND_TRIMESTER", "Cairo", "Egyptian")
        mockMomRepository.moms[mom.id] = mom

        val results = clusteringService.clusterAllMoms(5)

        assertEquals(1, results.size)
        assertEquals("solo", results[0].memberIds[0])
    }

    @Test
    fun testAssignToClusterUpdatesDb() = runBlocking {
        mockMomRepository.moms.clear()
        for (i in 1..5) {
            val mom = createCompleteMom("existing_$i", 28, listOf("yoga"), "SECOND_TRIMESTER", "Cairo", "Egyptian")
                .copy(clusterId = "cluster_0")
            mockMomRepository.moms[mom.id] = mom
        }

        val newMom = createCompleteMom("new_mom", 29, listOf("yoga", "fitness"), "SECOND_TRIMESTER", "Cairo", "Egyptian")
        mockMomRepository.moms[newMom.id] = newMom

        val clusterId = clusteringService.assignToCluster("new_mom")

        assertNotNull(clusterId)
        assertEquals(clusterId, mockMomRepository.moms["new_mom"]!!.clusterId)
        assertTrue(mockMomRepository.moms["new_mom"]!!.personaComplete)
    }

    @Test
    fun testAssignToClusterRequiresCompletePersona() = runBlocking {
        val incompleteMom = Mom(
            id = "incomplete", authUid = "auth_inc", fullName = "Test", email = "test@test.com",
            phone = "+201234567890", maritalStatus = "SINGLE", photoUrl = "",
            nidId = "nid_inc", nidRef = "/nids/nid_inc"
        )
        mockMomRepository.moms["incomplete"] = incompleteMom

        val result = clusteringService.assignToCluster("incomplete")
        assertNull(result)
    }

    @Test
    fun testClusterDescriptionGeneration() = runBlocking {
        mockMomRepository.moms.clear()
        for (i in 1..3) {
            val mom = createCompleteMom("desc_$i", 28, listOf("yoga", "meditation"), "SECOND_TRIMESTER", "Cairo", "Egyptian")
            mockMomRepository.moms[mom.id] = mom
        }

        val results = clusteringService.clusterAllMoms(1)

        assertTrue(results.isNotEmpty())
        val desc = results[0].description
        assertTrue(desc.contains("yoga") || desc.contains("meditation") || desc.contains("Cairo"))
    }
}

package com.evelolvetech.service

import com.evelolvetech.data.models.Mom
import com.evelolvetech.data.models.MomCirclePreferences
import com.evelolvetech.data.models.MomLocation
import com.evelolvetech.mocks.MockMomRepository
import com.evelolvetech.service.persona.PersonaService
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class PersonaServiceTest {

    private val mockMomRepository = MockMomRepository()
    private val personaService = PersonaService(mockMomRepository)

    private fun createBaseMom(id: String = "mom_test"): Mom {
        return Mom(
            id = id, authUid = "auth_$id", fullName = "Test Mom", email = "$id@test.com",
            phone = "+201234567890", maritalStatus = "SINGLE", photoUrl = "", nidId = "nid_$id", nidRef = "/nids/nid_$id"
        )
    }

    @Test
    fun testExtractAgeFromNaturalLanguage() {
        assertEquals(28, personaService.extractAge("I'm 28 years old"))
        assertEquals(32, personaService.extractAge("I am 32"))
        assertEquals(25, personaService.extractAge("age is 25"))
        assertEquals(30, personaService.extractAge("30 years old"))
        assertNull(personaService.extractAge("I have 5 kids"))
        assertNull(personaService.extractAge("I'm 12 years old"))
        assertNull(personaService.extractAge("age 99"))
    }

    @Test
    fun testExtractInterestsFromNaturalLanguage() {
        val result1 = personaService.extractInterests("I love yoga and cooking")
        assertTrue(result1.contains("yoga"))
        assertTrue(result1.contains("cooking"))

        val result2 = personaService.extractInterests("I'm into fitness and meditation")
        assertTrue(result2.contains("fitness"))
        assertTrue(result2.contains("meditation"))

        val result3 = personaService.extractInterests("nothing specific")
        assertTrue(result3.isEmpty())
    }

    @Test
    fun testExtractLocationFromKnownCities() {
        val cairo = personaService.extractLocation("I live in Cairo")
        assertNotNull(cairo)
        assertEquals("Cairo", cairo!!.city)
        assertEquals("EG", cairo.country)

        val giza = personaService.extractLocation("I'm from Giza")
        assertNotNull(giza)
        assertEquals("Giza", giza!!.city)

        val alex = personaService.extractLocation("located in Alexandria")
        assertNotNull(alex)
        assertEquals("Alexandria", alex!!.city)
    }

    @Test
    fun testExtractLocationFromPattern() {
        val result = personaService.extractLocation("I live in Mansoura")
        assertNotNull(result)
        assertEquals("Mansoura", result!!.city)
    }

    @Test
    fun testExtractPregnancyStage() {
        assertEquals("FIRST_TRIMESTER", personaService.extractPregnancyStage("I'm in my first trimester"))
        assertEquals("SECOND_TRIMESTER", personaService.extractPregnancyStage("2nd trimester now"))
        assertEquals("THIRD_TRIMESTER", personaService.extractPregnancyStage("third trimester"))
        assertEquals("POSTPARTUM", personaService.extractPregnancyStage("I'm postpartum"))
        assertEquals("POSTPARTUM", personaService.extractPregnancyStage("after birth recovery"))
        assertNull(personaService.extractPregnancyStage("I feel great today"))
    }

    @Test
    fun testExtractCirclePreferences() {
        val prefs1 = personaService.extractCirclePreferences("small group, weekly, weekend morning, online")
        assertEquals(4, prefs1.preferredGroupSize)
        assertEquals("WEEKLY", prefs1.preferredMeetingFrequency)
        assertTrue(prefs1.preferredMeetingTimes.contains("WEEKEND_MORNING"))
        assertEquals("ONLINE_VIDEO", prefs1.preferredSessionType)

        val prefs2 = personaService.extractCirclePreferences("large group, biweekly, in-person, evening")
        assertEquals(7, prefs2.preferredGroupSize)
        assertEquals("BIWEEKLY", prefs2.preferredMeetingFrequency)
        assertEquals("PHYSICAL_MEETING", prefs2.preferredSessionType)
    }

    @Test
    fun testPersonaCompletionCheck() {
        val incomplete = createBaseMom()
        assertFalse(personaService.isPersonaComplete(incomplete))

        val complete = incomplete.copy(
            age = 28,
            interests = listOf("yoga"),
            location = MomLocation(city = "Cairo"),
            pregnancyStage = "SECOND_TRIMESTER",
            culturalBackground = "Egyptian",
            circlePreferences = MomCirclePreferences()
        )
        assertTrue(personaService.isPersonaComplete(complete))
    }

    @Test
    fun testPersonaCompletionMissingAge() {
        val mom = createBaseMom().copy(
            interests = listOf("yoga"), location = MomLocation(city = "Cairo"),
            pregnancyStage = "SECOND_TRIMESTER", culturalBackground = "Egyptian",
            circlePreferences = MomCirclePreferences()
        )
        assertFalse(personaService.isPersonaComplete(mom))
    }

    @Test
    fun testPersonaCompletionMissingInterests() {
        val mom = createBaseMom().copy(
            age = 28, location = MomLocation(city = "Cairo"),
            pregnancyStage = "SECOND_TRIMESTER", culturalBackground = "Egyptian",
            circlePreferences = MomCirclePreferences()
        )
        assertFalse(personaService.isPersonaComplete(mom))
    }

    @Test
    fun testDynamicQuestionAdaptsToMissingFields() {
        val emptyMom = createBaseMom()
        val q1 = personaService.getNextQuestion(emptyMom)
        assertNotNull(q1)
        assertTrue(q1!!.contains("age", ignoreCase = true) || q1.contains("old", ignoreCase = true))

        val withAge = emptyMom.copy(age = 28)
        val q2 = personaService.getNextQuestion(withAge)
        assertNotNull(q2)
        assertTrue(q2!!.contains("28") || q2.contains("activities", ignoreCase = true) || q2.contains("hobbies", ignoreCase = true))

        val withAgeAndInterests = withAge.copy(interests = listOf("yoga"))
        val q3 = personaService.getNextQuestion(withAgeAndInterests)
        assertNotNull(q3)
        assertTrue(q3!!.contains("city", ignoreCase = true) || q3.contains("yoga", ignoreCase = true))

        val allComplete = withAgeAndInterests.copy(
            location = MomLocation(city = "Cairo"),
            culturalBackground = "Egyptian",
            circlePreferences = MomCirclePreferences(),
            pregnancyStage = "SECOND_TRIMESTER"
        )
        assertNull(personaService.getNextQuestion(allComplete))
    }

    @Test
    fun testGetPersonaStatusShowsCompletion() = runBlocking {
        val mom = createBaseMom("mom_status")
        mockMomRepository.moms["mom_status"] = mom

        val result = personaService.getPersonaStatus("mom_status")

        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals(0, result.data!!.completionPercentage)
        assertEquals(6, result.data!!.totalFields)
        assertEquals(0, result.data!!.completedFields)
        assertFalse(result.data!!.isComplete)
        assertNotNull(result.data!!.nextQuestion)
    }

    @Test
    fun testUpdatePersonaFieldAge() = runBlocking {
        mockMomRepository.moms["mom_age"] = createBaseMom("mom_age")

        val result = personaService.updatePersonaField("mom_age", "age", "I'm 28 years old")

        assertTrue(result.success)
        assertEquals(28, mockMomRepository.moms["mom_age"]!!.age)
    }

    @Test
    fun testUpdatePersonaFieldInterests() = runBlocking {
        mockMomRepository.moms["mom_int"] = createBaseMom("mom_int")

        val result = personaService.updatePersonaField("mom_int", "interests", "I love yoga and meditation")

        assertTrue(result.success)
        assertTrue(mockMomRepository.moms["mom_int"]!!.interests.contains("yoga"))
        assertTrue(mockMomRepository.moms["mom_int"]!!.interests.contains("meditation"))
    }

    @Test
    fun testUpdatePersonaFieldLocation() = runBlocking {
        mockMomRepository.moms["mom_loc"] = createBaseMom("mom_loc")

        val result = personaService.updatePersonaField("mom_loc", "location", "I live in Cairo")

        assertTrue(result.success)
        assertEquals("Cairo", mockMomRepository.moms["mom_loc"]!!.location!!.city)
    }

    @Test
    fun testExtractFromMessageMultipleFields() = runBlocking {
        mockMomRepository.moms["mom_multi"] = createBaseMom("mom_multi")

        val result = personaService.extractFromMessage(
            "mom_multi",
            "I'm 28, love yoga and cooking, living in Cairo, second trimester"
        )

        assertTrue(result.success)
        val mom = mockMomRepository.moms["mom_multi"]!!
        assertEquals(28, mom.age)
        assertTrue(mom.interests.contains("yoga"))
        assertTrue(mom.interests.contains("cooking"))
        assertEquals("Cairo", mom.location!!.city)
        assertEquals("SECOND_TRIMESTER", mom.pregnancyStage)
    }

    @Test
    fun testExtractFromMessageSkipsAlreadyFilledFields() = runBlocking {
        mockMomRepository.moms["mom_skip"] = createBaseMom("mom_skip").copy(age = 30)

        personaService.extractFromMessage("mom_skip", "I'm 28 years old and love yoga")

        val mom = mockMomRepository.moms["mom_skip"]!!
        assertEquals(30, mom.age)
        assertTrue(mom.interests.contains("yoga"))
    }

    @Test
    fun testPersonaCompletionPercentageProgression() = runBlocking {
        val momId = "mom_progress"
        mockMomRepository.moms[momId] = createBaseMom(momId)

        val s1 = personaService.getPersonaStatus(momId)
        assertEquals(0, s1.data!!.completionPercentage)

        personaService.updatePersonaField(momId, "age", "28")
        val s2 = personaService.getPersonaStatus(momId)
        assertEquals(16, s2.data!!.completionPercentage)

        personaService.updatePersonaField(momId, "interests", "yoga")
        val s3 = personaService.getPersonaStatus(momId)
        assertEquals(33, s3.data!!.completionPercentage)

        personaService.updatePersonaField(momId, "location", "Cairo")
        personaService.updatePersonaField(momId, "culturalBackground", "Egyptian")
        personaService.updatePersonaField(momId, "preferences", "small group weekly online morning")
        personaService.updatePersonaField(momId, "stage", "second trimester")

        val sFinal = personaService.getPersonaStatus(momId)
        assertEquals(100, sFinal.data!!.completionPercentage)
        assertTrue(mockMomRepository.moms[momId]!!.personaComplete)
    }

    @Test
    fun testInvalidFieldReturnsError() = runBlocking {
        mockMomRepository.moms["mom_inv"] = createBaseMom("mom_inv")

        val result = personaService.updatePersonaField("mom_inv", "invalid_field", "value")

        assertFalse(result.success)
        assertTrue(result.message!!.contains("Unknown field"))
    }

    @Test
    fun testInvalidAgeReturnsError() = runBlocking {
        mockMomRepository.moms["mom_bad_age"] = createBaseMom("mom_bad_age")

        val result = personaService.updatePersonaField("mom_bad_age", "age", "not a number")

        assertFalse(result.success)
        assertTrue(result.message!!.contains("valid age"))
    }
}

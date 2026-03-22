package com.evelolvetech.service.persona

import com.evelolvetech.data.models.Mom
import com.evelolvetech.data.models.MomCirclePreferences
import com.evelolvetech.data.models.MomLocation
import com.evelolvetech.data.repository.api.mom.MomRepository
import com.evelolvetech.data.responses.BasicApiResponse
import com.evelolvetech.data.responses.PersonaStatusResponse
import com.evelolvetech.data.responses.PersonaFieldStatus

class PersonaService(
    private val momRepository: MomRepository
) {

    companion object {
        val KNOWN_INTERESTS = listOf(
            "yoga", "fitness", "exercise", "nutrition", "cooking", "meditation",
            "reading", "parenting", "breastfeeding", "mental_health", "wellness",
            "gardening", "crafts", "music", "swimming", "walking", "journaling",
            "support_groups", "self_care", "sleep", "baby_care", "postpartum_recovery"
        )

        val KNOWN_PREGNANCY_STAGES = listOf(
            "FIRST_TRIMESTER", "SECOND_TRIMESTER", "THIRD_TRIMESTER", "POSTPARTUM"
        )

        val KNOWN_CITIES_EG = listOf(
            "Cairo", "Giza", "Alexandria", "Shubra El-Kheima", "Port Said",
            "Suez", "Luxor", "Aswan", "Mansoura", "Tanta", "Ismailia",
            "Faiyum", "Zagazig", "Damietta", "Assiut", "Minya", "Beni Suef",
            "Qena", "Sohag", "Hurghada", "6th of October", "New Cairo"
        )

        val PERSONA_FIELDS = listOf("age", "interests", "location", "culturalBackground", "circlePreferences", "pregnancyStage")
    }

    suspend fun getPersonaStatus(momId: String): BasicApiResponse<PersonaStatusResponse> {
        return try {
            val mom = momRepository.getMomById(momId)
                ?: return BasicApiResponse(success = false, message = "Mom not found")

            val fields = listOf(
                PersonaFieldStatus("age", mom.age != null, mom.age?.toString()),
                PersonaFieldStatus("interests", mom.interests.isNotEmpty(), mom.interests.joinToString(", ").ifEmpty { null }),
                PersonaFieldStatus("location", mom.location != null, mom.location?.city),
                PersonaFieldStatus("culturalBackground", mom.culturalBackground != null, mom.culturalBackground),
                PersonaFieldStatus("circlePreferences", mom.circlePreferences != null, mom.circlePreferences?.preferredSessionType),
                PersonaFieldStatus("pregnancyStage", mom.pregnancyStage != null, mom.pregnancyStage)
            )

            val completedCount = fields.count { it.filled }
            val totalCount = fields.size
            val completionPercentage = ((completedCount.toDouble() / totalCount) * 100).toInt()

            BasicApiResponse(
                success = true,
                data = PersonaStatusResponse(
                    momId = momId,
                    fields = fields,
                    completedFields = completedCount,
                    totalFields = totalCount,
                    completionPercentage = completionPercentage,
                    isComplete = mom.personaComplete,
                    nextQuestion = getNextQuestion(mom),
                    clusterId = mom.clusterId
                )
            )
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error retrieving persona status: ${e.message}")
        }
    }

    suspend fun updatePersonaField(momId: String, field: String, value: String): BasicApiResponse<PersonaStatusResponse> {
        return try {
            val mom = momRepository.getMomById(momId)
                ?: return BasicApiResponse(success = false, message = "Mom not found")

            when (field.lowercase()) {
                "age" -> {
                    val age = extractAge(value)
                        ?: return BasicApiResponse(success = false, message = "Could not extract a valid age (18-55)")
                    momRepository.updateMomPersona(momId, age = age, interests = null, pregnancyStage = null, location = null, culturalBackground = null, circlePreferences = null)
                }
                "interests" -> {
                    val interests = extractInterests(value)
                    if (interests.isEmpty()) return BasicApiResponse(success = false, message = "Could not extract valid interests")
                    val merged = (mom.interests + interests).distinct()
                    momRepository.updateMomPersona(momId, age = null, interests = merged, pregnancyStage = null, location = null, culturalBackground = null, circlePreferences = null)
                }
                "location" -> {
                    val location = extractLocation(value)
                        ?: return BasicApiResponse(success = false, message = "Could not extract a valid location")
                    momRepository.updateMomPersona(momId, age = null, interests = null, pregnancyStage = null, location = location, culturalBackground = null, circlePreferences = null)
                }
                "culturalbackground", "cultural_background", "culture" -> {
                    momRepository.updateMomPersona(momId, age = null, interests = null, pregnancyStage = null, location = null, culturalBackground = value.trim(), circlePreferences = null)
                }
                "pregnancystage", "pregnancy_stage", "stage" -> {
                    val stage = extractPregnancyStage(value)
                        ?: return BasicApiResponse(success = false, message = "Invalid pregnancy stage. Must be: ${KNOWN_PREGNANCY_STAGES.joinToString(", ")}")
                    momRepository.updateMomPersona(momId, age = null, interests = null, pregnancyStage = stage, location = null, culturalBackground = null, circlePreferences = null)
                }
                "preferences", "circlepreferences", "circle_preferences" -> {
                    val preferences = extractCirclePreferences(value)
                    momRepository.updateMomPersona(momId, age = null, interests = null, pregnancyStage = null, location = null, culturalBackground = null, circlePreferences = preferences)
                }
                else -> return BasicApiResponse(success = false, message = "Unknown field: $field. Valid: ${PERSONA_FIELDS.joinToString(", ")}")
            }

            val updatedMom = momRepository.getMomById(momId)!!
            val isComplete = isPersonaComplete(updatedMom)
            if (isComplete && !updatedMom.personaComplete) {
                momRepository.updateMomCluster(momId, updatedMom.clusterId ?: "unassigned", true)
            }

            getPersonaStatus(momId)
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error updating persona: ${e.message}")
        }
    }

    suspend fun extractFromMessage(momId: String, message: String): BasicApiResponse<PersonaStatusResponse> {
        return try {
            val mom = momRepository.getMomById(momId)
                ?: return BasicApiResponse(success = false, message = "Mom not found")

            var updated = false

            if (mom.age == null) {
                val age = extractAge(message)
                if (age != null) {
                    momRepository.updateMomPersona(momId, age = age, interests = null, pregnancyStage = null, location = null, culturalBackground = null, circlePreferences = null)
                    updated = true
                }
            }

            val newInterests = extractInterests(message)
            if (newInterests.isNotEmpty()) {
                val merged = (mom.interests + newInterests).distinct()
                if (merged.size > mom.interests.size) {
                    momRepository.updateMomPersona(momId, age = null, interests = merged, pregnancyStage = null, location = null, culturalBackground = null, circlePreferences = null)
                    updated = true
                }
            }

            if (mom.location == null) {
                val location = extractLocation(message)
                if (location != null) {
                    momRepository.updateMomPersona(momId, age = null, interests = null, pregnancyStage = null, location = location, culturalBackground = null, circlePreferences = null)
                    updated = true
                }
            }

            if (mom.pregnancyStage == null) {
                val stage = extractPregnancyStage(message)
                if (stage != null) {
                    momRepository.updateMomPersona(momId, age = null, interests = null, pregnancyStage = stage, location = null, culturalBackground = null, circlePreferences = null)
                    updated = true
                }
            }

            if (updated) {
                val updatedMom = momRepository.getMomById(momId)!!
                if (isPersonaComplete(updatedMom) && !updatedMom.personaComplete) {
                    momRepository.updateMomCluster(momId, updatedMom.clusterId ?: "unassigned", true)
                }
            }

            getPersonaStatus(momId)
        } catch (e: Exception) {
            BasicApiResponse(success = false, message = "Error extracting persona data: ${e.message}")
        }
    }

    fun isPersonaComplete(mom: Mom): Boolean {
        return mom.age != null &&
            mom.interests.isNotEmpty() &&
            mom.location != null &&
            mom.pregnancyStage != null &&
            mom.culturalBackground != null &&
            mom.circlePreferences != null
    }

    fun getNextQuestion(mom: Mom): String? {
        return when {
            mom.age == null -> buildAgeQuestion(mom)
            mom.interests.isEmpty() -> buildInterestsQuestion(mom)
            mom.location == null -> buildLocationQuestion(mom)
            mom.culturalBackground == null -> buildCulturalQuestion(mom)
            mom.circlePreferences == null -> buildPreferencesQuestion(mom)
            mom.pregnancyStage == null -> buildStageQuestion(mom)
            else -> null
        }
    }

    fun extractAge(text: String): Int? {
        val patterns = listOf(
            Regex("""(?:i'?m|i am|age is|aged?)\s*(\d{2})""", RegexOption.IGNORE_CASE),
            Regex("""(\d{2})\s*(?:years?\s*old|yo|y\.o\.)""", RegexOption.IGNORE_CASE),
            Regex("""^(\d{2})$""")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val age = match.groupValues[1].toIntOrNull()
                if (age != null && age in 18..55) return age
            }
        }
        return null
    }

    fun extractInterests(text: String): List<String> {
        val lowerText = text.lowercase()
        return KNOWN_INTERESTS.filter { interest ->
            lowerText.contains(interest.replace("_", " ")) || lowerText.contains(interest)
        }
    }

    fun extractLocation(text: String): MomLocation? {
        val lowerText = text.lowercase()
        for (city in KNOWN_CITIES_EG) {
            if (lowerText.contains(city.lowercase())) {
                return MomLocation(city = city, country = "EG")
            }
        }
        val patterns = listOf(
            Regex("""(?:live in|from|in|located in|city is)\s+([A-Z][a-z]+(?:\s[A-Z][a-z]+)*)""", RegexOption.IGNORE_CASE),
            Regex("""([A-Z][a-z]+(?:\s[A-Z][a-z]+)*)\s*(?:city|area|district)""", RegexOption.IGNORE_CASE)
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return MomLocation(city = match.groupValues[1].trim(), country = "EG")
            }
        }
        return null
    }

    fun extractPregnancyStage(text: String): String? {
        val lowerText = text.lowercase()
        return when {
            lowerText.contains("first") || lowerText.contains("1st") || lowerText.contains("trimester 1") -> "FIRST_TRIMESTER"
            lowerText.contains("second") || lowerText.contains("2nd") || lowerText.contains("trimester 2") -> "SECOND_TRIMESTER"
            lowerText.contains("third") || lowerText.contains("3rd") || lowerText.contains("trimester 3") -> "THIRD_TRIMESTER"
            lowerText.contains("postpartum") || lowerText.contains("after birth") || lowerText.contains("post partum") || lowerText.contains("postnatal") -> "POSTPARTUM"
            KNOWN_PREGNANCY_STAGES.any { lowerText.contains(it.lowercase()) } -> KNOWN_PREGNANCY_STAGES.first { lowerText.contains(it.lowercase()) }
            else -> null
        }
    }

    fun extractCirclePreferences(text: String): MomCirclePreferences {
        val lowerText = text.lowercase()
        val groupSize = when {
            lowerText.contains("small") || lowerText.contains("2") || lowerText.contains("3") || lowerText.contains("4") -> 4
            lowerText.contains("large") || lowerText.contains("7") -> 7
            else -> 6
        }
        val frequency = when {
            lowerText.contains("daily") -> "DAILY"
            lowerText.contains("biweekly") || lowerText.contains("bi-weekly") || lowerText.contains("twice a month") -> "BIWEEKLY"
            lowerText.contains("monthly") -> "MONTHLY"
            else -> "WEEKLY"
        }
        val times = mutableListOf<String>()
        if (lowerText.contains("morning")) times.add(if (lowerText.contains("weekend")) "WEEKEND_MORNING" else "WEEKDAY_MORNING")
        if (lowerText.contains("evening")) times.add(if (lowerText.contains("weekend")) "WEEKEND_EVENING" else "WEEKDAY_EVENING")
        if (lowerText.contains("afternoon")) times.add("WEEKDAY_AFTERNOON")
        if (times.isEmpty()) times.add("WEEKEND_MORNING")

        val sessionType = if (lowerText.contains("physical") || lowerText.contains("in person") || lowerText.contains("in-person")) {
            "PHYSICAL_MEETING"
        } else {
            "ONLINE_VIDEO"
        }

        return MomCirclePreferences(
            preferredGroupSize = groupSize,
            preferredMeetingFrequency = frequency,
            preferredMeetingTimes = times,
            preferredSessionType = sessionType
        )
    }

    private fun buildAgeQuestion(mom: Mom): String {
        return if (mom.interests.isNotEmpty()) {
            "Since you're interested in ${mom.interests.joinToString(" and ")}, what age group are you in? This helps match you with moms at similar life stages."
        } else {
            "To help match you with the right support circle, how old are you?"
        }
    }

    private fun buildInterestsQuestion(mom: Mom): String {
        return if (mom.age != null) {
            "You're ${mom.age} — what activities or hobbies do you enjoy? (yoga, fitness, nutrition, cooking, meditation, reading, etc.)"
        } else {
            "What activities or hobbies do you enjoy? This helps us find circles that match your interests."
        }
    }

    private fun buildLocationQuestion(mom: Mom): String {
        return if (mom.interests.isNotEmpty()) {
            "Since you enjoy ${mom.interests.first()}, which city are you in? This helps us find local support circles and activities near you."
        } else {
            "Which city are you in? This helps us find the closest support circles."
        }
    }

    private fun buildCulturalQuestion(mom: Mom): String {
        return if (mom.location != null) {
            "You're in ${mom.location!!.city}! What's your cultural background? This helps us create culturally inclusive circles."
        } else {
            "What's your cultural background? This helps us create culturally inclusive circles."
        }
    }

    private fun buildPreferencesQuestion(mom: Mom): String {
        return "What type of support circle do you prefer? (small/large group, weekly/biweekly, online/in-person, morning/evening)"
    }

    private fun buildStageQuestion(mom: Mom): String {
        return "Which stage are you in? (first trimester, second trimester, third trimester, or postpartum)"
    }
}

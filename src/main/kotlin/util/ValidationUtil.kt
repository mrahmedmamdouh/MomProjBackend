package com.evelolvetech.util

object ValidationUtil {

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }

    fun isValidPhone(phone: String): Boolean {
        val cleanedPhone = phone.replace(Regex("[^0-9]"), "")
        if (cleanedPhone.length !in 10..15) {
            return false
        }
        val hasValidFormat = phone.matches("^[+]?[0-9]{1,4}[\\s\\-\\(\\)\\.]*[0-9\\s\\-\\(\\)\\.]+$".toRegex())
        return hasValidFormat
    }

    fun isValidMaritalStatus(status: String): Boolean {
        val validStatuses = setOf("SINGLE", "MARRIED", "DIVORCED", "WIDOWED")
        return validStatuses.contains(status.uppercase())
    }

    fun isValidSpecialization(specialization: String): Boolean {
        val validSpecializations = setOf(
            "PSYCHIATRIST",
            "CLINICAL_PSYCHOLOGIST",
            "COUNSELING_PSYCHOLOGIST",
            "PERINATAL_MENTAL_HEALTH",
            "FAMILY_THERAPIST",
            "TRAUMA_THERAPIST",
            "COGNITIVE_BEHAVIORAL_THERAPIST",
            "GROUP_THERAPIST"
        )
        return validSpecializations.contains(specialization.uppercase())
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}

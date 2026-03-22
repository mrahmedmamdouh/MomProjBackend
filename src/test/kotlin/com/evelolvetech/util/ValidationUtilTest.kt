package com.evelolvetech.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationUtilTest {

    @Test
    fun testValidEmails() {
        assertTrue(ValidationUtil.isValidEmail("user@example.com"))
        assertTrue(ValidationUtil.isValidEmail("test.email+tag@domain.co.uk"))
        assertTrue(ValidationUtil.isValidEmail("user123@test-domain.org"))
        assertTrue(ValidationUtil.isValidEmail("a@b.co"))
    }

    @Test
    fun testInvalidEmails() {
        assertFalse(ValidationUtil.isValidEmail(""))
        assertFalse(ValidationUtil.isValidEmail("invalid-email"))
        assertFalse(ValidationUtil.isValidEmail("@domain.com"))
        assertFalse(ValidationUtil.isValidEmail("user@"))
        assertFalse(ValidationUtil.isValidEmail("user@domain"))
    }

    @Test
    fun testValidPhones() {
        assertTrue(ValidationUtil.isValidPhone("+1234567890"))
        assertTrue(ValidationUtil.isValidPhone("1234567890"))
        assertTrue(ValidationUtil.isValidPhone("+1 (234) 567-8900"))
        assertTrue(ValidationUtil.isValidPhone("+44 20 7946 0958"))
        assertTrue(ValidationUtil.isValidPhone("123-456-7890"))
        assertTrue(ValidationUtil.isValidPhone("123.456.7890"))
    }

    @Test
    fun testInvalidPhones() {
        assertFalse(ValidationUtil.isValidPhone(""))
        assertFalse(ValidationUtil.isValidPhone("123"))
        assertFalse(ValidationUtil.isValidPhone("12345678901234567890"))
        assertFalse(ValidationUtil.isValidPhone("abc-def-ghij"))
        assertFalse(ValidationUtil.isValidPhone("123-abc-7890"))
        assertFalse(ValidationUtil.isValidPhone("+"))
        assertFalse(ValidationUtil.isValidPhone("1234567890123456"))
    }

    @Test
    fun testValidMaritalStatuses() {
        assertTrue(ValidationUtil.isValidMaritalStatus("SINGLE"))
        assertTrue(ValidationUtil.isValidMaritalStatus("MARRIED"))
        assertTrue(ValidationUtil.isValidMaritalStatus("DIVORCED"))
        assertTrue(ValidationUtil.isValidMaritalStatus("WIDOWED"))
        assertTrue(ValidationUtil.isValidMaritalStatus("single"))
        assertTrue(ValidationUtil.isValidMaritalStatus("married"))
        assertTrue(ValidationUtil.isValidMaritalStatus("divorced"))
        assertTrue(ValidationUtil.isValidMaritalStatus("widowed"))
    }

    @Test
    fun testInvalidMaritalStatuses() {
        assertFalse(ValidationUtil.isValidMaritalStatus(""))
        assertFalse(ValidationUtil.isValidMaritalStatus("INVALID"))
        assertFalse(ValidationUtil.isValidMaritalStatus("ENGAGED"))
        assertFalse(ValidationUtil.isValidMaritalStatus("SEPARATED"))
        assertFalse(ValidationUtil.isValidMaritalStatus("123"))
    }

    @Test
    fun testValidSpecializations() {
        assertTrue(ValidationUtil.isValidSpecialization("PSYCHIATRIST"))
        assertTrue(ValidationUtil.isValidSpecialization("CLINICAL_PSYCHOLOGIST"))
        assertTrue(ValidationUtil.isValidSpecialization("COUNSELING_PSYCHOLOGIST"))
        assertTrue(ValidationUtil.isValidSpecialization("PERINATAL_MENTAL_HEALTH"))
        assertTrue(ValidationUtil.isValidSpecialization("FAMILY_THERAPIST"))
        assertTrue(ValidationUtil.isValidSpecialization("TRAUMA_THERAPIST"))
        assertTrue(ValidationUtil.isValidSpecialization("COGNITIVE_BEHAVIORAL_THERAPIST"))
        assertTrue(ValidationUtil.isValidSpecialization("GROUP_THERAPIST"))
        assertTrue(ValidationUtil.isValidSpecialization("psychiatrist"))
        assertTrue(ValidationUtil.isValidSpecialization("group_therapist"))
    }

    @Test
    fun testInvalidSpecializations() {
        assertFalse(ValidationUtil.isValidSpecialization(""))
        assertFalse(ValidationUtil.isValidSpecialization("DERMATOLOGY"))
        assertFalse(ValidationUtil.isValidSpecialization("GENERAL_MEDICINE"))
        assertFalse(ValidationUtil.isValidSpecialization("PEDIATRICS"))
        assertFalse(ValidationUtil.isValidSpecialization("123"))
        assertFalse(ValidationUtil.isValidSpecialization("GENERAL"))
    }

    @Test
    fun testValidPasswords() {
        assertTrue(ValidationUtil.isValidPassword("password123"))
        assertTrue(ValidationUtil.isValidPassword("123456"))
        assertTrue(ValidationUtil.isValidPassword("abcdef"))
        assertTrue(ValidationUtil.isValidPassword("verylongpassword123"))
        assertTrue(ValidationUtil.isValidPassword("P@ssw0rd!"))
    }

    @Test
    fun testInvalidPasswords() {
        assertFalse(ValidationUtil.isValidPassword(""))
        assertFalse(ValidationUtil.isValidPassword("12345"))
        assertFalse(ValidationUtil.isValidPassword("abc"))
        assertFalse(ValidationUtil.isValidPassword("pass"))
        assertFalse(ValidationUtil.isValidPassword("word"))
    }
}

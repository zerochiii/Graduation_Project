package com.example.graduationproject.ui.screens

import com.example.graduationproject.DataClass.RegisterElderRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationRoleTest {
    @Test
    fun defaultRole_isElder() {
        assertEquals(RegistrationRole.ELDER, defaultRegistrationRole())
    }

    @Test
    fun roleSelection_hasDistinctElderAndFamilyValues() {
        assertEquals("elder", RegistrationRole.ELDER.wireValue)
        assertEquals("family", RegistrationRole.FAMILY.wireValue)
    }

    @Test
    fun currentBackend_allowsElderButBlocksFamilyRegistration() {
        assertTrue(currentBackendSupportsRegistration(RegistrationRole.ELDER))
        assertFalse(currentBackendSupportsRegistration(RegistrationRole.FAMILY))
    }

    @Test
    fun registrationRequest_isRoleAware() {
        val request = RegisterElderRequest(
            name = "測試家屬",
            username = "family-test",
            email = "family@example.com",
            phone = "0912345678",
            password = "password",
            role = RegistrationRole.FAMILY.wireValue
        )

        assertEquals("family", request.role)
    }

    @Test
    fun registrationRequest_defaultsToElderForExistingCallers() {
        val request = RegisterElderRequest(
            name = "測試長者",
            username = "elder-test",
            email = "elder@example.com",
            phone = "0912345678",
            password = "password"
        )

        assertEquals("elder", request.role)
    }
}

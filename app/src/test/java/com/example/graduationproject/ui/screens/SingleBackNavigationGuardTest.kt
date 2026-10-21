package com.example.graduationproject.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SingleBackNavigationGuardTest {
    @Test
    fun rapidBackRequests_triggerNavigationOnlyOnce() {
        val guard = SingleBackNavigationGuard()
        var navigationCount = 0

        assertTrue(guard.tryNavigate { navigationCount++ })
        repeat(9) {
            assertFalse(guard.tryNavigate { navigationCount++ })
        }

        assertTrue(navigationCount == 1)
    }
}

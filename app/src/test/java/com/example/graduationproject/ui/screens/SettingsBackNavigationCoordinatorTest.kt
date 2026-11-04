package com.example.graduationproject.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsBackNavigationCoordinatorTest {
    @Test
    fun repeatedBackBeforeDestinationChange_navigatesOnlyOnce() {
        val coordinator = readyCoordinator("profile_summary")
        var navigationCount = 0

        assertTrue(coordinator.tryNavigate("profile_summary") { navigationCount++ })
        repeat(9) {
            assertFalse(coordinator.tryNavigate("profile_summary") { navigationCount++ })
        }

        assertEquals(1, navigationCount)
    }

    @Test
    fun destinationChange_blocksRemainingEventsUntilNewDestinationSettles() {
        val coordinator = readyCoordinator("profile_summary")
        var navigationCount = 0
        coordinator.tryNavigate("profile_summary") { navigationCount++ }

        coordinator.onDestinationChanged("settings")

        repeat(9) {
            assertFalse(coordinator.tryNavigate("settings") { navigationCount++ })
        }
        assertEquals(1, navigationCount)
    }

    @Test
    fun newIntentionalActionAfterDestinationSettles_canNavigate() {
        val coordinator = readyCoordinator("profile_summary")
        var navigationCount = 0
        coordinator.tryNavigate("profile_summary") { navigationCount++ }
        coordinator.onDestinationChanged("settings")
        coordinator.onDestinationSettled("settings")

        assertTrue(coordinator.tryNavigate("settings") { navigationCount++ })

        assertEquals(2, navigationCount)
    }

    @Test
    fun settingsCenterNormalBack_works() {
        val coordinator = readyCoordinator("settings")
        var navigationCount = 0

        assertTrue(coordinator.tryNavigate("settings") { navigationCount++ })

        assertEquals(1, navigationCount)
    }

    @Test
    fun leavingAndReturningToSettings_doesNotPermanentlyLockGuard() {
        val coordinator = readyCoordinator("settings")
        coordinator.tryNavigate("settings") { }
        coordinator.onDestinationChanged("home")
        coordinator.onDestinationSettled("home")
        coordinator.onDestinationChanged("settings")
        coordinator.onDestinationSettled("settings")

        assertTrue(coordinator.tryNavigate("settings") { })
    }

    @Test
    fun unsavedDisplayNameBack_showsDialogInsteadOfNavigating() {
        var navigationCount = 0
        var dialogCount = 0

        val navigated = dispatchSettingsBackRequest(
            backNavigationEnabled = false,
            onNavigateBack = { navigationCount++ },
            onBackBlocked = { dialogCount++ }
        )

        assertFalse(navigated)
        assertEquals(0, navigationCount)
        assertEquals(1, dialogCount)
    }

    @Test
    fun nonSettingsDestination_isNotIntercepted() {
        val coordinator = readyCoordinator("home")

        assertFalse(coordinator.tryNavigate("home") { error("must not navigate") })
    }

    private fun readyCoordinator(destination: String): SettingsBackNavigationCoordinator =
        SettingsBackNavigationCoordinator().apply {
            onDestinationChanged(destination)
            onDestinationSettled(destination)
        }
}

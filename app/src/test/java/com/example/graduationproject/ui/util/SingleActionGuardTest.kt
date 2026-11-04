package com.example.graduationproject.ui.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SingleActionGuardTest {
    @Test
    fun firstAction_isAccepted() {
        val guard = SingleActionGuard()

        assertTrue(guard.tryStart())
    }

    @Test
    fun repeatedAction_whileRunning_isIgnored() {
        val guard = SingleActionGuard()

        assertTrue(guard.tryStart())
        assertFalse(guard.tryStart())
        assertFalse(guard.tryStart())
    }

    @Test
    fun failedAction_afterFinish_canBeRetried() {
        val guard = SingleActionGuard()

        assertTrue(guard.tryStart())
        guard.finish()

        assertTrue(guard.tryStart())
    }
}

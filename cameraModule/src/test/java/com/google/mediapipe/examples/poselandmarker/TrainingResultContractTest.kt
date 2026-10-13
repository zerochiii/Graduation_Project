package com.google.mediapipe.examples.poselandmarker

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingResultContractTest {

    @Test
    fun expectedCodeMatchesOnlyTheCompletedFragmentResult() {
        assertTrue(resultContainsExpectedCode("A-3,B-2,C-3,D-3", "B-2"))
        assertFalse(resultContainsExpectedCode("A-3,B-2,C-3,D-3", "B-3"))
        assertFalse(resultContainsExpectedCode("A-3,B-2,C-3,D-3", null))
    }

    @Test
    fun expectedExerciseCodeMustMatchLevelAndCameraResult() {
        assertEquals(
            "B-2",
            resolveExpectedExerciseCode("A-3,B-2,C-3,D-3", "B+", "B-2")
        )
        assertNull(resolveExpectedExerciseCode("A-3,B-2,C-3,D-3", "B", "B-3"))
        assertNull(resolveExpectedExerciseCode("A-3,B-2,C-3,D-3", "D", "B-2"))
    }

    @Test
    fun singleTrainingResultGuard_acceptsOnlyFirstResult() {
        val guard = SingleTrainingResultGuard()

        assertTrue(guard.trySubmit())
        assertFalse(guard.trySubmit())
    }
}

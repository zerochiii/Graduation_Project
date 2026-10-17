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

    @Test
    fun saveResponse_requiresHttpAndBusinessSuccess() {
        assertTrue(isSuccessfulSaveResponse(200, "{\"success\":true}"))
        assertFalse(isSuccessfulSaveResponse(200, "{\"success\":false}"))
        assertFalse(isSuccessfulSaveResponse(400, "{\"success\":false}"))
        assertFalse(isSuccessfulSaveResponse(500, "{\"success\":true}"))
    }

    @Test
    fun saveResponse_rejectsMalformedOrMissingSuccess() {
        assertFalse(isSuccessfulSaveResponse(200, "not-json"))
        assertFalse(isSuccessfulSaveResponse(200, "{\"message\":\"ok\"}"))
        assertFalse(isSuccessfulSaveResponse(200, null))
    }

    @Test
    fun trainingResult_isPublishedOnlyAfterSuccessfulSave() {
        val result = ExerciseResult(exerciseName = "test", exerciseId = "D-6")

        assertNull(resultToPublishAfterSave(result, saveSucceeded = false))
        assertEquals(result, resultToPublishAfterSave(result, saveSucceeded = true))
    }

    @Test
    fun trainingSaveUrl_usesConfiguredBaseUrlWithOneSeparator() {
        assertEquals(
            "http://127.0.0.1:8000/api/save_exercise_record.php",
            trainingSaveUrl("http://127.0.0.1:8000//").toString()
        )
    }
}

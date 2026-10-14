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
        val requestId = "11111111-1111-4111-8111-111111111111"
        val success = """{"success":true,"request_id":"$requestId","idempotent_replay":false}"""

        assertTrue(isSuccessfulSaveResponse(200, success, requestId))
        assertFalse(
            isSuccessfulSaveResponse(
                200,
                """{"success":false,"request_id":"$requestId"}""",
                requestId
            )
        )
        assertFalse(isSuccessfulSaveResponse(400, success, requestId))
        assertFalse(isSuccessfulSaveResponse(409, success, requestId))
        assertFalse(isSuccessfulSaveResponse(500, success, requestId))
    }

    @Test
    fun saveResponse_rejectsMalformedOrMissingSuccess() {
        val requestId = "11111111-1111-4111-8111-111111111111"

        assertFalse(isSuccessfulSaveResponse(200, "not-json", requestId))
        assertFalse(isSuccessfulSaveResponse(200, "{\"message\":\"ok\"}", requestId))
        assertFalse(isSuccessfulSaveResponse(200, "", requestId))
        assertFalse(isSuccessfulSaveResponse(200, null, requestId))
    }

    @Test
    fun pendingTrainingSave_reusesOneRequestIdAndPayload() {
        val result = ExerciseResult(
            exerciseName = "test",
            exerciseId = "D-6",
            accuracy = 88f,
            durationSeconds = 120
        )
        var generatedCount = 0
        val requestIdFactory = {
            generatedCount++
            "11111111-1111-4111-8111-111111111111"
        }

        val first = getOrCreatePendingTrainingSave(
            current = null,
            accountId = 7,
            exerciseCode = "D-6",
            result = result,
            requestIdFactory = requestIdFactory
        )
        val reused = getOrCreatePendingTrainingSave(
            current = first,
            accountId = 99,
            exerciseCode = "A-1",
            result = ExerciseResult(exerciseName = "other"),
            requestIdFactory = requestIdFactory
        )

        assertEquals(first, reused)
        assertEquals(1, generatedCount)
        assertEquals(first.requestId, buildTrainingSaveRequestBody(first)["request_id"].asString)
        assertEquals(7, buildTrainingSaveRequestBody(first)["account_id"].asInt)
        assertEquals("D-6", buildTrainingSaveRequestBody(first)["exercise_code"].asString)
    }

    @Test
    fun saveResponse_requiresMatchingRequestId() {
        val pendingId = "11111111-1111-4111-8111-111111111111"
        val otherId = "22222222-2222-4222-8222-222222222222"

        assertTrue(
            isSuccessfulSaveResponse(
                200,
                """{"success":true,"request_id":"$pendingId","idempotent_replay":false}""",
                pendingId
            )
        )
        assertFalse(
            isSuccessfulSaveResponse(
                200,
                """{"success":true,"request_id":"$otherId","idempotent_replay":false}""",
                pendingId
            )
        )
        assertFalse(
            isSuccessfulSaveResponse(
                200,
                """{"success":true,"idempotent_replay":false}""",
                pendingId
            )
        )
    }

    @Test
    fun saveResponse_acceptsOriginalAndIdempotentReplay() {
        val requestId = "11111111-1111-4111-8111-111111111111"

        listOf(false, true).forEach { replay ->
            assertTrue(
                isSuccessfulSaveResponse(
                    200,
                    """{"success":true,"request_id":"$requestId","idempotent_replay":$replay}""",
                    requestId
                )
            )
        }
    }

    @Test
    fun trainingResult_isPublishedOnlyAfterSuccessfulSave() {
        val result = ExerciseResult(exerciseName = "test", exerciseId = "D-6")

        assertNull(resultToPublishAfterSave(result, saveSucceeded = false))
        assertEquals(result, resultToPublishAfterSave(result, saveSucceeded = true))
    }
}

package com.example.graduationproject.ui.screens

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import com.example.graduationproject.resolveCameraExerciseRoute
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Proxy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AssignmentViewModelTest {

    @Test
    fun beginTraining_doesNotCompleteExercise() = runBlocking {
        val viewModel = readyViewModel()

        val attempt = viewModel.beginTraining("A1")

        assertNotNull(attempt)
        assertFalse(viewModel.loadedState().completedExerciseIds.contains("A1"))
    }

    @Test
    fun permissionDenied_doesNotCompleteOrUnlock() = runBlocking {
        val viewModel = readyViewModel()
        val attempt = requireNotNull(viewModel.beginTraining("A1"))

        assertFalse(viewModel.applyTrainingResult(attempt.id, "A1", completed = false))

        val state = viewModel.loadedState()
        assertFalse(state.completedExerciseIds.contains("A1"))
        assertEquals("A1", state.exercises.first { it.status.name == "CURRENT" }.id)
    }

    @Test
    fun returningBeforeRecognitionCompletes_doesNotUnlock() = runBlocking {
        val viewModel = readyViewModel()
        val attempt = requireNotNull(viewModel.beginTraining("A1"))

        viewModel.applyTrainingResult(attempt.id, "A1", completed = false)

        assertEquals("A1", viewModel.loadedState().exercises.first { it.status.name == "CURRENT" }.id)
    }

    @Test
    fun successfulRecognition_completesCurrentAndUnlocksNext() = runBlocking {
        val viewModel = readyViewModel()
        val attempt = requireNotNull(viewModel.beginTraining("A1"))

        assertTrue(applySuccessfulTrainingResult(viewModel, attempt.id, "A1"))

        val state = viewModel.uiState.first { "A1" in it.completedExerciseIds }
        assertEquals("A2", state.exercises.first { it.status.name == "CURRENT" }.id)
    }

    @Test
    fun duplicateSuccessResult_isAppliedOnlyOnce() = runBlocking {
        val viewModel = readyViewModel()
        val attempt = requireNotNull(viewModel.beginTraining("A1"))

        assertTrue(applySuccessfulTrainingResult(viewModel, attempt.id, "A1"))
        assertFalse(viewModel.applyTrainingResult(attempt.id, "A1", completed = true))

        assertEquals(setOf("A1"), viewModel.uiState.first { "A1" in it.completedExerciseIds }.completedExerciseIds)
    }

    @Test
    fun staleResult_cannotCompleteNewAttempt() = runBlocking {
        val viewModel = readyViewModel()
        val stale = requireNotNull(viewModel.beginTraining("A1"))
        viewModel.applyTrainingResult(stale.id, "A1", completed = false)
        val current = requireNotNull(viewModel.beginTraining("A1"))

        assertFalse(viewModel.applyTrainingResult(stale.id, "A1", completed = true))
        assertTrue(applySuccessfulTrainingResult(viewModel, current.id, "A1"))
    }

    @Test
    fun mismatchedExerciseId_cannotCompleteCurrentExercise() = runBlocking {
        val viewModel = readyViewModel()
        val attempt = requireNotNull(viewModel.beginTraining("A1"))

        assertFalse(viewModel.applyTrainingResult(attempt.id, "A2", completed = true))

        assertFalse(viewModel.loadedState().completedExerciseIds.contains("A1"))
    }

    @Test
    fun rapidRepeatedStart_createsOnlyOneAttempt() = runBlocking {
        val viewModel = readyViewModel()

        val first = viewModel.beginTraining("A1")
        val second = viewModel.beginTraining("A1")

        assertNotNull(first)
        assertNull(second)
    }

    @Test
    fun lockedExerciseCannotStart() = runBlocking {
        val viewModel = readyViewModel()

        assertNull(viewModel.beginTraining("A2"))
        assertTrue(viewModel.loadedState().completedExerciseIds.isEmpty())
    }

    @Test
    fun completingLastExercise_finishesPlanWithoutExtraUnlock() = runBlocking {
        val viewModel = readyViewModel()
        for (exerciseId in listOf("A1", "A2", "A3", "A4", "A5", "A6", "A7")) {
            val attempt = requireNotNull(viewModel.beginTraining(exerciseId))
            assertTrue(applySuccessfulTrainingResult(viewModel, attempt.id, exerciseId))
            viewModel.uiState.first { exerciseId in it.completedExerciseIds }
        }

        val state = viewModel.loadedState()
        assertEquals(7, state.completedExerciseIds.size)
        assertTrue(state.exercises.all { it.status.name == "COMPLETED" })
        assertFalse(state.exercises.any { it.status.name == "CURRENT" })
        assertNull(viewModel.beginTraining("A1"))
    }

    @Test
    fun sameDayServerRecords_restoreCompletedDailyPlan() = runBlocking {
        val viewModel = testViewModel()
        viewModel.updateParams(level = "A", day = 1, week = 1)

        restoreServerRecords(
            viewModel,
            "A-1", "A-2", "A-3", "A-4", "A-5", "A-6", "A-7"
        )

        val state = viewModel.loadedState()
        assertEquals(
            setOf("A1", "A2", "A3", "A4", "A5", "A6", "A7"),
            state.completedExerciseIds
        )
        assertTrue(state.exercises.all { it.status.name == "COMPLETED" })
        assertNull(viewModel.beginTraining("A1"))
    }

    @Test
    fun newDateWithNoServerRecords_startsNewDailyPlan() = runBlocking {
        val viewModel = testViewModel()
        viewModel.updateParams(level = "A", day = 1, week = 1)

        restoreServerRecords(viewModel)

        val state = viewModel.loadedState()
        assertTrue(state.completedExerciseIds.isEmpty())
        assertEquals("A1", state.exercises.first { it.status.name == "CURRENT" }.id)
        assertNotNull(viewModel.beginTraining("A1"))
    }

    @Test
    fun resetForAccountChange_clearsPlanCompletionAndActiveAttempt() = runBlocking {
        val viewModel = readyViewModel()
        val attempt = requireNotNull(viewModel.beginTraining("A1"))
        assertTrue(applySuccessfulTrainingResult(viewModel, attempt.id, "A1"))
        assertNotNull(viewModel.beginTraining("A2"))

        viewModel.resetForAccountChange()

        val state = viewModel.uiState.value
        assertNull(state.dailyPlan)
        assertTrue(state.exercises.isEmpty())
        assertTrue(state.completedExerciseIds.isEmpty())

        TestApplicationState.accountId = -1
        viewModel.updateParams(level = "A", day = 1, week = 1)
        markRecordsReady(viewModel)
        assertNotNull(viewModel.beginTraining("A1"))
    }

    @Test
    fun selectedExercisesResolveDirectlyToTheirOwnCameraScreensAndResultCodes() {
        val expectedRoutes = mapOf(
            "A1" to ("walking_fragment" to "A-1"),
            "A2" to ("squeeze_ball_fragment" to "A-2"),
            "A3" to ("bottle_lift_fragment" to "A-3"),
            "A4" to ("weighted_leg_stretch_fragment" to "A-4"),
            "A5" to ("chair_stand_fragment" to "A-5"),
            "A6" to ("camera_fragment" to "A-6"),
            "A7" to ("stretch_fragment" to "A-7"),
            "B7" to ("walking_b_fragment" to "B-1"),
            "B1" to ("bottle_lift_fragment" to "B-2"),
            "B2" to ("squeeze_ball_fragment" to "B-3"),
            "B3" to ("simulated_sitting_fragment" to "B-4"),
            "B4" to ("toe_heel_walking_fragment" to "B-5"),
            "B5" to ("chair_arm_stretch_fragment" to "B-6"),
            "B6" to ("stretch_fragment" to "B-7"),
            "C8" to ("walking_c_fragment" to "C-1"),
            "C1" to ("wring_towel_fragment" to "C-2"),
            "C2" to ("bottle_lift_fragment" to "C-3"),
            "C3" to ("chair_stand_fragment" to "C-4"),
            "C4" to ("obstacle_crossing_fragment" to "C-5"),
            "C5" to ("figure8_walking_fragment" to "C-6"),
            "C6" to ("leg_stretch_fragment" to "C-7"),
            "C7" to ("stretch_fragment" to "C-8"),
            "D9" to ("walking_d_fragment" to "D-1"),
            "D1" to ("wring_towel_fragment" to "D-2"),
            "D2" to ("bottle_lift_fragment" to "D-3"),
            "D3" to ("chair_stand_fragment" to "D-4"),
            "D4" to ("stair_climbing_fragment" to "D-5"),
            "D5" to ("balloon_walking_fragment" to "D-6"),
            "D6" to ("figure8_walking_fragment" to "D-7"),
            "D7" to ("stretch_fragment" to "D-8"),
            "D8" to ("leg_stretch_fragment" to "D-9")
        )

        expectedRoutes.forEach { (exerciseId, expected) ->
            val route = resolveCameraExerciseRoute(exerciseId, week = 1)
            assertEquals(expected.first, route?.targetFragment)
            assertEquals(expected.second, route?.resultCode)
        }
        assertNull(resolveCameraExerciseRoute("unknown", week = 1))
    }

    @Test
    fun updateParams_usesExplicitWeekAndRegeneratesAdvancedPlan() = runBlocking {
        val viewModel = testViewModel()
        viewModel.updateParams(level = "B", day = 1, week = 6)
        assertEquals("2-5分鐘/組", viewModel.loadedState().exercises.first { it.id == "B7" }.repsOrTime)

        viewModel.updateParams(level = "B", day = 1, week = 7)
        assertEquals("8分鐘/組", viewModel.loadedState().exercises.first { it.id == "B7" }.repsOrTime)
    }

    @Test
    fun missingOrInvalidWeek_doesNotFallbackToWeekOne() = runBlocking {
        val viewModel = testViewModel()

        viewModel.updateParams(level = "B", day = 1, week = null)
        assertTrue(viewModel.uiState.value.isWeekLoading)
        assertNull(viewModel.uiState.value.dailyPlan)

        viewModel.updateParams(level = "B", day = 1, week = 0)
        assertFalse(viewModel.uiState.value.isWeekLoading)
        assertNotNull(viewModel.uiState.value.weekError)
        assertNull(viewModel.uiState.value.dailyPlan)

        viewModel.updateParams(level = "B", day = 1, week = 13)
        assertNotNull(viewModel.uiState.value.weekError)
        assertNull(viewModel.uiState.value.dailyPlan)
    }

    @Test
    fun trainingAttempt_capturesValidatedWeek() = runBlocking {
        val viewModel = readyViewModel(level = "D", week = 12)

        assertEquals(12, requireNotNull(viewModel.beginTraining("D9")).week)
    }

    @Test
    fun walkingRoutes_useAdvancedFragmentsOnlyFromWeekSeven() {
        assertEquals("walking_b_fragment", resolveCameraExerciseRoute("B7", 6)?.targetFragment)
        assertEquals("walking_c_fragment", resolveCameraExerciseRoute("C8", 1)?.targetFragment)
        assertEquals("walking_d_fragment", resolveCameraExerciseRoute("D9", 6)?.targetFragment)

        assertEquals("walking_b_week7_fragment", resolveCameraExerciseRoute("B7", 7)?.targetFragment)
        assertEquals("walking_c_week7_fragment", resolveCameraExerciseRoute("C8", 12)?.targetFragment)
        assertEquals("walking_d_week7_fragment", resolveCameraExerciseRoute("D9", 7)?.targetFragment)
        assertNull(resolveCameraExerciseRoute("B7", 0))
    }

    private suspend fun readyViewModel(
        level: String = "A",
        week: Int = 1
    ): AssignmentViewModel {
        return testViewModel().also { viewModel ->
            viewModel.updateParams(level = level, day = 1, week = week)
            markRecordsReady(viewModel)
            viewModel.loadedState()
        }
    }

    private fun testViewModel(): AssignmentViewModel {
        TestApplicationState.accountId = -1
        val application = allocateWithoutConstructor(TestApplication::class.java)
        return AssignmentViewModel(application)
    }

    private fun markRecordsReady(viewModel: AssignmentViewModel) {
        TestApplicationState.accountId = TEST_ACCOUNT_ID
        setField(viewModel, "accountId", TEST_ACCOUNT_ID)
        setField(viewModel, "recordsReady", true)
        setField(viewModel, "recordsLoading", false)
        viewModel.javaClass.getDeclaredMethod("refreshUiState").apply {
            isAccessible = true
        }.invoke(viewModel)
    }

    private fun restoreServerRecords(
        viewModel: AssignmentViewModel,
        vararg exerciseCodes: String
    ) {
        setField(viewModel, "exerciseCounts", exerciseCodes.groupingBy { it }.eachCount())
        markRecordsReady(viewModel)
    }

    private fun applySuccessfulTrainingResult(
        viewModel: AssignmentViewModel,
        attemptId: Long,
        exerciseId: String
    ): Boolean {
        TestApplicationState.accountId = -1
        setField(viewModel, "accountId", -1)
        return try {
            viewModel.applyTrainingResult(attemptId, exerciseId, completed = true)
        } finally {
            TestApplicationState.accountId = TEST_ACCOUNT_ID
            setField(viewModel, "accountId", TEST_ACCOUNT_ID)
        }
    }

    private fun setField(target: Any, name: String, value: Any) {
        target.javaClass.getDeclaredField(name).apply {
            isAccessible = true
        }.set(target, value)
    }

    private suspend fun AssignmentViewModel.loadedState(): AssignmentUiState =
        uiState.first { it.dailyPlan != null && it.exercises.isNotEmpty() }

    private class TestApplication : Application() {
        override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences =
            TestApplicationState.preferences

        override fun getApplicationInfo(): ApplicationInfo =
            allocateWithoutConstructor(ApplicationInfo::class.java).apply {
                flags = ApplicationInfo.FLAG_DEBUGGABLE
            }
    }

    private object TestApplicationState {
        var accountId: Int = -1

        val preferences: SharedPreferences = Proxy.newProxyInstance(
            SharedPreferences::class.java.classLoader,
            arrayOf(SharedPreferences::class.java)
        ) { _, method, arguments ->
            when (method.name) {
                "getInt" -> if (arguments?.firstOrNull() == "ACCOUNT_ID") {
                    accountId
                } else {
                    arguments?.getOrNull(1) as? Int ?: -1
                }
                "contains" -> arguments?.firstOrNull() == "ACCOUNT_ID" && accountId > 0
                "getAll" -> if (accountId > 0) mapOf("ACCOUNT_ID" to accountId) else emptyMap<String, Any>()
                "toString" -> "TestSharedPreferences"
                "hashCode" -> System.identityHashCode(this)
                "equals" -> false
                else -> defaultValue(method.returnType)
            }
        } as SharedPreferences

        private fun defaultValue(type: Class<*>): Any? = when (type) {
            java.lang.Boolean.TYPE -> false
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Float.TYPE -> 0f
            else -> null
        }
    }

    private companion object {
        const val TEST_ACCOUNT_ID = 2

        @Suppress("UNCHECKED_CAST")
        fun <T> allocateWithoutConstructor(type: Class<T>): T {
            val unsafeType = Class.forName("sun.misc.Unsafe")
            val unsafe = unsafeType.getDeclaredField("theUnsafe").let { field ->
                field.isAccessible = true
                field.get(null)
            }
            return unsafeType.getMethod("allocateInstance", Class::class.java)
                .invoke(unsafe, type) as T
        }
    }
}

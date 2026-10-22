package com.example.graduationproject.ui.screens

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.graduationproject.DataClass.DailyPlan
import com.example.graduationproject.DataClass.ExerciseStatus
import com.example.graduationproject.DataClass.GetExerciseRecordsRequest
import com.example.graduationproject.api.ApiClient
import com.example.graduationproject.repository.VivifrailRepository
import com.example.graduationproject.resolveCameraExerciseRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class AssignmentUiState(
    val dailyPlan: DailyPlan? = null,
    val exercises: List<com.example.graduationproject.DataClass.Exercise> = emptyList(),
    val completedExerciseIds: Set<String> = emptySet(),
    val isWeekLoading: Boolean = true,
    val weekError: String? = null,
    val recordsLoading: Boolean = false,
    val recordsError: String? = null
)

internal data class TrainingAttempt(
    val id: Long,
    val exerciseId: String,
    val week: Int
)

class AssignmentViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "AssignmentViewModel"
        private const val PREFS_NAME = "ElderCarePrefs"
        private const val ACCOUNT_KEY = "ACCOUNT_ID"
    }

    private val repository = VivifrailRepository()
    private val preferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val taiwanTimeZone = TimeZone.getTimeZone("Asia/Taipei")
    private val debugMode = (application.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private var accountId = -1
    private var level: String? = null
    private var day = 1
    private var week: Int? = null
    private var recordDate = todayString()
    private var exerciseCounts: Map<String, Int> = emptyMap()
    private var completedIds: Set<String> = emptySet()
    private var recordsReady = false
    private var recordsLoading = false
    private var recordsError: String? = null
    private var requestGeneration = 0
    private var nextTrainingAttemptId = 1L
    private var activeTrainingAttempt: TrainingAttempt? = null

    private val _uiState = MutableStateFlow(AssignmentUiState())
    val uiState: StateFlow<AssignmentUiState> = _uiState.asStateFlow()

    private fun getLoggedInAccountId(): Int {
        return preferences.getInt(ACCOUNT_KEY, -1)
    }

    private fun todayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = taiwanTimeZone
        }.format(Date())
    }

    private fun actualTrainingDay(): Int {
        val calendar = Calendar.getInstance(taiwanTimeZone)
        return (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1
    }

    private fun normalizeCode(code: String): String {
        return code.trim().uppercase(Locale.ROOT)
    }

    private fun getScheduledPlan(): DailyPlan? {
        val currentLevel = level ?: return null
        val currentWeek = week?.takeIf { it in 1..12 } ?: return null
        if (day !in 1..7) return null
        return repository.getDailyPlan(currentLevel, day, currentWeek)
    }

    private fun getDisplayPlan(): DailyPlan? {
        val currentLevel = level ?: return null
        val currentWeek = week?.takeIf { it in 1..12 } ?: return null
        return repository.getDailyPlan(currentLevel, 1, currentWeek).copy(dayNumber = day)
    }

    private fun getRunnablePlan(): DailyPlan? {
        return if (debugMode) getDisplayPlan() else getScheduledPlan()
    }

    private fun getRunnableExerciseIds(): Set<String> {
        return getRunnablePlan()?.exercises?.map { it.id }?.toSet() ?: emptySet()
    }

    private fun getExerciseCode(exerciseId: String): String? {
        val currentWeek = week ?: return null
        return resolveCameraExerciseRoute(exerciseId, currentWeek)
            ?.resultCode
            ?.let(::normalizeCode)
    }

    private fun calculateCompletedIds(): Set<String> {
        return getRunnablePlan()?.exercises
            ?.filter { exercise ->
                val code = getExerciseCode(exercise.id) ?: return@filter false
                (exerciseCounts[code] ?: 0) > 0
            }
            ?.map { exercise -> exercise.id }
            ?.toSet()
            ?: emptySet()
    }

    private fun currentExerciseId(): String? {
        if (!recordsReady) return null
        return getRunnablePlan()?.exercises?.firstOrNull { exercise ->
            exercise.id !in completedIds && getExerciseCode(exercise.id) != null
        }?.id
    }

    private fun refreshUiState() {
        val currentWeek = week
        val weekError = when {
            currentWeek == null -> null
            currentWeek !in 1..12 -> "訓練週次資料異常（$currentWeek）"
            else -> null
        }

        if (recordsReady) {
            completedIds = calculateCompletedIds()
        } else {
            completedIds = emptySet()
        }

        val displayPlan = getDisplayPlan()
        val runnableIds = getRunnableExerciseIds()
        val nextExerciseId = currentExerciseId()

        val processedExercises = displayPlan?.exercises?.map { exercise ->
            val status = when {
                !recordsReady -> ExerciseStatus.LOCKED
                exercise.id !in runnableIds -> ExerciseStatus.LOCKED
                exercise.id in completedIds -> ExerciseStatus.COMPLETED
                exercise.id == nextExerciseId -> ExerciseStatus.CURRENT
                else -> ExerciseStatus.LOCKED
            }
            exercise.copy(status = status)
        } ?: emptyList()

        _uiState.value = AssignmentUiState(
            dailyPlan = displayPlan,
            exercises = processedExercises,
            completedExerciseIds = completedIds,
            isWeekLoading = currentWeek == null,
            weekError = weekError,
            recordsLoading = recordsLoading,
            recordsError = recordsError
        )

        Log.d(
            TAG,
            "account=$accountId, day=$day, week=$week, " +
                    "counts=$exerciseCounts, completed=$completedIds, next=$nextExerciseId, " +
                    "loading=$recordsLoading, error=$recordsError"
        )
    }

    fun updateParams(level: String, day: Int, week: Int?) {
        val currentAccount = getLoggedInAccountId()
        val currentDay = actualTrainingDay()
        val currentDate = todayString()

        val changed =
            accountId != currentAccount ||
                    this.level != level ||
                    this.day != currentDay ||
                    this.week != week ||
                    recordDate != currentDate

        if (changed) {
            requestGeneration++
            exerciseCounts = emptyMap()
            completedIds = emptySet()
            recordsReady = false
            recordsLoading = false
            recordsError = null
            activeTrainingAttempt = null
        }

        accountId = currentAccount
        this.level = level
        this.day = currentDay
        this.week = week
        recordDate = currentDate

        refreshUiState()

        if (changed && accountId > 0 && week in 1..12) {
            reloadCompletedRecords()
        }
    }

    fun reloadCompletedRecords() {
        if (accountId <= 0 || week !in 1..12 || getRunnablePlan() == null) return

        val requestedAccount = accountId
        val requestedDate = recordDate
        val requestNumber = ++requestGeneration

        recordsLoading = true
        recordsError = null
        refreshUiState()

        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getExerciseRecords(
                    GetExerciseRecordsRequest(
                        accountId = requestedAccount,
                        trainingDate = requestedDate
                    )
                )

                if (
                    requestNumber != requestGeneration ||
                    requestedAccount != accountId ||
                    requestedDate != recordDate
                ) return@launch

                val body = response.body()

                if (response.isSuccessful && body?.success == true) {
                    val databaseCounts = body.records
                        .groupingBy { normalizeCode(it.exerciseCode) }
                        .eachCount()

                    exerciseCounts = (exerciseCounts.keys + databaseCounts.keys)
                        .associateWith { code ->
                            maxOf(
                                exerciseCounts[code] ?: 0,
                                databaseCounts[code] ?: 0
                            )
                        }

                    recordsReady = true
                    recordsError = null

                    Log.d(
                        TAG,
                        "訓練紀錄同步成功: date=$requestedDate, counts=$exerciseCounts"
                    )
                } else {
                    recordsError = body?.message ?: "讀取訓練紀錄失敗"
                    Log.e(TAG, recordsError.orEmpty())
                }
            } catch (e: Exception) {
                if (requestNumber == requestGeneration) {
                    recordsError = e.message ?: "無法讀取訓練紀錄"
                    Log.e(TAG, "讀取訓練紀錄失敗", e)
                }
            } finally {
                if (requestNumber == requestGeneration) {
                    recordsLoading = false
                    refreshUiState()
                }
            }
        }
    }

    internal fun beginTraining(exerciseId: String): TrainingAttempt? {
        if (activeTrainingAttempt != null) return null
        if (accountId <= 0 || accountId != getLoggedInAccountId()) return null
        if (!recordsReady || recordsLoading) return null
        if (recordDate != todayString()) return null
        if (currentExerciseId() != exerciseId) return null

        val currentWeek = week?.takeIf { it in 1..12 } ?: return null
        val resultCode = getExerciseCode(exerciseId) ?: return null
        val currentLevel = level?.trim()?.uppercase(Locale.ROOT)?.take(1) ?: return null

        if (!resultCode.startsWith("$currentLevel-")) return null

        return TrainingAttempt(
            id = nextTrainingAttemptId++,
            exerciseId = exerciseId,
            week = currentWeek
        ).also {
            activeTrainingAttempt = it
        }
    }

    internal fun applyTrainingResult(
        attemptId: Long,
        exerciseId: String,
        completed: Boolean
    ): Boolean {
        val attempt = activeTrainingAttempt ?: return false

        if (
            attempt.id != attemptId ||
            attempt.exerciseId != exerciseId ||
            attempt.week != week
        ) return false

        activeTrainingAttempt = null

        if (!completed) return false
        if (accountId != getLoggedInAccountId()) return false
        if (!recordsReady) return false

        if (recordDate != todayString()) {
            updateParams(level ?: return false, actualTrainingDay(), week)
            return false
        }

        if (currentExerciseId() != exerciseId) return false

        val resultCode = getExerciseCode(exerciseId) ?: return false

        exerciseCounts = exerciseCounts + (
                resultCode to ((exerciseCounts[resultCode] ?: 0) + 1)
                )

        refreshUiState()

        Log.d(
            TAG,
            "完成任務 $exerciseId，下一項=${currentExerciseId()}"
        )

        reloadCompletedRecords()
        return true
    }

    internal fun cancelActiveTraining() {
        activeTrainingAttempt = null
    }

    fun resetForAccountChange() {
        requestGeneration++
        accountId = -1
        level = null
        day = 1
        week = null
        recordDate = todayString()
        exerciseCounts = emptyMap()
        completedIds = emptySet()
        recordsReady = false
        recordsLoading = false
        recordsError = null
        activeTrainingAttempt = null
        nextTrainingAttemptId = 1L
        _uiState.value = AssignmentUiState()
    }
}

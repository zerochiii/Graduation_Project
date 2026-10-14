/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.poselandmarker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 *  This ViewModel is used to store pose landmarker helper settings
 */
data class ExerciseResult(
    val exerciseName: String,     // 運動名稱 (例如: "水瓶舉重")
    val exerciseId: String = "",  // 運動 ID
    val reps: Int = 0,            // 總次數
    val sets: Int = 0,            // 總組數
    val steps: Int = 0,           // 總步數 (步行類運動用)
    val accuracy: Float = 0f,     // 平均準確率
    val durationSeconds: Int = 0, // 總運動時長 (秒)
    val timestamp: Long = System.currentTimeMillis()
)

internal data class PendingTrainingSave(
    val requestId: String,
    val accountId: Int,
    val exerciseCode: String,
    val result: ExerciseResult
)

internal data class TrainingSaveResponse(
    val success: Boolean? = null,
    val message: String? = null,
    @SerializedName("request_id") val requestId: String? = null,
    @SerializedName("idempotent_replay") val idempotentReplay: Boolean? = null,
    @SerializedName("base_points") val basePoints: Int? = null,
    @SerializedName("streak_days") val streakDays: Int? = null,
    @SerializedName("streak_bonus_percent") val streakBonusPercent: Int? = null,
    @SerializedName("exp_gained") val expGained: Int? = null,
    @SerializedName("points_earned") val pointsEarned: Int? = null,
    @SerializedName("total_exp") val totalExp: Int? = null,
    @SerializedName("current_points") val currentPoints: Int? = null,
    val level: Int? = null,
    @SerializedName("leveled_up") val leveledUp: Boolean? = null
)
enum class ExerciseMode {
    LEG_LIFT,      // 抬腿 (使用 Pose)
    SQUEEZE_BALL,  // 捏球 (使用 Hand + Object)
    IDLE
}

enum class SaveStatus {
    IDLE,
    SAVING,
    SUCCESS,
    FAILED
}

internal fun resolveExpectedExerciseCode(
    resultCodes: String,
    level: String,
    expectedCode: String?
): String? {
    val baseLevel = level.trim().uppercase().take(1)
    if (baseLevel.isBlank() || resultCodes.isBlank()) return null
    val codes = resultCodes.split(",").map(String::trim)
    return if (expectedCode != null) {
        expectedCode.takeIf { it.startsWith("$baseLevel-") && it in codes }
    } else {
        codes.firstOrNull { it.startsWith("$baseLevel-") }
    }
}

internal class SingleTrainingResultGuard {
    private var submitted = false

    @Synchronized
    fun trySubmit(): Boolean {
        if (submitted) return false
        submitted = true
        return true
    }
}

internal fun getOrCreatePendingTrainingSave(
    current: PendingTrainingSave?,
    accountId: Int,
    exerciseCode: String,
    result: ExerciseResult,
    requestIdFactory: () -> String = { UUID.randomUUID().toString() }
): PendingTrainingSave = current ?: PendingTrainingSave(
    requestId = requestIdFactory(),
    accountId = accountId,
    exerciseCode = exerciseCode,
    result = result
)

internal fun buildTrainingSaveRequestBody(pending: PendingTrainingSave): JsonObject =
    JsonObject().apply {
        addProperty("request_id", pending.requestId)
        addProperty("account_id", pending.accountId)
        addProperty("exercise_code", pending.exerciseCode)
        addProperty("accuracy", pending.result.accuracy)
        addProperty("duration_seconds", pending.result.durationSeconds)
        addProperty("exp_gained", pending.result.accuracy.toInt())
    }

internal fun parseVerifiedTrainingSaveResponse(
    responseCode: Int,
    responseBody: String?,
    expectedRequestId: String
): TrainingSaveResponse? {
    if (responseCode !in 200..299 || responseBody.isNullOrBlank()) return null

    return runCatching {
        val responseJson = JsonParser.parseString(responseBody)
        if (!responseJson.isJsonObject) return@runCatching null
        val responseObject = responseJson.asJsonObject
        val success = responseObject.get("success")
        val requestId = responseObject.get("request_id")
        if (success?.isJsonPrimitive != true || !success.asJsonPrimitive.isBoolean) {
            return@runCatching null
        }
        if (requestId?.isJsonPrimitive != true || !requestId.asJsonPrimitive.isString) {
            return@runCatching null
        }
        Gson().fromJson(responseObject, TrainingSaveResponse::class.java)
    }.getOrNull()?.takeIf { response ->
        response.success == true &&
            response.requestId != null &&
            response.requestId == expectedRequestId
    }
}

internal fun isSuccessfulSaveResponse(
    responseCode: Int,
    responseBody: String?,
    expectedRequestId: String
): Boolean = parseVerifiedTrainingSaveResponse(
    responseCode,
    responseBody,
    expectedRequestId
) != null

internal enum class TrainingSaveAttemptOutcome {
    SUCCESS,
    RETRYABLE_FAILURE,
    PERMANENT_FAILURE
}

internal const val TRAINING_SAVE_MAX_ATTEMPTS = 3

internal fun classifyTrainingSaveResponse(
    responseCode: Int,
    responseBody: String?,
    expectedRequestId: String
): TrainingSaveAttemptOutcome = when {
    responseCode in setOf(500, 502, 503, 504) ->
        TrainingSaveAttemptOutcome.RETRYABLE_FAILURE

    responseCode !in 200..299 ->
        TrainingSaveAttemptOutcome.PERMANENT_FAILURE

    isSuccessfulSaveResponse(responseCode, responseBody, expectedRequestId) ->
        TrainingSaveAttemptOutcome.SUCCESS

    else -> TrainingSaveAttemptOutcome.PERMANENT_FAILURE
}

internal fun retryDelayMillis(completedAttemptNumber: Int): Long = when (completedAttemptNumber) {
    1 -> 1_000L
    2 -> 2_000L
    else -> 0L
}

internal suspend fun executeTrainingSaveWithRetry(
    pending: PendingTrainingSave,
    maxAttempts: Int = TRAINING_SAVE_MAX_ATTEMPTS,
    delayStrategy: suspend (Long) -> Unit = { delay(it) },
    attempt: suspend (PendingTrainingSave) -> TrainingSaveAttemptOutcome
): Boolean {
    require(maxAttempts > 0) { "maxAttempts 必須大於 0" }

    for (attemptNumber in 1..maxAttempts) {
        val outcome = try {
            attempt(pending)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            TrainingSaveAttemptOutcome.RETRYABLE_FAILURE
        } catch (e: Exception) {
            TrainingSaveAttemptOutcome.PERMANENT_FAILURE
        }

        when (outcome) {
            TrainingSaveAttemptOutcome.SUCCESS -> return true
            TrainingSaveAttemptOutcome.PERMANENT_FAILURE -> return false
            TrainingSaveAttemptOutcome.RETRYABLE_FAILURE -> {
                if (attemptNumber == maxAttempts) return false
                delayStrategy(retryDelayMillis(attemptNumber))
            }
        }
    }
    return false
}

internal fun pendingAfterSave(
    pending: PendingTrainingSave,
    saveSucceeded: Boolean
): PendingTrainingSave? = pending.takeUnless { saveSucceeded }

internal fun resultToPublishAfterSave(
    result: ExerciseResult,
    saveSucceeded: Boolean
): ExerciseResult? = result.takeIf { saveSucceeded }

class MainViewModel : ViewModel() {

    companion object {
        private const val TAG = "ExerciseData"

        private const val BASE_URL = "http://192.168.0.10/xampp/Graduation_Project/"
    }

    private var _model = PoseLandmarkerHelper.MODEL_POSE_LANDMARKER_FULL
    private var _delegate: Int = PoseLandmarkerHelper.DELEGATE_CPU
    private var _minPoseDetectionConfidence: Float =
        PoseLandmarkerHelper.DEFAULT_POSE_DETECTION_CONFIDENCE
    private var _minPoseTrackingConfidence: Float = PoseLandmarkerHelper
        .DEFAULT_POSE_TRACKING_CONFIDENCE
    private var _minPosePresenceConfidence: Float = PoseLandmarkerHelper
        .DEFAULT_POSE_PRESENCE_CONFIDENCE

    val currentDelegate: Int get() = _delegate
    val currentModel: Int get() = _model
    val currentMinPoseDetectionConfidence: Float
        get() =
            _minPoseDetectionConfidence
    val currentMinPoseTrackingConfidence: Float
        get() =
            _minPoseTrackingConfidence
    val currentMinPosePresenceConfidence: Float
        get() =
            _minPosePresenceConfidence

    private var accountId: Int = -1
    private var userLevel: String = ""
    private var expectedExerciseCode: String? = null
    private val submissionGuard = SingleTrainingResultGuard()
    private var pendingTrainingSave: PendingTrainingSave? = null

    fun setAccountInfo(accountId: Int, userLevel: String, expectedCode: String?) {
        this.accountId = accountId
        this.userLevel = userLevel.trim().uppercase().take(1)
        this.expectedExerciseCode = expectedCode
    }

    private val _lastResult = MutableLiveData<ExerciseResult>()
    val lastResult: LiveData<ExerciseResult> get() = _lastResult

    private val _saveStatus = MutableLiveData<SaveStatus>()
    val saveStatus: LiveData<SaveStatus> get() = _saveStatus

    fun postResult(result: ExerciseResult) {
        val exerciseCode = resolveExpectedExerciseCode(
            result.exerciseId,
            userLevel,
            expectedExerciseCode
        )
        if (accountId <= 0 || exerciseCode == null) {
            android.util.Log.w(
                TAG,
                "缺少 accountId 或無法解析 exerciseCode（exerciseId=${result.exerciseId}, userLevel=$userLevel），這次訓練成績不會被存檔"
            )
            _saveStatus.postValue(SaveStatus.FAILED)
            return
        }
        if (!submissionGuard.trySubmit()) return

        val pending = getOrCreatePendingTrainingSave(
            current = pendingTrainingSave,
            accountId = accountId,
            exerciseCode = exerciseCode,
            result = result
        )
        pendingTrainingSave = pending

        _saveStatus.postValue(SaveStatus.SAVING)
        viewModelScope.launch {
            val success = executeTrainingSaveWithRetry(
                pending = pending,
                attempt = ::sendPendingTrainingSave
            )
            pendingTrainingSave = pendingAfterSave(pending, success)

            android.util.Log.d(
                TAG,
                "訓練成績存檔${if (success) "成功" else "失敗"}: ${pending.result} " +
                    "(exercise_code=${pending.exerciseCode})"
            )
            resultToPublishAfterSave(pending.result, success)?.let(_lastResult::postValue)
            _saveStatus.postValue(if (success) SaveStatus.SUCCESS else SaveStatus.FAILED)
        }
    }

    private suspend fun sendPendingTrainingSave(
        pending: PendingTrainingSave
    ): TrainingSaveAttemptOutcome = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(BASE_URL + "api/save_exercise_record.php")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connectTimeout = 8000
                readTimeout = 8000
            }

            val body = buildTrainingSaveRequestBody(pending)
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseBody = (if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            })?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }

            classifyTrainingSaveResponse(
                responseCode,
                responseBody,
                pending.requestId
            )
        } finally {
            connection?.disconnect()
        }
    }


    fun setDelegate(delegate: Int) {
        _delegate = delegate
    }

    fun setMinPoseDetectionConfidence(confidence: Float) {
        _minPoseDetectionConfidence = confidence
    }

    fun setMinPoseTrackingConfidence(confidence: Float) {
        _minPoseTrackingConfidence = confidence
    }

    fun setMinPosePresenceConfidence(confidence: Float) {
        _minPosePresenceConfidence = confidence
    }

    fun setModel(model: Int) {
        _model = model
    }

    private var _currentExerciseMode = ExerciseMode.IDLE
    val currentExerciseMode: ExerciseMode get() = _currentExerciseMode

    fun setExerciseMode(mode: ExerciseMode) {
        _currentExerciseMode = mode
    }
}

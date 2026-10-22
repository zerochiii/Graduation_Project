package com.google.mediapipe.examples.poselandmarker

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.UUID

data class ExerciseResult(
    val exerciseName: String,
    val exerciseId: String = "",
    val reps: Int = 0,
    val sets: Int = 0,
    val steps: Int = 0,
    val accuracy: Float = 0f,
    val durationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
enum class ExerciseMode {
    LEG_LIFT,
    SQUEEZE_BALL,
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
    val baseLevel = level.trim().uppercase(Locale.ROOT).take(1)
    if (baseLevel !in listOf("A", "B", "C", "D")) return null
    val codes = resultCodes.split(",")
        .map { it.trim().uppercase(Locale.ROOT) }
        .filter { it.isNotEmpty() }
    val expected = expectedCode?.trim()?.uppercase(Locale.ROOT)
    return if (expected != null) {
        expected.takeIf { it.startsWith("$baseLevel-") && it in codes }
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
    @Synchronized
    fun reset() {
        submitted = false
    }
}

internal fun isSuccessfulSaveResponse(
    responseCode: Int,
    responseBody: String?,
    expectedRequestId: String? = null
): Boolean {
    if (responseCode !in 200..299 || responseBody.isNullOrBlank()) return false
    return runCatching {
        val json = JSONObject(responseBody)
        json.opt("success") == true &&
                (expectedRequestId == null || json.optString("request_id") == expectedRequestId)
    }.getOrDefault(false)
}

internal fun resultToPublishAfterSave(
    result: ExerciseResult,
    saveSucceeded: Boolean
): ExerciseResult? = result.takeIf { saveSucceeded }

internal fun trainingSaveUrl(apiBaseUrl: String): URL =
    URL(apiBaseUrl.trim().trimEnd('/') + "/api/save_exercise_record.php")

class MainViewModel : ViewModel() {
    companion object {
        private const val TAG = "ExerciseData"
    }

    private var _model = PoseLandmarkerHelper.MODEL_POSE_LANDMARKER_FULL
    private var _delegate = PoseLandmarkerHelper.DELEGATE_CPU
    private var _minPoseDetectionConfidence =
        PoseLandmarkerHelper.DEFAULT_POSE_DETECTION_CONFIDENCE
    private var _minPoseTrackingConfidence =
        PoseLandmarkerHelper.DEFAULT_POSE_TRACKING_CONFIDENCE
    private var _minPosePresenceConfidence =
        PoseLandmarkerHelper.DEFAULT_POSE_PRESENCE_CONFIDENCE

    val currentDelegate: Int get() = _delegate
    val currentModel: Int get() = _model
    val currentMinPoseDetectionConfidence: Float get() = _minPoseDetectionConfidence
    val currentMinPoseTrackingConfidence: Float get() = _minPoseTrackingConfidence
    val currentMinPosePresenceConfidence: Float get() = _minPosePresenceConfidence

    private var accountId = -1
    private var userLevel = ""
    private var expectedExerciseCode: String? = null
    private var apiBaseUrl = ""
    private val submissionGuard = SingleTrainingResultGuard()
    private var pendingResult: ExerciseResult? = null
    private var pendingRequestId: String? = null
    private val _lastResult = MutableLiveData<ExerciseResult>()
    val lastResult: LiveData<ExerciseResult> get() = _lastResult

    private val _saveStatus = MutableLiveData(SaveStatus.IDLE)
    val saveStatus: LiveData<SaveStatus> get() = _saveStatus

    fun setAccountInfo(
        accountId: Int,
        userLevel: String,
        expectedCode: String?,
        apiBaseUrl: String
    ) {
        this.accountId = accountId
        this.userLevel = userLevel.trim().uppercase(Locale.ROOT).take(1)
        this.expectedExerciseCode = expectedCode
        this.apiBaseUrl = apiBaseUrl
        pendingResult = null
        pendingRequestId = null
        submissionGuard.reset()
        _saveStatus.value = SaveStatus.IDLE
    }

    fun postResult(result: ExerciseResult) {
        val resultToSave = pendingResult ?: result
        val exerciseCode = resolveExpectedExerciseCode(
            resultToSave.exerciseId,
            userLevel,
            expectedExerciseCode
        )
        if (accountId <= 0 || exerciseCode == null || apiBaseUrl.isBlank()) {
            Log.e(
                TAG,
                "訓練資料不正確: accountId=$accountId, level=$userLevel, exerciseId=${resultToSave.exerciseId}"
            )
            _saveStatus.postValue(SaveStatus.FAILED)
            return
        }
        if (!submissionGuard.trySubmit()) return
        pendingResult = resultToSave
        val requestId = pendingRequestId ?: UUID.randomUUID().toString().also {
            pendingRequestId = it
        }
        _saveStatus.postValue(SaveStatus.SAVING)
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                var connection: HttpURLConnection? = null
                try {
                    connection = (trainingSaveUrl(apiBaseUrl)
                        .openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        doOutput = true
                        connectTimeout = 8000
                        readTimeout = 8000
                        setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    }
                    val body = JSONObject().apply {
                        put("account_id", accountId)
                        put("exercise_code", exerciseCode)
                        put("accuracy", resultToSave.accuracy)
                        put("duration_seconds", resultToSave.durationSeconds)
                        put("exp_gained", resultToSave.accuracy.toInt())
                        put("request_id", requestId)
                    }
                    OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use {
                        it.write(body.toString())
                        it.flush()
                    }
                    val responseCode = connection.responseCode
                    val responseBody = (if (responseCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    })?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    val saved = isSuccessfulSaveResponse(
                        responseCode,
                        responseBody,
                        requestId
                    )
                    if (!saved) {
                        Log.e(TAG, "API 未確認儲存成功: HTTP $responseCode, $responseBody")
                    } else {
                        Log.d(TAG, "訓練儲存成功: $exerciseCode, request_id=$requestId")
                    }
                    saved
                } catch (e: Exception) {
                    Log.e(TAG, "上傳訓練紀錄失敗", e)
                    false
                } finally {
                    connection?.disconnect()
                }
            }
            if (success) {
                _saveStatus.postValue(SaveStatus.SUCCESS)
                resultToPublishAfterSave(resultToSave, true)?.let(_lastResult::postValue)
            } else {
                submissionGuard.reset()
                _saveStatus.postValue(SaveStatus.FAILED)
            }
        }
    }

    fun retryPendingResult(): Boolean {
        val result = pendingResult ?: return false
        if (saveStatus.value == SaveStatus.SAVING) return false
        postResult(result)
        return true
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

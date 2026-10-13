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
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

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

internal fun isSuccessfulSaveResponse(responseCode: Int, responseBody: String?): Boolean {
    if (responseCode !in 200..299 || responseBody.isNullOrBlank()) return false

    return runCatching {
        val responseJson = JsonParser.parseString(responseBody)
        if (!responseJson.isJsonObject) return@runCatching false
        val success = responseJson.asJsonObject.get("success")
        success?.isJsonPrimitive == true &&
            success.asJsonPrimitive.isBoolean &&
            success.asBoolean
    }.getOrDefault(false)
}

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

        _saveStatus.postValue(SaveStatus.SAVING)
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
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

                    val body = JSONObject().apply {
                        put("account_id", accountId)
                        put("exercise_code", exerciseCode)
                        put("accuracy", result.accuracy)
                        put("duration_seconds", result.durationSeconds)
                        put("exp_gained", result.accuracy.toInt())
                    }

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

                    isSuccessfulSaveResponse(responseCode, responseBody)
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "上傳訓練紀錄失敗", e)
                    false
                } finally {
                    connection?.disconnect()
                }
            }

            android.util.Log.d(TAG, "訓練成績存檔${if (success) "成功" else "失敗"}: $result (exercise_code=$exerciseCode)")
            resultToPublishAfterSave(result, success)?.let(_lastResult::postValue)
            _saveStatus.postValue(if (success) SaveStatus.SUCCESS else SaveStatus.FAILED)
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

package com.example.graduationproject.DataClass
import androidx.compose.ui.graphics.Color
import com.example.graduationproject.ui.screens.PointRecord
import com.google.gson.annotations.SerializedName

data class CommunityUser(
    val id: String,
    val name: String,
    val level: String,
    val avatarColor: Color,
    val weeklyExercise: Int,
    val weeklyExp: Int,
    val rank: Int,
    val isFriend: Boolean = false,
    val hasPendingRequestSent: Boolean = false
)

data class FriendRequest(
    val id: String,
    val senderName: String,
    val senderLevel: String,
    val senderAvatarColor: Color,
    val requestTime: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: UserData?
)
data class UserData(
    val account_id: Int,
    val role: String?,
    val name: String?
)
data class RegisterElderRequest(
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val password: String
)

data class RedeemRequest(
    val account_id: Int,
    val reward_id: Int
)

data class RedeemResponse(
    val success: Boolean,
    val message: String,
    val remaining_points: Int
)

data class GetPointsRequest(
    @SerializedName("account_id") val accountId: Int
)

data class GetPointsResponse(
    val success: Boolean,
    val message: String?,
    val points: Int
)

data class ElderDashboardResponse(
    val success: Boolean,
    val message: String?,
    val name: String,
    val level: Int,
    val grade: String,
    val points: Int,
    @SerializedName("streak_days") val streakDays: Int,
    @SerializedName("current_week") val currentWeek: Int
)

data class GetPointHistoryRequest(val account_id: Int)

data class GetPointHistoryResponse(
    val success: Boolean,
    val message: String?,
    val records: List<PointRecord>?
)

data class SendOtpRequest(
    val email: String
)

data class SendOtpResponse(
    val success: Boolean,
    val message: String
)

data class SaveAssessmentRequest(
    val account_id: Int,
    val sppb_score: Int,
    val grade: String,
    val has_fall_risk: Boolean
)

data class SaveAssessmentResponse(
    val success: Boolean,
    val message: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp_code: String
)

data class VerifyOtpResponse(
    val success: Boolean,
    val message: String
)

data class BindFamilyRequest(
    val account_id: Int,
    val elder_id: Int,
    val name: String
)

data class CommonResponse(
    val success: Boolean,
    val message: String
)

// --- VIVIFRAIL Data Models ---

enum class ExerciseStatus {
    COMPLETED,
    CURRENT,
    LOCKED
}

data class Exercise(
    val id: String,
    val name: String,
    val intensity: String, // 輕鬆/中等/挑戰
    val sets: String,      // 組數
    val repsOrTime: String, // 每組次數/時間
    val restTime: String,   // 組間休息
    val status: ExerciseStatus = ExerciseStatus.LOCKED
)

data class DailyPlan(
    val dayNumber: Int,
    val level: String, // A/B/C/D
    val exercises: List<Exercise>
)

data class CommunityDataResponse(
    val success: Boolean,
    val message: String?,
    val leaderboard: List<CommunityUser>,
    val friends: List<CommunityUser>,
    @SerializedName("pendingRequests") val pendingRequests: List<FriendRequest>
)

data class FriendActionRequest(
    @SerializedName("account_id") val accountId: Int,
    val action: String,
    @SerializedName("target_id") val targetId: Int? = null,
    val phone: String? = null
)

data class ResetPasswordRequest(
    val email: String,
    @SerializedName("new_password") val newPassword: String
)

data class GetFitnessRadarRequest(val account_id: Int)

data class FitnessRadarResponse(
    val success: Boolean,
    val message: String?,
    val radar: Map<String, Float>? // key 對應 FitnessCategory.apiKey
)

data class SaveExerciseRecordRequest(
    val account_id: Int,
    val exercise_code: String,
    val accuracy: Float,       // 對應 Fragment 算出的 finalAccuracy
    val duration_seconds: Int? = null,
    val exp_gained: Int = 0
)
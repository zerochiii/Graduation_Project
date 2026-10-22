package com.example.graduationproject.DataClass
import androidx.compose.ui.graphics.Color
import com.example.graduationproject.ui.screens.PointRecord
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    val name: String?,
    @SerializedName("chat_token") val chatToken: String? = null
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
    @SerializedName("account_id")
    val accountId: Int
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

    @SerializedName("streak_days")
    val streakDays: Int,

    @SerializedName("current_week")
    private val apiCurrentWeek: Int = 1,

    @SerializedName("program_completed")
    val programCompleted: Boolean = false,

    @SerializedName("training_start_date")
    val trainingStartDate: String? = null,

    @SerializedName("sppb_score")
    val sppbScore: Int? = null
) {
    val currentWeek: Int
        get() {
            val startDate = trainingStartDate
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: return apiCurrentWeek

            return calculateCurrentTrainingWeek(startDate)
                ?: apiCurrentWeek
        }
}
private fun calculateCurrentTrainingWeek(
    startDate: String
): Int? {
    return try {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("Asia/Taipei")
        }

        val start = formatter.parse(startDate)
            ?: return null

        val todayText = formatter.format(Date())
        val today = formatter.parse(todayText)
            ?: return null

        val days = (
                (today.time - start.time) /
                        (24L * 60L * 60L * 1000L)
                ).coerceAtLeast(0L)

        (days / 7L + 1L)
            .coerceIn(1L, 12L)
            .toInt()

    } catch (e: Exception) {
        null
    }
}

data class GetPointHistoryRequest(
    val account_id: Int
)

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


enum class ExerciseStatus {
    COMPLETED,
    CURRENT,
    LOCKED
}

data class Exercise(
    val id: String,
    val name: String,
    val intensity: String,
    val sets: String,
    val repsOrTime: String,
    val restTime: String,
    val status: ExerciseStatus = ExerciseStatus.LOCKED
)

data class DailyPlan(
    val dayNumber: Int,
    val level: String,
    val exercises: List<Exercise>
)

data class CommunityDataResponse(
    val success: Boolean,
    val message: String?,
    val leaderboard: List<CommunityUser>,
    val friends: List<CommunityUser>,
    @SerializedName("pendingRequests")
    val pendingRequests: List<FriendRequest>
)

data class FriendActionRequest(
    @SerializedName("account_id")
    val accountId: Int,
    val action: String,
    @SerializedName("target_id")
    val targetId: Int? = null,
    val phone: String? = null
)

data class ResetPasswordRequest(
    val email: String,
    @SerializedName("new_password")
    val newPassword: String
)

data class GetFitnessRadarRequest(
    val account_id: Int
)

data class FitnessRadarResponse(
    val success: Boolean,
    val message: String?,
    val radar: Map<String, Float>?
)

data class SaveExerciseRecordRequest(
    val account_id: Int,
    val exercise_code: String,
    val accuracy: Float,
    val duration_seconds: Int? = null,
    val exp_gained: Int = 0
)

data class RewardCatalogItem(
    val id: Int,
    val name: String,
    val price: Int,
    val quantity: Int,
    val image: String?
)

data class GetRewardsResponse(
    val success: Boolean,
    val data: List<RewardCatalogItem>?
)

data class GetExerciseRecordsRequest(
    @SerializedName("account_id")
    val accountId: Int,

    @SerializedName("training_date")
    val trainingDate: String
)

data class SavedExerciseRecord(
    @SerializedName("exercise_code")
    val exerciseCode: String,

    val score: Int? = null,

    @SerializedName("completed_at")
    val completedAt: String? = null
)

data class GetExerciseRecordsResponse(
    val success: Boolean,
    val message: String? = null,
    val records: List<SavedExerciseRecord> = emptyList()
)

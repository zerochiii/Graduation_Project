package com.example.graduationproject.DataClass
import com.example.graduationproject.ui.screens.PointRecord
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val account_id: Int,
    val role: String?
)

data class RegisterElderRequest(
    val name: String,
    val username: String,
    val email: String,
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
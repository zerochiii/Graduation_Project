package com.example.graduationproject.DataClass
import com.google.gson.annotations.SerializedName

class LoginClass {

}
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
    val role: String
)
data class RegisterElderRequest(
    val name: String,
    val username: String,
    val password: String,
    val phone: String
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
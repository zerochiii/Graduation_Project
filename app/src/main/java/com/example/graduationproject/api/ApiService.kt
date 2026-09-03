package com.example.graduationproject.api

import com.example.graduationproject.DataClass.BindFamilyRequest
import com.example.graduationproject.DataClass.CommonResponse
import com.example.graduationproject.DataClass.CommunityDataResponse
import com.example.graduationproject.DataClass.ElderDashboardResponse
import com.example.graduationproject.DataClass.FitnessRadarResponse
import com.example.graduationproject.DataClass.FriendActionRequest
import com.example.graduationproject.DataClass.GetFitnessRadarRequest
import com.example.graduationproject.DataClass.GetPointHistoryRequest
import com.example.graduationproject.DataClass.GetPointHistoryResponse
import com.example.graduationproject.DataClass.GetPointsRequest
import com.example.graduationproject.DataClass.GetPointsResponse
import com.example.graduationproject.DataClass.LoginRequest
import com.example.graduationproject.DataClass.LoginResponse
import com.example.graduationproject.DataClass.RedeemRequest
import com.example.graduationproject.DataClass.RedeemResponse
import com.example.graduationproject.DataClass.RegisterElderRequest
import com.example.graduationproject.DataClass.ResetPasswordRequest
import com.example.graduationproject.DataClass.SaveAssessmentRequest
import com.example.graduationproject.DataClass.SaveAssessmentResponse
import com.example.graduationproject.DataClass.SaveExerciseRecordRequest
import com.example.graduationproject.DataClass.SendOtpRequest
import com.example.graduationproject.DataClass.SendOtpResponse
import com.example.graduationproject.DataClass.VerifyOtpRequest
import com.example.graduationproject.DataClass.VerifyOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/login.php")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/register.php")
    suspend fun registerElder(@Body request: RegisterElderRequest): Response<LoginResponse>

    @POST("api/redeem.php")
    suspend fun redeemReward(@Body request: RedeemRequest): Response<RedeemResponse>

    @POST("api/get_points.php")
    suspend fun getPoints(@Body request: GetPointsRequest): Response<GetPointsResponse>

    @POST("api/get_point_history.php")
    suspend fun getPointHistory(@Body request: GetPointHistoryRequest): Response<GetPointHistoryResponse>

    @POST("api/send_email_otp.php")
    suspend fun sendEmailOtp(
        @Body request: SendOtpRequest
    ): Response<SendOtpResponse>

    @POST("api/save_assessment.php")
    suspend fun saveAssessment(
        @Body request: SaveAssessmentRequest
    ): Response<SaveAssessmentResponse>

    @POST("api/verify_email_otp.php")
    suspend fun verifyEmailOtp(
        @Body request: VerifyOtpRequest
    ): retrofit2.Response<VerifyOtpResponse>

    @POST("bind_family.php")
    suspend fun bindFamily(
        @Body request: BindFamilyRequest
    ): Response<CommonResponse>

    @POST("api/get_elder_dashboard.php")
    suspend fun getElderDashboardData(@Body request: GetPointsRequest): retrofit2.Response<ElderDashboardResponse>

    @POST("api/get_community_data.php")
    suspend fun getCommunityData(@Body request: GetPointsRequest): Response<CommunityDataResponse>

    @POST("api/handle_friend_action.php")
    suspend fun handleFriendAction(@Body request: FriendActionRequest): Response<CommonResponse>

    @POST("api/send_forgot_otp.php")
    suspend fun sendForgotOtp(@Body request: SendOtpRequest): Response<CommonResponse>

    @POST("api/reset_password.php")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<CommonResponse>

    @POST("api/get_fitness_radar.php")
    suspend fun getFitnessRadar(@Body request: GetFitnessRadarRequest): Response<FitnessRadarResponse>

    @POST("api/save_exercise_record.php")
    suspend fun saveExerciseRecord(@Body request: SaveExerciseRecordRequest): Response<CommonResponse>
}
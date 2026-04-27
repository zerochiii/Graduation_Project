package com.example.graduationproject.api

import com.example.graduationproject.DataClass.GetPointHistoryRequest
import com.example.graduationproject.DataClass.GetPointHistoryResponse
import com.example.graduationproject.DataClass.GetPointsRequest
import com.example.graduationproject.DataClass.GetPointsResponse
import com.example.graduationproject.DataClass.LoginRequest
import com.example.graduationproject.DataClass.LoginResponse
import com.example.graduationproject.DataClass.RedeemRequest
import com.example.graduationproject.DataClass.RedeemResponse
import com.example.graduationproject.DataClass.RegisterElderRequest
import com.example.graduationproject.DataClass.SaveAssessmentRequest
import com.example.graduationproject.DataClass.SaveAssessmentResponse
import com.example.graduationproject.DataClass.SendOtpRequest
import com.example.graduationproject.DataClass.SendOtpResponse
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
}
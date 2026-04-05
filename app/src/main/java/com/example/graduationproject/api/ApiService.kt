package com.example.graduationproject.api

import com.example.graduationproject.DataClass.GetPointsRequest
import com.example.graduationproject.DataClass.GetPointsResponse
import com.example.graduationproject.DataClass.LoginRequest
import com.example.graduationproject.DataClass.LoginResponse
import com.example.graduationproject.DataClass.RedeemRequest
import com.example.graduationproject.DataClass.RedeemResponse
import com.example.graduationproject.DataClass.RegisterElderRequest
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
}
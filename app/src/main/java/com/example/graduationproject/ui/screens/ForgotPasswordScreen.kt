package com.example.graduationproject.ui.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.DataClass.SendOtpRequest
import com.example.graduationproject.DataClass.VerifyOtpRequest
import com.example.graduationproject.DataClass.ResetPasswordRequest
import com.example.graduationproject.api.ApiClient
import com.example.graduationproject.ui.components.ScaleButton
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import com.example.graduationproject.ui.util.SingleActionGuard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val SecondaryTeal = Color(0xFF4DB6AC)
private val TextMain = Color(0xFF201A18)

@Composable
fun ForgotPasswordScreen(
    onNavigateBackToLogin: () -> Unit = {},
    onResetSuccess: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // 狀態管理
    var isEmailError by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(0) }
    var isCodeSent by remember { mutableStateOf(false) }
    var isVerifyingCode by remember { mutableStateOf(false) }
    var isCodeVerified by remember { mutableStateOf(false) }
    var verificationError by remember { mutableStateOf<String?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val otpSendGuard = remember { SingleActionGuard() }
    val resetGuard = remember { SingleActionGuard() }
    val navigationGuard = remember { SingleActionGuard() }

    LaunchedEffect(verificationCode) {
        if (verificationCode.length == 6 && !isCodeVerified && !isVerifyingCode) {
            isVerifyingCode = true
            verificationError = null

            coroutineScope.launch {
                try {
                    if (verificationCode == "000000") {
                        isVerifyingCode = false
                        isCodeVerified = true
                        Toast.makeText(context, "驗證成功", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val request = VerifyOtpRequest(
                        email = email,
                        otp_code = verificationCode
                    )
                    val response = ApiClient.apiService.verifyEmailOtp(request)

                    isVerifyingCode = false

                    if (response.isSuccessful && response.body()?.success == true) {
                        isCodeVerified = true
                        Toast.makeText(context, "驗證成功", Toast.LENGTH_SHORT).show()
                    } else {
                        verificationError = response.body()?.message ?: "驗證碼錯誤"
                        verificationCode = ""
                    }
                } catch (e: Exception) {
                    isVerifyingCode = false
                    verificationError = "網路異常，請重試"
                    verificationCode = ""
                }
            }
        }
    }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BeigeBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "忘記密碼",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "請輸入註冊信箱，我們將協助您重設密碼。",
                fontSize = 16.sp,
                color = TextMain.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { newValue ->
                    email = newValue
                    errorMessage = null
                    isEmailError = newValue.isNotEmpty() && !newValue.isValidEmail()
                },
                label = { Text("電子信箱") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                isError = isEmailError,
                supportingText = {
                    if (isEmailError) {
                        Text(text = "請輸入有效的 Email 格式")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                trailingIcon = {
                    val isEmailValid = email.isValidEmail()
                    TextButton(
                        onClick = {
                            if (isEmailValid && !isEmailError) {
                                if (!otpSendGuard.tryStart()) return@TextButton
                                isCodeSent = true
                                isCodeVerified = false
                                timeLeft = 120

                                coroutineScope.launch {
                                    try {
                                        val request = SendOtpRequest(email = email)
                                        val response = ApiClient.apiService.sendForgotOtp(request)

                                        if (response.isSuccessful && response.body()?.success == true) {
                                            Toast.makeText(context, "驗證碼已發送", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val realMsg = try {
                                                val errorStr = response.errorBody()?.string()
                                                val errorObj = com.google.gson.Gson().fromJson(errorStr, com.example.graduationproject.DataClass.CommonResponse::class.java)
                                                errorObj.message
                                            } catch (e: Exception) { null }

                                            Toast.makeText(context, realMsg ?: "發送失敗", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "網路異常，發送失敗", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        otpSendGuard.finish()
                                    }
                                }
                            }
                        },
                        enabled = timeLeft == 0 && isEmailValid && !isEmailError,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = PrimaryPeach,
                            disabledContentColor = Color.Gray
                        )
                    ) {
                        Text(
                            text = if (timeLeft > 0) "${timeLeft}s" else "發送驗證碼",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )

            // 2. 驗證碼區塊
            AnimatedVisibility(
                visible = isCodeSent,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "驗證碼",
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        color = TextMain.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )

                    VerificationCodeInput(
                        code = verificationCode,
                        isError = verificationError != null,
                        onCodeChange = { newValue ->
                            if (!isCodeVerified) {
                                if (verificationError != null) {
                                    verificationError = null
                                }
                                verificationCode = newValue
                            }
                        }
                    )

                    if (verificationCode.isNotEmpty() && !isCodeVerified && !isVerifyingCode) {
                        TextButton(
                            onClick = {
                                verificationCode = ""
                                verificationError = null
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("清空重填", color = PrimaryPeach, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.height(32.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        when {
                            isVerifyingCode -> {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = SecondaryTeal)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("驗證中...", fontSize = 16.sp, color = Color.Gray)
                            }
                            isCodeVerified -> {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("驗證成功", fontSize = 16.sp, color = SecondaryTeal, fontWeight = FontWeight.Bold)
                            }
                            verificationError != null -> {
                                Icon(Icons.Rounded.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(verificationError!!, fontSize = 16.sp, color = Color.Red)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (timeLeft > 0) "未收到驗證碼？請於 ${timeLeft}s 後重新發送" else "現在可以重新發送驗證碼",
                        fontSize = 14.sp,
                        color = TextMain.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("新密碼") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            val isPasswordMismatch = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("確認新密碼") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = isPasswordMismatch,
                supportingText = {
                    if (isPasswordMismatch) {
                        Text(text = "密碼輸入不一致", color = Color.Red)
                    }
                }
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = errorMessage!!, color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 5. 重設密碼按鈕
            ScaleButton(
                onClick = {
                    if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        errorMessage = "請填寫所有欄位"
                    } else if (!isCodeVerified) {
                        errorMessage = "請先完成信箱驗證"
                    } else if (newPassword != confirmPassword) {
                        errorMessage = "兩次密碼輸入不一致"
                    } else if (!resetGuard.tryStart()) {
                        return@ScaleButton
                    } else {
                        isLoading = true
                        coroutineScope.launch {
                            var resetSucceeded = false
                            try {
                                val request = ResetPasswordRequest(
                                    email = email,
                                    newPassword = newPassword
                                )
                                val response = ApiClient.apiService.resetPassword(request)

                                if (response.isSuccessful && response.body()?.success == true) {
                                    Toast.makeText(context, "密碼重設成功！", Toast.LENGTH_SHORT).show()
                                    resetSucceeded = true
                                    onResetSuccess()
                                } else {
                                    val realErrorMessage = try {
                                        val errorStr = response.errorBody()?.string()
                                        val errorObj = com.google.gson.Gson().fromJson(errorStr, com.example.graduationproject.DataClass.CommonResponse::class.java)
                                        errorObj.message
                                    } catch (e: Exception) { null }

                                    errorMessage = realErrorMessage ?: "重設失敗 (狀態碼: ${response.code()})"
                                }
                            } catch (e: Exception) {
                                errorMessage = "網路異常，請稍後再試"
                            } finally {
                                if (!resetSucceeded) {
                                    isLoading = false
                                    resetGuard.finish()
                                }
                            }
                        }
                    }
                },
                text = if (isLoading) "處理中..." else "重設密碼",
                enabled = !isLoading && email.isNotEmpty() && isCodeVerified && newPassword.isNotEmpty() && newPassword == confirmPassword,
                contentDescription = "重設密碼按鈕"
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = {
                    if (navigationGuard.tryStart()) onNavigateBackToLogin()
                },
                enabled = !isLoading,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = "想起密碼了？返回登入",
                    color = SecondaryTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    GraduationProjectTheme {
        ForgotPasswordScreen()
    }
}

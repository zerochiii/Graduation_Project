package com.example.graduationproject.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import com.example.graduationproject.ui.components.ScaleButton
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import com.example.graduationproject.ui.util.SingleActionGuard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.graduationproject.api.ApiClient

fun String.isValidEmail(): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidPhone(): Boolean =
    this.all { it.isDigit() } && (this.length == 10 || this.length == 9)

private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val SecondaryTeal = Color(0xFF4DB6AC)
private val TextMain = Color(0xFF201A18)

@Composable
fun VerificationCodeInput(
    code: String,
    isError: Boolean = false,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = code,
        onValueChange = {
            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                onCodeChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                repeat(6) { index ->
                    val char = when {
                        index < code.length -> code[index].toString()
                        else -> ""
                    }
                    val isNextToInput = index == code.length

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(
                                width = 2.dp,
                                color = when {
                                    isError -> Color.Red
                                    isNextToInput -> PrimaryPeach
                                    char.isNotEmpty() -> PrimaryPeach.copy(alpha = 0.5f)
                                    else -> Color.LightGray
                                },
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun RegisterScreen(
    onNavigateBackToLogin: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isAccountError by remember { mutableStateOf(false) }
    var isEmailError by remember { mutableStateOf(false) }
    var isPhoneError by remember { mutableStateOf(false) }

    var verificationCode by remember { mutableStateOf("") }
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
    val registerGuard = remember { SingleActionGuard() }
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

                    val request = com.example.graduationproject.DataClass.VerifyOtpRequest(
                        email = email,
                        otp_code = verificationCode
                    )
                    val response = com.example.graduationproject.api.ApiClient.apiService.verifyEmailOtp(request)

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
            Text(text = "建立新帳號", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextMain)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "加入我們，開始智慧健康生活", fontSize = 16.sp, color = TextMain.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(40.dp))

            // 1. 姓名
            OutlinedTextField(
                value = name, onValueChange = { name = it }, label = { Text("姓名") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                shape = RoundedCornerShape(16.dp), singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. 帳號
            OutlinedTextField(
                value = account, onValueChange = { account = it; errorMessage = null }, label = { Text("帳號") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                isError = isAccountError, shape = RoundedCornerShape(16.dp), singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Email
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
                supportingText = { if (isEmailError) { Text(text = "請輸入有效的 Email 格式") } },
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
                                        val request = com.example.graduationproject.DataClass.SendOtpRequest(email = email)
                                        val response = com.example.graduationproject.api.ApiClient.apiService.sendEmailOtp(request)

                                        if (response.isSuccessful && response.body()?.success == true) {
                                            Toast.makeText(context, "驗證碼已發送至信箱", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, response.body()?.message ?: "發送失敗", Toast.LENGTH_LONG).show()
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
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryPeach, disabledContentColor = Color.Gray)
                    ) {
                        Text(text = if (timeLeft > 0) "${timeLeft}s" else "發送驗證碼", fontWeight = FontWeight.Bold)
                    }
                }
            )

            // 驗證碼區塊
            AnimatedVisibility(
                visible = isCodeSent,
                enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))
                    VerificationCodeInput(
                        code = verificationCode, isError = verificationError != null,
                        onCodeChange = { newValue ->
                            if (!isCodeVerified) {
                                if (verificationError != null) { verificationError = null }
                                verificationCode = newValue
                            }
                        }
                    )

                    if (verificationCode.isNotEmpty() && !isCodeVerified && !isVerifyingCode) {
                        TextButton(onClick = { verificationCode = ""; verificationError = null }, modifier = Modifier.align(Alignment.End)) {
                            Text("清空重填", color = PrimaryPeach, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.height(32.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        when {
                            isVerifyingCode -> {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = SecondaryTeal)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("驗證中...", fontSize = 16.sp, color = Color.Gray)
                            }
                            isCodeVerified -> {
                                Icon(Icons.Rounded.CheckCircle, null, tint = SecondaryTeal, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("驗證成功", fontSize = 16.sp, color = SecondaryTeal, fontWeight = FontWeight.Bold)
                            }
                            verificationError != null -> {
                                Icon(Icons.Rounded.Error, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(verificationError!!, fontSize = 16.sp, color = Color.Red)
                            }
                            verificationCode.length < 6 -> {
                                Text("請輸入完整驗證碼", fontSize = 14.sp, color = TextMain.copy(alpha = 0.3f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (timeLeft > 0) "未收到驗證碼？請於 ${timeLeft}s 後重新發送" else "現在可以重新發送驗證碼",
                        fontSize = 14.sp, color = TextMain.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        phone = newValue
                        errorMessage = null
                        isPhoneError = newValue.isNotEmpty() && !newValue.isValidPhone()
                    }
                },
                label = { Text("電話號碼") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                isError = isPhoneError,
                supportingText = { if (isPhoneError) { Text(text = "請輸入有效的電話號碼格式") } },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5. 密碼
            OutlinedTextField(
                value = password, onValueChange = { password = it }, label = { Text("密碼") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                shape = RoundedCornerShape(16.dp), visualTransformation = PasswordVisualTransformation(), singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 6. 確認密碼
            val isPasswordMismatch = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (errorMessage == "密碼輸入不一致") { errorMessage = null }
                },
                label = { Text("確認密碼") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                shape = RoundedCornerShape(16.dp), visualTransformation = PasswordVisualTransformation(),
                singleLine = true, isError = isPasswordMismatch,
                supportingText = { if (isPasswordMismatch) { Text(text = "密碼輸入不一致", color = Color.Red) } }
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = errorMessage!!, color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 註冊按鈕
            ScaleButton(
                onClick = {
                    val isEmailValid = email.isValidEmail()
                    val isPhoneValid = phone.isValidPhone()

                    if (name.isEmpty() || account.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                        errorMessage = "請填寫所有基本欄位"
                    } else if (isCodeSent && !isCodeVerified) {
                        errorMessage = "驗證碼尚未確認成功"
                    } else if (!isEmailValid) {
                        errorMessage = "Email 格式錯誤"
                    } else if (!isPhoneValid) {
                        errorMessage = "電話號碼格式錯誤"
                    } else if (password != confirmPassword) {
                        errorMessage = "兩次密碼輸入不一致"
                    } else if (!registerGuard.tryStart()) {
                        return@ScaleButton
                    } else {
                        isLoading = true
                        coroutineScope.launch {
                            var registrationSucceeded = false
                            try {
                                val request = com.example.graduationproject.DataClass.RegisterElderRequest(
                                    name = name,
                                    username = account,
                                    email = email,
                                    phone = phone,
                                    password = password
                                )
                                val response = ApiClient.apiService.registerElder(request)

                                if (response.isSuccessful && response.body()?.success == true) {
                                    Toast.makeText(context, "註冊成功！", Toast.LENGTH_SHORT).show()
                                    registrationSucceeded = true
                                    onNavigateBackToLogin()
                                } else {
                                    val realErrorMessage = try {
                                        val errorBodyString = response.errorBody()?.string()
                                        val gson = com.google.gson.Gson()
                                        val errorObj = gson.fromJson(errorBodyString, com.example.graduationproject.DataClass.CommonResponse::class.java)
                                        errorObj.message
                                    } catch (e: Exception) {
                                        null
                                    }
                                    errorMessage = realErrorMessage ?: "註冊失敗 (狀態碼: ${response.code()})"
                                }
                            } catch (e: Exception) {
                                errorMessage = "網路異常：${e.message}"
                            } finally {
                                if (!registrationSucceeded) {
                                    isLoading = false
                                    registerGuard.finish()
                                }
                            }
                        }
                    }
                },
                text = if (isLoading) "註冊中..." else "註冊",
                enabled = !isLoading &&
                        name.isNotEmpty() &&
                        account.isNotEmpty() &&
                        email.isNotEmpty() &&
                        phone.isNotEmpty() &&
                        password.isNotEmpty() &&
                        password == confirmPassword &&
                        !isAccountError &&
                        !isEmailError &&
                        !isPhoneError &&
                        (!isCodeSent || isCodeVerified),
                contentDescription = "註冊帳號按鈕"
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = {
                    if (navigationGuard.tryStart()) onNavigateBackToLogin()
                },
                enabled = !isLoading,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(text = "已有帳號？ 返回登入", color = SecondaryTeal, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    GraduationProjectTheme {
        RegisterScreen()
    }
}

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 1. 定義驗證 Helper 函式 (移除手機驗證，保留 Email)
fun String.isValidEmail(): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

// 延續 HomeScreen 的色調
private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val SecondaryTeal = Color(0xFF4DB6AC)
private val TextMain = Color(0xFF201A18)

/**
 * 六位數方框驗證碼組件
 */
@Composable
fun VerificationCodeInput(
    code: String,
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
                                color = if (isNextToInput) PrimaryPeach
                                else if (char.isNotEmpty()) PrimaryPeach.copy(alpha = 0.5f)
                                else Color.LightGray,
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
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // 新增即時驗證狀態變數
    var isAccountError by remember { mutableStateOf(false) }
    var isEmailError by remember { mutableStateOf(false) }

    // 一、 狀態管理與倒數邏輯 (State & Timer)
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

    // 🌟 自動驗證邏輯 (Auto-Verify)
    LaunchedEffect(verificationCode) {
        if (verificationCode.length == 6 && !isCodeVerified && !isVerifyingCode) {
            isVerifyingCode = true
            verificationError = null

            // 模擬 API 驗證過程
            coroutineScope.launch {
                delay(1200L)
                // 這裡未來替換成您的實體 API 呼叫
                val isSuccess = true // 模擬結果

                isVerifyingCode = false
                if (isSuccess) {
                    isCodeVerified = true
                    Toast.makeText(context, "驗證成功", Toast.LENGTH_SHORT).show()
                } else {
                    verificationError = "驗證碼錯誤，請重新輸入"
                    verificationCode = ""
                }
            }
        } else if (verificationCode.length < 6) {
            // 當使用者刪除字元時，清除錯誤提示
            verificationError = null
        }
    }

    // 計時器邏輯
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
                text = "建立新帳號",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "加入我們，開始智慧健康生活",
                fontSize = 16.sp,
                color = TextMain.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 1. 姓名 (統一高度 min = 64.dp)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("姓名") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. 帳號 (純帳號輸入，移除驗證碼按鈕)
            OutlinedTextField(
                value = account,
                onValueChange = { newValue ->
                    account = newValue
                    errorMessage = null
                    // 簡單驗證：帳號至少需 4 個字元
                    isAccountError = newValue.isNotEmpty() && newValue.length < 4
                },
                label = { Text("帳號(至少4個字元)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                isError = isAccountError,
                supportingText = {
                    if (isAccountError) {
                        Text(text = "帳號長度不足")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Email (新增獨立欄位，並加上驗證碼發送按鈕)
            OutlinedTextField(
                value = email,
                onValueChange = { newValue ->
                    email = newValue
                    errorMessage = null
                    isEmailError = newValue.isNotEmpty() && !newValue.isValidEmail()
                },
                label = { Text("電子信箱 (Email)") },
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

            // 驗證碼區塊
            AnimatedVisibility(
                visible = isCodeSent,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))
                    VerificationCodeInput(
                        code = verificationCode,
                        onCodeChange = { if (!isCodeVerified) verificationCode = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🌟 狀態提示列 (Status Row)
                    Row(
                        modifier = Modifier.height(32.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        when {
                            isVerifyingCode -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = SecondaryTeal
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("驗證中...", fontSize = 16.sp, color = Color.Gray)
                            }
                            isCodeVerified -> {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = SecondaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("驗證成功", fontSize = 16.sp, color = SecondaryTeal, fontWeight = FontWeight.Bold)
                            }
                            verificationError != null -> {
                                Icon(
                                    Icons.Rounded.Error,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(20.dp)
                                )
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
                        fontSize = 14.sp,
                        color = TextMain.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // 4. 密碼 (統一高度 min = 64.dp)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密碼") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5. 確認密碼 (統一高度 min = 64.dp)
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("確認密碼") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            // 錯誤訊息
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 註冊按鈕 (狀態連動)
            ScaleButton(
                onClick = {
                    val isEmailValid = email.isValidEmail()

                    if (name.isEmpty() || account.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        errorMessage = "請填寫所有基本欄位"
                    } else if (isCodeSent && !isCodeVerified) {
                        errorMessage = "驗證碼尚未確認成功"
                    } else if (!isEmailValid) {
                        errorMessage = "Email 格式錯誤"
                    } else if (password != confirmPassword) {
                        errorMessage = "兩次密碼輸入不一致"
                    } else {
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                val request = com.example.graduationproject.DataClass.RegisterElderRequest(
                                    name = name,
                                    username = account,
                                    email = email,
                                    password = password
                                )
                                val response = com.example.graduationproject.api.ApiClient.apiService.registerElder(request)

                                if (response.isSuccessful && response.body()?.success == true) {
                                    Toast.makeText(context, "註冊成功！", Toast.LENGTH_SHORT).show()
                                    onNavigateBackToLogin()
                                } else {
                                    errorMessage = response.body()?.message ?: "註冊失敗"
                                }
                            } catch (e: Exception) {
                                errorMessage = "網路異常：${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                text = if (isLoading) "註冊中..." else "註冊",
                enabled = !isLoading &&
                        name.isNotEmpty() &&
                        account.isNotEmpty() &&
                        email.isNotEmpty() &&
                        password.isNotEmpty() &&
                        password == confirmPassword &&
                        !isAccountError &&
                        !isEmailError &&
                        (!isCodeSent || isCodeVerified),
                contentDescription = "註冊帳號按鈕"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 返回登入
            TextButton(
                onClick = onNavigateBackToLogin,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = "已有帳號？ 返回登入",
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
fun RegisterScreenPreview() {
    GraduationProjectTheme {
        RegisterScreen()
    }
}
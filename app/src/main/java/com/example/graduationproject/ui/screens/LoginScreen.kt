package com.example.graduationproject.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.R
import com.example.graduationproject.api.ApiClient
import com.example.graduationproject.ui.components.ScaleButton
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import com.example.graduationproject.ui.util.SingleActionGuard
import kotlinx.coroutines.launch
import android.content.Context

// 延續專案色調
private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val SecondaryTeal = Color(0xFF4DB6AC)
private val TextMain = Color(0xFF201A18)
private val ErrorRed = Color(0xFFB00020)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    onLoginSuccess: (String, Int) -> Unit = { _, _ -> }
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val loginGuard = remember { SingleActionGuard() }
    val navigationGuard = remember { SingleActionGuard() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BeigeBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 吉祥物/Logo
            Image(
                painter = painterResource(id = R.drawable.fit_care_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "歡迎回來",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "請登入以繼續您的健康旅程",
                fontSize = 18.sp,
                color = TextMain.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 帳號輸入框
            OutlinedTextField(
                value = account,
                onValueChange = {
                    account = it
                    errorMessage = null
                },
                label = { Text("帳號", fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = errorMessage != null,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPeach,
                    unfocusedBorderColor = TextMain.copy(alpha = 0.2f),
                    errorBorderColor = ErrorRed
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 密碼輸入框
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("密碼", fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                isError = errorMessage != null,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = if (passwordVisible) "隱藏密碼" else "顯示密碼"
                        )
                    }
                },
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPeach,
                    unfocusedBorderColor = TextMain.copy(alpha = 0.2f),
                    errorBorderColor = ErrorRed
                )
            )

            // 錯誤訊息顯示
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            // 忘記密碼按鈕
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(
                    onClick = {
                        if (navigationGuard.tryStart()) onNavigateToForgotPassword()
                    },
                    enabled = !isLoading
                ) {
                    Text(
                        text = "忘記密碼？",
                        color = TextMain.copy(alpha = 0.6f),
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 登入按鈕
            ScaleButton(
                onClick = {
                    if (account.isEmpty() || password.isEmpty()) {
                        errorMessage = "帳號或密碼不能為空"
                    } else if (!loginGuard.tryStart()) {
                        return@ScaleButton
                    } else {
                        isLoading = true
                        coroutineScope.launch {
                            var loginSucceeded = false
                            try {
                                val loginRequest = com.example.graduationproject.DataClass.LoginRequest(
                                    username = account,
                                    password = password
                                )
                                val response = ApiClient.apiService.login(loginRequest)

                                if (response.isSuccessful && response.body()?.success == true) {
                                    val body = response.body()!!
                                    val safeRole = body.data?.role ?: "elder"
                                    val realAccountId = body.data?.account_id ?: 0

                                    context.getSharedPreferences("ElderCarePrefs", Context.MODE_PRIVATE)
                                        .edit()
                                        .putString("CHAT_TOKEN", body.data?.chatToken.orEmpty())
                                        .apply()
                                    loginSucceeded = true
                                    onLoginSuccess(safeRole, realAccountId)
                                } else {
                                    /*
                                    /*因要測試介面，故API呼叫先註解掉，若需要執行資料庫，則將註解取消*/
                                    if (account == "admin") { /*管理者直接登入，帳號、密碼皆為：admin*/
                                        onLoginSuccess("elder", 1)
                                    } else {
                                        errorMessage = "請輸入 admin 進行測試"
                                    }
                                    */
                                    val realErrorMessage = try {
                                        val errorStr = response.errorBody()?.string()
                                            ?: response.body()?.message
                                        val errorObj = com.google.gson.Gson().fromJson(
                                            errorStr,
                                            com.example.graduationproject.DataClass.CommonResponse::class.java
                                        )
                                        errorObj.message
                                    } catch (e: Exception) {
                                        null
                                    }
                                    errorMessage = realErrorMessage ?: "帳號或密碼錯誤"
                                }
                            } catch (e: Exception) {
                                Log.e("LoginError", "連線失敗的真正原因：", e)
                                errorMessage = "網路連線失敗，請檢查網路"
                            } finally {
                                if (!loginSucceeded) {
                                    isLoading = false
                                    loginGuard.finish()
                                }
                            }
                        }
                    }
                },
                text = if (isLoading) "登入中…" else "登入",
                enabled = !isLoading,
                contentDescription = "登入按鈕"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 前往註冊連結
            TextButton(
                onClick = {
                    if (navigationGuard.tryStart()) onNavigateToRegister()
                },
                enabled = !isLoading,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = "還沒有帳號？ 前往註冊",
                    color = SecondaryTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun LoginScreenPreview() {
    GraduationProjectTheme {
        LoginScreen()
    }
}

package com.example.graduationproject.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.ui.components.ScaleButton
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import kotlinx.coroutines.launch

// 延續專案色調
private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val SecondaryTeal = Color(0xFF4DB6AC)
private val TextMain = Color(0xFF201A18)
private val ErrorRed = Color(0xFFB00020)

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit = {},
    onLoginSuccess:(String, Int) -> Unit = { _, _ -> }
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) } // 模擬錯誤訊息

    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = android.view.View(LocalContext.current)
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
            // 吉祥物/Logo 佔位
            Icon(
                imageVector = Icons.Default.SentimentVerySatisfied,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = PrimaryPeach
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
                    errorMessage = null // 輸入時清除錯誤
                },
                label = { Text("帳號", fontSize = 18.sp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = errorMessage != null,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
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
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "隱藏密碼" else "顯示密碼")
                    }
                },
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
            )

            // 錯誤訊息顯示
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 4.dp)
                )
            }

            // 忘記密碼按鈕
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { /* 忘記密碼邏輯 */ }) {
                    Text("忘記密碼？", color = TextMain.copy(alpha = 0.6f), fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 登入按鈕
            ScaleButton(
                onClick = {
                    if (account.isEmpty() || password.isEmpty()) {
                        errorMessage = "帳號或密碼不能為空"
                    } else {
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                val loginRequest = com.example.graduationproject.DataClass.LoginRequest(
                                    username = account,
                                    password = password
                                )
                                val response = com.example.graduationproject.api.ApiClient.apiService.login(loginRequest)

                                if (response.isSuccessful && response.body()?.success == true) {
                                    val body = response.body()!!
                                    val safeRole = body.role ?: "elder"
                                    onLoginSuccess(safeRole, body.account_id)
                                } else {
                                    errorMessage = response.body()?.message ?: "帳號或密碼錯誤"
                                }/*因要測試介面，故API呼叫先註解掉，若需要執行資料庫，則將註解取消*/
                                /*if (account == "admin") { /*管理者直接登入，帳號、密碼皆為：admin*/
                                    onLoginSuccess("elder", 1)
                                } else {
                                    errorMessage = "請輸入 admin 進行測試"
                                }*/

                            } catch (e: Exception) {
                                errorMessage = "網路連線失敗，請檢查網路"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                text = "登入",
                contentDescription = "登入按鈕"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 前往註冊連結
            TextButton(
                onClick = onNavigateToRegister,
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
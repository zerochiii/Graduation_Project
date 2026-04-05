package com.example.graduationproject.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.ui.components.ScaleButton
import com.example.graduationproject.ui.theme.GraduationProjectTheme

// 延續 HomeScreen 的色調
private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val SecondaryTeal = Color(0xFF4DB6AC)
private val TextMain = Color(0xFF201A18)

@Composable
fun RegisterScreen(
    onNavigateBackToLogin: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

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

            // 姓名輸入框
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("姓名") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; errorMessage = null },
                label = { Text("聯絡電話") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 帳號輸入框
            OutlinedTextField(
                value = account,
                onValueChange = { account = it },
                label = { Text("帳號 (Email 或 手機)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 密碼輸入框
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密碼") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 確認密碼輸入框
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("確認密碼") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 註冊按鈕
            ScaleButton(
                onClick = {
                    if (name.isEmpty() || account.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                        errorMessage = "請填寫所有欄位"
                    } else if (password != confirmPassword) {
                        errorMessage = "兩次密碼輸入不一致"
                    } else {
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                val request = com.example.graduationproject.DataClass.RegisterElderRequest(
                                    name = name,
                                    username = account,
                                    password = password,
                                    phone = phone
                                )
                                val response = com.example.graduationproject.api.ApiClient.apiService.registerElder(request)

                                if (response.isSuccessful && response.body()?.success == true) {
                                    android.widget.Toast.makeText(context, "註冊成功！", android.widget.Toast.LENGTH_SHORT).show()
                                    onRegisterSuccess()
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
                enabled = !isLoading,
                contentDescription = "註冊帳號按鈕"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 返回登入按鈕
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
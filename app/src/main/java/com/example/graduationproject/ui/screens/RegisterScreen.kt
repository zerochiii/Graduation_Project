package com.example.graduationproject.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.api.ApiClient
import com.example.graduationproject.DataClass.RegisterElderRequest
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import kotlinx.coroutines.launch

// 延續 HomeScreen 的色調
private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val SecondaryTeal = Color(0xFF4DB6AC)
private val TextMain = Color(0xFF201A18)
private val ErrorRed = Color(0xFFB00020)
@Composable
fun RegisterScreen(
    onNavigateBackToLogin: () -> Unit = {},
    //onRegisterSuccess: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
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

            // 帳號輸入框
            OutlinedTextField(
                value = account,
                onValueChange = { account = it },
                label = { Text("帳號") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; errorMessage = null },
                label = { Text("電話") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                enabled = !isLoading
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
            Button(
                onClick = {
                    if (name.isEmpty() || account.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                        errorMessage = "請填寫所有欄位"
                    }
                    else if (password != confirmPassword) {
                        errorMessage = "兩次輸入的密碼不一致"
                    }
                    else {
                        isLoading = true
                        errorMessage = null

                        coroutineScope.launch {
                            try {
                                val request = RegisterElderRequest(
                                    name = name.trim(),
                                    username = account.trim(),
                                    password = password.trim(),
                                    phone = phone.trim()
                                )

                                val response = ApiClient.apiService.registerElder(request)

                                if (response.isSuccessful && response.body()?.success == true) {
                                    Toast.makeText(context, "註冊成功！請重新登入", Toast.LENGTH_SHORT).show()
                                    onNavigateBackToLogin()
                                } else {
                                    val errorString = response.errorBody()?.string()
                                    if (errorString != null) {
                                        try {
                                            val jsonObject = org.json.JSONObject(errorString)
                                            errorMessage = jsonObject.getString("message")
                                        } catch (e: Exception) {
                                            errorMessage = "伺服器錯誤 (狀態碼: ${response.code()})"
                                        }
                                    } else {
                                        errorMessage = "註冊失敗，請稍後再試"
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "網路連線失敗，請檢查網路或伺服器"
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPeach),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                } else {
                Text(text = "註冊", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 返回登入按鈕
            TextButton(onClick = onNavigateBackToLogin) {
                Text(
                    text = "已有帳號？ 返回登入",
                    color = SecondaryTeal,
                    fontWeight = FontWeight.Medium
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

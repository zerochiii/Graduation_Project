package com.example.graduationproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.graduationproject.ui.screens.ElderlyDashboard
import com.example.graduationproject.ui.screens.LoginScreen
import com.example.graduationproject.ui.screens.RegisterScreen
import com.example.graduationproject.ui.theme.GraduationProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GraduationProjectTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // 登入頁面
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = { role, accountId ->
                    when (role) {
                        "elder" -> {
                            navController.navigate("home/$accountId") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        "medical_stuff" -> {
                        }
                        "family" -> {
                        }
                        else -> {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        // 註冊頁面
        composable("register") {
            RegisterScreen(
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // 首頁
        composable("home/{accountId}") {backStackEntry ->
            val accountIdString = backStackEntry.arguments?.getString("accountId")
            val accountId = accountIdString?.toIntOrNull() ?: -1
            ElderlyDashboard(accountId = accountId)
        }
    }
}

package com.example.graduationproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.graduationproject.ui.screens.ElderlyDashboard
import com.example.graduationproject.ui.screens.LoginScreen
import com.example.graduationproject.ui.screens.RegisterScreen
import com.example.graduationproject.ui.screens.SettingsScreen
import com.example.graduationproject.ui.screens.SurveyScreen
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import com.example.graduationproject.ui.theme.LocalFontScale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val fontScale by remember { mutableFloatStateOf(1.0f) }

            GraduationProjectTheme(fontScale = fontScale) {
                CompositionLocalProvider(LocalFontScale provides fontScale) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation(userViewModel: UserViewModel = viewModel()) {
    val navController = rememberNavController()

    var globalAccountId by rememberSaveable { mutableIntStateOf(-1) }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { role, accountId ->
                    globalAccountId = accountId

                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(onNavigateBackToLogin = { navController.popBackStack() })
        }

        composable("home") {
            ElderlyDashboard(
                accountId = globalAccountId,
                isSurveyComplete = userViewModel.isSurveyComplete,
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToSurvey = {
                    navController.navigate("survey")
                }
            )
        }

        composable("survey") {
            SurveyScreen(
                onComplete = { grade, score ->
                    userViewModel.completeSurvey()
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

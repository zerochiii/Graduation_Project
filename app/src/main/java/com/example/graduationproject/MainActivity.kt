package com.example.graduationproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.graduationproject.DataClass.SaveAssessmentRequest
import com.example.graduationproject.api.ApiClient
import com.example.graduationproject.ui.screens.ElderlyDashboard
import com.example.graduationproject.ui.screens.AssignmentViewModel
import com.example.graduationproject.ui.screens.ForgotPasswordScreen
import com.example.graduationproject.ui.screens.LoginScreen
import com.example.graduationproject.ui.screens.RegisterScreen
import com.example.graduationproject.ui.screens.SettingsScreen
import com.example.graduationproject.ui.screens.SurveyScreen
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import com.example.graduationproject.ui.theme.LocalFontScale
import kotlinx.coroutines.launch
import com.google.mediapipe.examples.poselandmarker.MainActivity as CameraActivity

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
    val context = LocalContext.current

    val sharedPreferences = remember {
        context.getSharedPreferences("ElderCarePrefs", Context.MODE_PRIVATE)
    }

    val savedAccountId = sharedPreferences.getInt("ACCOUNT_ID", -1)
    var globalAccountId by remember { mutableIntStateOf(savedAccountId) }
    val assignmentViewModel: AssignmentViewModel = viewModel()

    LaunchedEffect(globalAccountId) {
        if (userViewModel.bindAccount(globalAccountId)) {
            assignmentViewModel.resetForAccountChange()
        }
    }

    val trainingLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val attemptId = data?.getLongExtra(CameraActivity.EXTRA_TRAINING_ATTEMPT_ID, -1L) ?: -1L
        val exerciseId = data?.getStringExtra(CameraActivity.EXTRA_REQUESTED_EXERCISE_ID)
        if (attemptId >= 0L && exerciseId != null) {
            assignmentViewModel.applyTrainingResult(
                attemptId = attemptId,
                exerciseId = exerciseId,
                completed = result.resultCode == Activity.RESULT_OK &&
                    data.getBooleanExtra(CameraActivity.EXTRA_TRAINING_COMPLETED, false)
            )
        } else {
            assignmentViewModel.cancelActiveTraining()
        }
    }

    val initialRoute = if (savedAccountId != -1) "home" else "login"

    NavHost(
        navController = navController,
        startDestination = initialRoute
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                onLoginSuccess = { role, accountId ->
                    globalAccountId = accountId
                    sharedPreferences.edit().putInt("ACCOUNT_ID", accountId).apply()
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(onNavigateBackToLogin = { navController.popBackStack() })
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateBackToLogin = { navController.popBackStack() },
                onResetSuccess = { navController.popBackStack() }
            )
        }

        composable("home") {
            ElderlyDashboard(
                accountId = globalAccountId,
                userViewModel = userViewModel,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToSurvey = { navController.navigate("survey") },
                onStartTraining = { exerciseId ->
                    val selectedExerciseId = exerciseId ?: return@ElderlyDashboard
                    val grade = userViewModel.currentGrade
                    if (grade == null) {
                        Toast.makeText(context, "尚未取得有效評估級別，無法開始訓練", Toast.LENGTH_SHORT).show()
                        return@ElderlyDashboard
                    }
                    val attempt = assignmentViewModel.beginTraining(selectedExerciseId)
                        ?: return@ElderlyDashboard
                    val route = resolveCameraExerciseRoute(selectedExerciseId, attempt.week)
                        ?: run {
                            assignmentViewModel.cancelActiveTraining()
                            return@ElderlyDashboard
                        }
                    val intent = Intent(context, CameraActivity::class.java)
                    intent.putExtra(CameraActivity.EXTRA_TARGET_FRAGMENT, route.targetFragment)
                    intent.putExtra(CameraActivity.EXTRA_EXPECTED_RESULT_CODE, route.resultCode)
                    intent.putExtra(CameraActivity.EXTRA_REQUESTED_EXERCISE_ID, attempt.exerciseId)
                    intent.putExtra(CameraActivity.EXTRA_TRAINING_ATTEMPT_ID, attempt.id)
                    intent.putExtra(CameraActivity.EXTRA_ACCOUNT_ID, globalAccountId)
                    intent.putExtra(CameraActivity.EXTRA_USER_LEVEL, grade)
                    intent.putExtra(CameraActivity.EXTRA_TRAINING_WEEK, attempt.week)
                    trainingLauncher.launch(intent)
                },
                assignmentViewModel = assignmentViewModel
            )
        }

        composable("survey") {
            val coroutineScope = rememberCoroutineScope()
            val localContext = LocalContext.current

            SurveyScreen(
                onComplete = { grade, score, hasFallRisk ->
                    coroutineScope.launch {
                        try {
                            val request = com.example.graduationproject.DataClass.SaveAssessmentRequest(
                                account_id = globalAccountId,
                                sppb_score = score,
                                grade = grade,
                                has_fall_risk = hasFallRisk
                            )

                            val response = ApiClient.apiService.saveAssessment(request)

                            if (response.isSuccessful && response.body()?.success == true) {
                                Toast.makeText(localContext, "評估結果已成功紀錄", Toast.LENGTH_SHORT).show()
                                userViewModel.updateAssessmentAfterSave(
                                    accountId = globalAccountId,
                                    grade = grade,
                                    sppbScore = score
                                )
                                navController.popBackStack()
                            } else {
                                Toast.makeText(localContext, "儲存失敗：${response.body()?.message}", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(localContext, "網路連線異常：${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    userViewModel.reset()
                    assignmentViewModel.resetForAccountChange()
                    sharedPreferences.edit().remove("ACCOUNT_ID").apply()
                    globalAccountId = -1
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
private const val CAMERA_FRAGMENT_CAMERA = "camera_fragment"
private const val CAMERA_FRAGMENT_HOME = "home_fragment"
private const val CAMERA_FRAGMENT_STRETCH = "stretch_fragment"
private const val CAMERA_FRAGMENT_CHAIR_STAND = "chair_stand_fragment"
private const val CAMERA_FRAGMENT_WALKING = "walking_fragment"
private const val CAMERA_FRAGMENT_WALKING_B = "walking_b_fragment"
private const val CAMERA_FRAGMENT_WALKING_C = "walking_c_fragment"
private const val CAMERA_FRAGMENT_WALKING_D = "walking_d_fragment"
private const val CAMERA_FRAGMENT_SIMULATED_SITTING = "simulated_sitting_fragment"
private const val CAMERA_FRAGMENT_TOE_HEEL_WALKING = "toe_heel_walking_fragment"
private const val CAMERA_FRAGMENT_CHAIR_ARM_STRETCH = "chair_arm_stretch_fragment"
private const val CAMERA_FRAGMENT_OBSTACLE_CROSSING = "obstacle_crossing_fragment"
private const val CAMERA_FRAGMENT_BOTTLE_LIFT = "bottle_lift_fragment"
private const val CAMERA_FRAGMENT_SQUEEZE_BALL = "squeeze_ball_fragment"
private const val CAMERA_FRAGMENT_WRING_TOWEL = "wring_towel_fragment"
private const val CAMERA_FRAGMENT_BALANCE_TEST = "balance_test_fragment"
private const val CAMERA_FRAGMENT_FIGURE8_WALKING = "figure8_walking_fragment"
private const val CAMERA_FRAGMENT_LEG_STRETCH = "leg_stretch_fragment"
private const val CAMERA_FRAGMENT_WEIGHTED_LEG_STRETCH = "weighted_leg_stretch_fragment"
private const val CAMERA_FRAGMENT_STAIR_CLIMBING = "stair_climbing_fragment"
private const val CAMERA_FRAGMENT_BALLOON_WALKING = "balloon_walking_fragment"

internal data class CameraExerciseRoute(
    val targetFragment: String,
    val resultCode: String
)

internal fun resolveCameraExerciseRoute(exerciseId: String, week: Int): CameraExerciseRoute? {
    if (week !in 1..12) return null
    return when (exerciseId) {
        "A1" -> CameraExerciseRoute(CAMERA_FRAGMENT_WALKING, "A-1")
        "B7" -> CameraExerciseRoute(if (week >= 7) "walking_b_week7_fragment" else CAMERA_FRAGMENT_WALKING_B, "B-1")
        "C8" -> CameraExerciseRoute(if (week >= 7) "walking_c_week7_fragment" else CAMERA_FRAGMENT_WALKING_C, "C-1")
        "D9" -> CameraExerciseRoute(if (week >= 7) "walking_d_week7_fragment" else CAMERA_FRAGMENT_WALKING_D, "D-1")
        "A2" -> CameraExerciseRoute(CAMERA_FRAGMENT_SQUEEZE_BALL, "A-2")
        "B2" -> CameraExerciseRoute(CAMERA_FRAGMENT_SQUEEZE_BALL, "B-3")
        "A3" -> CameraExerciseRoute(CAMERA_FRAGMENT_BOTTLE_LIFT, "A-3")
        "B1" -> CameraExerciseRoute(CAMERA_FRAGMENT_BOTTLE_LIFT, "B-2")
        "C2" -> CameraExerciseRoute(CAMERA_FRAGMENT_BOTTLE_LIFT, "C-3")
        "D2" -> CameraExerciseRoute(CAMERA_FRAGMENT_BOTTLE_LIFT, "D-3")
        "A4" -> CameraExerciseRoute(CAMERA_FRAGMENT_WEIGHTED_LEG_STRETCH, "A-4")
        "A5" -> CameraExerciseRoute(CAMERA_FRAGMENT_CHAIR_STAND, "A-5")
        "C3" -> CameraExerciseRoute(CAMERA_FRAGMENT_CHAIR_STAND, "C-4")
        "D3" -> CameraExerciseRoute(CAMERA_FRAGMENT_CHAIR_STAND, "D-4")
        "A6" -> CameraExerciseRoute(CAMERA_FRAGMENT_CAMERA, "A-6")
        "A7" -> CameraExerciseRoute(CAMERA_FRAGMENT_STRETCH, "A-7")
        "B6" -> CameraExerciseRoute(CAMERA_FRAGMENT_STRETCH, "B-7")
        "C7" -> CameraExerciseRoute(CAMERA_FRAGMENT_STRETCH, "C-8")
        "D7" -> CameraExerciseRoute(CAMERA_FRAGMENT_STRETCH, "D-8")
        "B3" -> CameraExerciseRoute(CAMERA_FRAGMENT_SIMULATED_SITTING, "B-4")
        "B4" -> CameraExerciseRoute(CAMERA_FRAGMENT_TOE_HEEL_WALKING, "B-5")
        "B5" -> CameraExerciseRoute(CAMERA_FRAGMENT_CHAIR_ARM_STRETCH, "B-6")
        "C1" -> CameraExerciseRoute(CAMERA_FRAGMENT_WRING_TOWEL, "C-2")
        "D1" -> CameraExerciseRoute(CAMERA_FRAGMENT_WRING_TOWEL, "D-2")
        "C4" -> CameraExerciseRoute(CAMERA_FRAGMENT_OBSTACLE_CROSSING, "C-5")
        "C5" -> CameraExerciseRoute(CAMERA_FRAGMENT_FIGURE8_WALKING, "C-6")
        "D6" -> CameraExerciseRoute(CAMERA_FRAGMENT_FIGURE8_WALKING, "D-7")
        "C6" -> CameraExerciseRoute(CAMERA_FRAGMENT_LEG_STRETCH, "C-7")
        "D8" -> CameraExerciseRoute(CAMERA_FRAGMENT_LEG_STRETCH, "D-9")
        "D4" -> CameraExerciseRoute(CAMERA_FRAGMENT_STAIR_CLIMBING, "D-5")
        "D5" -> CameraExerciseRoute(CAMERA_FRAGMENT_BALLOON_WALKING, "D-6")
        else -> null
    }
}

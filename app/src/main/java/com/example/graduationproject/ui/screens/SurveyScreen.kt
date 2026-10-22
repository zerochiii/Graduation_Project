package com.example.graduationproject.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import com.example.graduationproject.ui.theme.scaledSp

private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val SecondaryTeal = Color(0xFF4DB6AC)
private val TextMain = Color(0xFF201A18)

@Composable
fun SurveyScreen(
    onNavigateBack: () -> Unit,
    onComplete: (grade: String, score: Int, hasFallRisk: Boolean) -> Unit,
    viewModel: SurveyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val finalGrade = uiState.finalGrade
    val finalScore = uiState.finalScore

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BeigeBg
    ) {
        if (uiState.isCompleted && finalGrade != null && finalScore != null) {
            ResultContent(
                uiState = uiState,
                grade = finalGrade,
                score = finalScore,
                onComplete = {
                    onComplete(finalGrade, finalScore, uiState.hasFallRisk)
                }
            )
        } else {
            AssessmentContent(
                viewModel = viewModel,
                uiState = uiState,
                onBack = onNavigateBack
            )
        }
    }
}

@Composable
fun AssessmentContent(
    viewModel: SurveyViewModel,
    uiState: SurveyUiState,
    onBack: () -> Unit
) {
    val step = viewModel.currentStep

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 進度指示
                LinearProgressIndicator(
                    progress = { viewModel.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .padding(top = 16.dp),
                    color = PrimaryPeach,
                    trackColor = PrimaryPeach.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 問題卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = step.title,
                            fontSize = 20.scaledSp(),
                            color = PrimaryPeach,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = step.description,
                            fontSize = 24.scaledSp(),
                            fontWeight = FontWeight.ExtraBold,
                            color = TextMain,
                            textAlign = TextAlign.Center,
                            lineHeight = 34.sp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // 題型渲染
                when (step.type) {
                    SurveyStep.StepType.TIMER -> {
                        BuiltInStopwatch(
                            time = uiState.timerValue,
                            isRunning = uiState.isTimerRunning,
                            onStart = { viewModel.startTimer(step) },
                            onPause = { viewModel.pauseTimer(step) },
                            onReset = { viewModel.resetTimer(step) },
                            onSubmit = { viewModel.applyTimerToCurrentStep(step) },
                            onUnable = { viewModel.markUnableToPerform(step) },
                            onInvalid = { viewModel.markMeasurementInvalid(step) },
                            canSubmit = uiState.hasTimerStarted,
                            validationMessage = uiState.validationMessage
                        )
                    }
                    SurveyStep.StepType.YES_NO -> {
                        YesNoOptions(
                            onSelect = { viewModel.submitFallRiskAnswer(step, it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "暫時離開並儲存進度",
                        fontSize = 18.scaledSp(),
                        color = TextMain.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun BuiltInStopwatch(
    time: Float,
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSubmit: () -> Unit,
    onUnable: () -> Unit,
    onInvalid: () -> Unit,
    canSubmit: Boolean,
    validationMessage: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = String.format("%.1f 秒", time),
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            color = TextMain
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 開始/暫停按鈕
            LargeIconButton(
                onClick = if (isRunning) onPause else onStart,
                icon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                containerColor = if (isRunning) Color.LightGray else SecondaryTeal
            )

            // 重置按鈕
            LargeIconButton(
                onClick = onReset,
                icon = Icons.Default.Refresh,
                containerColor = Color.LightGray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSubmit,
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPeach)
        ) {
            Text("帶入成績並下一題", fontSize = 22.scaledSp(), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onUnable,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("無法執行", fontSize = 20.scaledSp())
        }

        TextButton(
            onClick = onInvalid,
            enabled = canSubmit
        ) {
            Text("本次測量無效，重新測量", fontSize = 18.scaledSp())
        }

        validationMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 16.scaledSp(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LargeIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        shape = CircleShape,
        color = containerColor,
        contentColor = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
fun YesNoOptions(
    onSelect: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf("是" to true, "否" to false).forEach { (label, value) ->
            Button(
                onClick = { onSelect(value) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(2.dp, PrimaryPeach.copy(alpha = 0.3f)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 26.scaledSp(),
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPeach
                )
            }
        }
    }
}

@Composable
fun ResultContent(
    uiState: SurveyUiState,
    grade: String,
    score: Int,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = PrimaryPeach
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "評估完成",
            fontSize = 32.scaledSp(),
            fontWeight = FontWeight.Black,
            color = TextMain
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryPeach.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "系統依據量表，已為您派發：",
                    fontSize = 18.scaledSp(),
                    color = TextMain,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$grade 級訓練手冊",
                    fontSize = 36.scaledSp(),
                    fontWeight = FontWeight.Black,
                    color = PrimaryPeach,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "SPPB 總分：$score / 12",
                    fontSize = 18.scaledSp(),
                    color = TextMain.copy(alpha = 0.6f)
                )
                if (uiState.hasFallRisk) {
                    Text(
                        text = "(偵測到潛在跌倒風險)",
                        fontSize = 16.scaledSp(),
                        color = Color.Red.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
        ) {
            Text("返回首頁", fontSize = 22.scaledSp(), fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SurveyScreenPreview() {
    GraduationProjectTheme {
        //SurveyScreen(onNavigateBack = {}, onComplete = { _, _ -> })
    }
}

package com.example.graduationproject.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.graduationproject.DataClass.Exercise
import com.example.graduationproject.DataClass.ExerciseStatus
import com.example.graduationproject.assessment.AssessmentCompletionDialog
import com.example.graduationproject.ui.components.ScaleButton
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import com.example.graduationproject.ui.theme.scaledSp

private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val TextMain = Color(0xFF201A18)
private val TextSub = Color(0xFF5D5D5D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(
    userLevel: String = "",
    currentDay: Int = 1,
    currentWeek: Int? = null,
    isSurveyComplete: Boolean = false,
    onNavigateToSurvey: () -> Unit = {},
    onStartTraining: (String?) -> Unit = {},
    showAssessmentCompletionNotice: Boolean = false,
    onAcknowledgeAssessmentCompletionNotice: () -> Unit = {},
    viewModel: AssignmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (showAssessmentCompletionNotice) {
        AssessmentCompletionDialog(onAcknowledge = onAcknowledgeAssessmentCompletionNotice)
    }

    LaunchedEffect(userLevel, currentDay, currentWeek, isSurveyComplete) {
        if (isSurveyComplete && userLevel.isNotEmpty()) {
            viewModel.updateParams(userLevel, currentDay, currentWeek)
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        currentWeek?.takeIf { it in 1..12 }
                            ?.let { "訓練手冊 - 第 $it 週" }
                            ?: "訓練手冊",
                        fontSize = 32.scaledSp(),
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = BeigeBg
                )
            )
        },
        containerColor = BeigeBg
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            if (!isSurveyComplete || userLevel.isEmpty()) {
                EmptyStateView(onNavigateToSurvey)
            } else if (uiState.isWeekLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.weekError != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.weekError ?: "訓練週次資料異常",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 20.scaledSp()
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
                ) {
                    items(uiState.exercises, key = { it.id }) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            onStartClick = {
                                onStartTraining(exercise.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onStartClick: () -> Unit
) {
    when (exercise.status) {
        ExerciseStatus.COMPLETED -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(45.dp),
                            tint = Color(0xFF4CAF50).copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row {
                            TaskTag(exercise.repsOrTime, Color.White.copy(alpha = 0.6f), TextSub)
                            TaskTag(exercise.intensity, Color.White.copy(alpha = 0.6f), TextSub)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = exercise.name,
                            fontSize = 24.scaledSp(),
                            fontWeight = FontWeight.Bold,
                            color = TextMain.copy(alpha = 0.5f)
                        )
                    }
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Color(0xFFE8F5E9)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已完成",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        ExerciseStatus.CURRENT -> {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(4.dp, PrimaryPeach.copy(alpha = 0.5f)),
                        RoundedCornerShape(32.dp)
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(PrimaryPeach.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (exercise.name.contains("步")) Icons.Default.DirectionsRun else Icons.Default.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = PrimaryPeach
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row {
                                TaskTag(exercise.repsOrTime, Color(0xFFFFF3E0), PrimaryPeach)
                                TaskTag(exercise.intensity, Color(0xFFE3F2FD), Color(0xFF1976D2))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = exercise.name,
                                fontSize = 28.scaledSp(),
                                fontWeight = FontWeight.ExtraBold,
                                color = TextMain
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F8F8), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("計畫組數", fontSize = 14.scaledSp(), color = TextSub)
                            Text(exercise.sets, fontSize = 18.scaledSp(), fontWeight = FontWeight.Bold, color = TextMain)
                        }
                        VerticalDivider(modifier = Modifier.height(30.dp), thickness = 1.dp, color = Color.LightGray)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("組間休息", fontSize = 14.scaledSp(), color = TextSub)
                            Text(exercise.restTime, fontSize = 18.scaledSp(), fontWeight = FontWeight.Bold, color = TextMain)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    ScaleButton(
                        onClick = onStartClick,
                        text = "現在開始訓練"
                    )
                }
            }
        }

        ExerciseStatus.LOCKED -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(35.dp),
                            tint = Color.LightGray
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = exercise.name,
                            fontSize = 24.scaledSp(),
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                        Text(
                            text = "完成前一項任務解鎖",
                            fontSize = 16.scaledSp(),
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskTag(text: String, containerColor: Color, contentColor: Color) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 14.scaledSp(),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyStateView(onNavigateToSurvey: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Assignment,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = PrimaryPeach.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "尚未制定訓練計畫",
            fontSize = 28.scaledSp(),
            fontWeight = FontWeight.Bold,
            color = TextMain
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "請先完成簡易體能狀況量表，我們將根據您的狀況安排專屬任務。",
            fontSize = 18.scaledSp(),
            color = TextSub,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        ScaleButton(
            onClick = onNavigateToSurvey,
            text = "立即進行體能評估"
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun AssignmentScreenPreview() {
    GraduationProjectTheme {
        AssignmentScreen(userLevel = "", isSurveyComplete = false)
    }
}

package com.example.graduationproject.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.ui.components.ScaleButton
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import com.example.graduationproject.ui.theme.scaledSp
import kotlinx.coroutines.launch

// 延續 HomeScreen 的色調
private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val SecondaryTeal = Color(0xFF4DB6AC)
private val TextMain = Color(0xFF201A18)
private val TextSub = Color(0xFF5D5D5D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(
    isSurveyComplete: Boolean = false,
    onNavigateToSurvey: () -> Unit = {}
) {
    // 4. 狀態管理：使用 isBannerVisible 控制通知橫幅
    var isBannerVisible by remember { mutableStateOf(isSurveyComplete) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "訓練手冊 - 第 1 週",
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
            // 4. 動態顯示與動畫效果
            AnimatedVisibility(
                visible = isBannerVisible,
                enter = expandVertically(),
                exit = shrinkVertically() + fadeOut()
            ) {
                // 2. 視覺優化：顏色模仿 CommunityScreen 的 MyRankHeader 樣式
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp), // 模仿 MyRankHeader 的圓角
                    border = BorderStroke(2.dp, SecondaryTeal.copy(alpha = 0.3f)), // 模仿 MyRankHeader 的邊框
                    colors = CardDefaults.cardColors(
                        containerColor = SecondaryTeal.copy(alpha = 0.1f), // 模仿 MyRankHeader 的背景色
                        contentColor = SecondaryTeal // 模仿 MyRankHeader 的主文字顏色
                    )
                ) {
                    // 1. 文案精簡與排版
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎉 評估完成！已為您配發專屬訓練任務。",
                            fontSize = 18.scaledSp(),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        // 3. 無障礙設計
                        IconButton(
                            onClick = { isBannerVisible = false }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "關閉通知"
                            )
                        }
                    }
                }
            }

            if (!isSurveyComplete) {
                // 空狀態畫面
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AssignmentLate,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = TextSub.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "尚無派發訓練任務",
                        fontSize = 24.scaledSp(),
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "請先完成簡易體能狀況量表\n以便為您安排專屬任務",
                        fontSize = 18.scaledSp(),
                        color = TextSub,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    FilledTonalButton(
                        onClick = onNavigateToSurvey,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = PrimaryPeach.copy(alpha = 0.1f),
                            contentColor = PrimaryPeach
                        )
                    ) {
                        Text(
                            text = "前往填寫簡易體能量表",
                            fontSize = 20.scaledSp(),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // 原本的任務列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item { TaskCompletedCard() }
                    item { TaskTodayCard() }
                    item { TaskLockedCard() }
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
            fontSize = 14.scaledSp(),
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TaskCompletedCard() {
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
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = null,
                    modifier = Modifier.size(45.dp),
                    tint = TextMain.copy(alpha = 0.3f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TaskTag("5 mins", Color.White.copy(alpha = 0.6f), TextSub)
                    TaskTag("輕鬆", Color.White.copy(alpha = 0.6f), TextSub)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "基礎呼吸練習",
                    fontSize = 24.scaledSp(),
                    fontWeight = FontWeight.Bold,
                    color = TextMain.copy(alpha = 0.5f)
                )
                Text(
                    text = "AI 準確率：92%",
                    fontSize = 16.scaledSp(),
                    fontWeight = FontWeight.Bold,
                    color = SecondaryTeal.copy(alpha = 0.7f)
                )
            }
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color(0xFFE8F5E9)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已完成",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
fun TaskTodayCard() {
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
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = PrimaryPeach
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Row {
                        TaskTag("15 mins", Color(0xFFFFF3E0), PrimaryPeach)
                        TaskTag("中等", Color(0xFFE3F2FD), Color(0xFF1976D2))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "核心平衡訓練",
                        fontSize = 28.scaledSp(),
                        fontWeight = FontWeight.ExtraBold,
                        color = TextMain
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 使用具備縮放與震動反饋的 ScaleButton
            ScaleButton(
                onClick = { /* 開始任務 */ },
                text = "現在開始訓練",
                icon = Icons.Default.PlayArrow,
                fontSize = 22.scaledSp(),
                containerColor = PrimaryPeach,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun TaskLockedCard() {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.5.dp, TextMain.copy(alpha = 0.15f)),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(TextMain.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "未解鎖",
                    tint = TextMain.copy(alpha = 0.3f),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    TaskTag("20 mins", Color.Transparent, TextMain.copy(alpha = 0.4f))
                    TaskTag("挑戰", Color.Transparent, TextMain.copy(alpha = 0.4f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "進階下肢強化",
                    fontSize = 22.scaledSp(),
                    fontWeight = FontWeight.Bold,
                    color = TextMain.copy(alpha = 0.4f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMain.copy(alpha = 0.2f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentScreenPreview() {
    GraduationProjectTheme {
        AssignmentScreen(isSurveyComplete = true)
    }
}
package com.example.graduationproject.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.DataClass.GetPointsRequest
import com.example.graduationproject.api.ApiClient
import com.example.graduationproject.ui.components.ScaleButton
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import com.example.graduationproject.ui.theme.LocalFontScale
import com.example.graduationproject.ui.theme.scaledSp
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

// MD3 高齡者友善色調
private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val SecondaryTeal = Color(0xFF4DB6AC)
private val StatsPastelBlue = Color(0xFFE3F2FD)
private val StatsPastelOrange = Color(0xFFFFF3E0)
private val TextMain = Color(0xFF201A18)
private val TextSub = Color(0xFF5D5D5D)

/**
 * 修改處：新增通知類型與資料類別，timeText 改為 createdAtMillis
 */
enum class NotificationType {
    SURVEY,
    TRAINING,
    POINTS,
    SOCIAL,
    SYSTEM
}

data class HomeNotification(
    val id: Int,
    val title: String,
    val message: String,
    val createdAtMillis: Long,
    val type: NotificationType,
    val isRead: Boolean = false
)

/**
 * 依據時間戳記回傳相對時間文字
 */
private fun getRelativeTimeText(createdAtMillis: Long): String {
    val now = System.currentTimeMillis()
    val diffMillis = now - createdAtMillis
    val diffMinutes = diffMillis / (60 * 1000)
    val diffHours = diffMinutes / 60
    val diffDays = diffHours / 24

    return when {
        diffMinutes < 1 -> "剛剛"
        diffMinutes < 60 -> "${diffMinutes} 分鐘前"
        diffHours < 24 -> "${diffHours} 小時前"
        diffDays < 7 -> "${diffDays} 天前"
        else -> "較早之前"
    }
}

/**
 * 依據當前小時回傳對應的問候語
 */
private fun getGreetingText(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..10 -> "早安"
        in 11..16 -> "午安"
        else -> "晚安"
    }
}

/**
 * 依據 SPPB 分數判斷體能狀態文字
 */
private fun getFitnessStatusText(sppbScore: Int?): String {
    return when (sppbScore) {
        null -> "尚未評估"
        in 0..3 -> "照護輔助"
        in 4..6 -> "循序恢復"
        in 7..9 -> "穩定提升"
        in 10..12 -> "良好維持"
        else -> "尚未評估"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderlyDashboard(
    accountId: Int,
    isSurveyComplete: Boolean = false,
    userLevel: String = "A",
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSurvey: () -> Unit = {},
    onStartTraining: (String?) -> Unit = {}
) {
    var elderName by remember { mutableStateOf("長輩") }
    var elderLevel by remember { mutableIntStateOf(1) }
    var elderGrade by remember { mutableStateOf("A") }
    var currentPoints by remember { mutableIntStateOf(0) }
    var streakDays by remember { mutableIntStateOf(0) }
    var currentWeek by remember { mutableIntStateOf(1) }
    var sppbScore by remember { mutableStateOf<Int?>(null) } 
    var selectedItem by remember { mutableIntStateOf(0) }
    var localIsSurveyComplete by remember(isSurveyComplete) { mutableStateOf(isSurveyComplete) }

    // 通知狀態管理與標記已讀邏輯
    var showNotificationScreen by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf<List<HomeNotification>>(emptyList()) }

    fun markNotificationAsRead(id: Int) {
        notifications = notifications.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    // 依據評估狀態初始化通知內容
    LaunchedEffect(localIsSurveyComplete) {
        val now = System.currentTimeMillis()
        notifications = if (!localIsSurveyComplete) {
            listOf(
                HomeNotification(
                    id = 1,
                    title = "體能評估提醒",
                    message = "請先完成體能評估，以便為您安排專屬任務。",
                    createdAtMillis = now,
                    type = NotificationType.SURVEY,
                    isRead = false
                )
            )
        } else {
            listOf(
                HomeNotification(
                    id = 1,
                    title = "今日訓練提醒",
                    message = "今天還有訓練任務尚未完成，記得開始今日訓練。",
                    createdAtMillis = now,
                    type = NotificationType.TRAINING,
                    isRead = false
                ),
                HomeNotification(
                    id = 2,
                    title = "點數獎勵提醒",
                    message = "完成訓練可獲得點數，累積後可兌換獎勵。",
                    createdAtMillis = now - (60 * 60 * 1000), 
                    type = NotificationType.POINTS,
                    isRead = false
                )
            )
        }
    }

    LaunchedEffect(accountId, selectedItem, isSurveyComplete) {
        if (accountId <= 0 || selectedItem != 0) return@LaunchedEffect

        try {
            val request = GetPointsRequest(accountId = accountId)
            val response = ApiClient.apiService.getElderDashboardData(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!
                elderName = data.name
                elderLevel = data.level
                elderGrade = data.grade
                currentPoints = data.points
                streakDays = data.streakDays
                currentWeek = data.currentWeek
                sppbScore = data.sppbScore
                localIsSurveyComplete = data.grade.isNotEmpty()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val items = listOf("首頁", "任務集", "社群", "獎勵")
    val icons = listOf(
        Icons.Default.Home,
        Icons.AutoMirrored.Filled.Assignment,
        Icons.Default.Groups,
        Icons.Default.EmojiEvents
    )

    if (showNotificationScreen) {
        NotificationScreen(
            notifications = notifications,
            onBack = { showNotificationScreen = false },
            onAction = { notification ->
                markNotificationAsRead(notification.id)
                showNotificationScreen = false
                when (notification.type) {
                    NotificationType.SURVEY -> onNavigateToSurvey()
                    NotificationType.TRAINING -> selectedItem = 1
                    NotificationType.POINTS -> selectedItem = 3
                    else -> {}
                }
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = BeigeBg,
            topBar = {
                if (selectedItem == 0) {
                    CenterAlignedTopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(onClick = onNavigateToSettings) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = "設定", modifier = Modifier.size(32.dp), tint = TextMain)
                            }
                        },
                        actions = {
                            // 修改處：優化通知圖示與 Badge 位置，解決裁切問題
                            val unreadCount = notifications.count { !it.isRead }
                            Box(
                                //modifier = Modifier.padding(end = 12.dp)
                            ) {
                                IconButton(
                                    onClick = { showNotificationScreen = true },
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(44.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "通知中心",
                                            modifier = Modifier.size(32.dp),
                                            tint = TextMain
                                        )

                                        if (unreadCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = (-6).dp, y = 2.dp)
                                                    .widthIn(min = 20.dp)
                                                    .height(20.dp)
                                                    .clip(CircleShape)
                                                    .background(PrimaryPeach)
                                                    .padding(horizontal = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                                    fontSize = 11.scaledSp(),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                }
            },
            floatingActionButton = {
                if (selectedItem == 0) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = if (localIsSurveyComplete) "💡 預計 15 分鐘，請準備一張穩固的椅子" else "💡 請先完成評估，以便為您安排專屬任務",
                                fontSize = 16.scaledSp(), fontWeight = FontWeight.Bold, color = PrimaryPeach, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        ScaleButton(
                            onClick = {
                                if (localIsSurveyComplete) selectedItem = 1 else onNavigateToSurvey()
                            },
                            text = if (localIsSurveyComplete) "🏃 開始今日訓練" else "前往填寫體能量表問卷",
                            modifier = Modifier.fillMaxWidth(0.9f).height(80.dp),
                            fontSize = if (localIsSurveyComplete) 24.sp else 22.sp,
                            shape = RoundedCornerShape(40.dp),
                            containerColor = PrimaryPeach,
                            icon = if (localIsSurveyComplete) null else Icons.Default.Lock
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            bottomBar = {
                ElderlyNavigationBar(selectedItem, items, icons) { selectedItem = it }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedItem) {
                    0 -> DashboardContent(
                        elderName = elderName,
                        elderLevel = elderLevel,
                        currentPoints = currentPoints,
                        streakDays = streakDays,
                        currentWeek = currentWeek,
                        isSurveyComplete = localIsSurveyComplete,
                        sppbScore = sppbScore,
                        onNavigateToSurvey = onNavigateToSurvey
                    )
                    1 -> AssignmentScreen(
                        userLevel = elderGrade,
                        isSurveyComplete = localIsSurveyComplete,
                        onNavigateToSurvey = onNavigateToSurvey,
                        onStartTraining = onStartTraining
                    )
                    2 -> CommunityScreen(accountId = accountId)
                    3 -> RewardScreen(accountId = accountId, currentPoints = currentPoints, onPointsUpdated = { currentPoints = it })
                }
            }
        }
    }
}

/**
 * 通知頁面組件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    notifications: List<HomeNotification>,
    onBack: () -> Unit,
    onAction: (HomeNotification) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BeigeBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "通知中心",
                        fontSize = 24.scaledSp(),
                        fontWeight = FontWeight.ExtraBold,
                        color = TextMain
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            modifier = Modifier.size(32.dp),
                            tint = TextMain
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = TextSub.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "目前沒有通知",
                    fontSize = 28.scaledSp(),
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    text = "完成訓練或收到提醒後，通知會顯示在這裡。",
                    fontSize = 18.scaledSp(),
                    color = TextSub,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onActionClick = { onAction(notification) }
                    )
                }
            }
        }
    }
}

/**
 * 修正卡片點擊問題。
 * 1. 移除 Surface 上的 clickable，確保點擊空白處不會觸發動作。
 * 2. 只有底部的 Button 使用 onActionClick 進行觸發。
 */
@Composable
fun NotificationCard(
    notification: HomeNotification,
    onActionClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconInfo = when (notification.type) {
                    NotificationType.SURVEY -> Icons.AutoMirrored.Filled.Assignment to PrimaryPeach
                    NotificationType.TRAINING -> Icons.Default.FitnessCenter to SecondaryTeal
                    NotificationType.POINTS -> Icons.Default.MonetizationOn to PrimaryPeach
                    NotificationType.SOCIAL -> Icons.Default.Groups to SecondaryTeal
                    NotificationType.SYSTEM -> Icons.Default.Info to Color.Gray
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(iconInfo.second.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconInfo.first,
                        contentDescription = null,
                        tint = iconInfo.second,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = notification.title,
                            fontSize = 22.scaledSp(),
                            fontWeight = FontWeight.ExtraBold,
                            color = TextMain
                        )
                        if (!notification.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPeach)
                            )
                        }
                    }
                    Text(
                        text = getRelativeTimeText(notification.createdAtMillis),
                        fontSize = 14.scaledSp(),
                        color = TextSub
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = notification.message,
                fontSize = 18.scaledSp(),
                color = TextMain,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (notification.type == NotificationType.TRAINING) SecondaryTeal else PrimaryPeach
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = when (notification.type) {
                        NotificationType.SURVEY -> "前往評估"
                        NotificationType.TRAINING -> "開始訓練"
                        NotificationType.POINTS -> "查看獎勵"
                        else -> "查看詳情"
                    },
                    fontSize = 20.scaledSp(),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DashboardContent(
    elderName: String,
    elderLevel: Int,
    currentPoints: Int,
    streakDays: Int,
    currentWeek: Int,
    isSurveyComplete: Boolean,
    sppbScore: Int?, 
    onNavigateToSurvey: () -> Unit
) {
    var greetingText by remember { mutableStateOf(getGreetingText()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            greetingText = getGreetingText()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp)
    ) {
        item {
            Column {
                Text(
                    text = "${greetingText}，${elderName}！",
                    fontSize = 32.scaledSp(),
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMain
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Surface(
                        color = SecondaryTeal,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))

                            val levelTitle = when(elderLevel) {
                                1 -> "新手長青"
                                2 -> "健康長青"
                                3 -> "活力長青"
                                else -> "運動達人"
                            }
                            Text(
                                text = "Lv.$elderLevel $levelTitle",
                                fontSize = 18.scaledSp(), fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "體能狀態：${getFitnessStatusText(sppbScore)}",
                            fontSize = 18.scaledSp(), fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            StatsFilledCardsRow(streakDays = streakDays, currentPoints = currentPoints)
            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "目前進度：第 $currentWeek 週",
                        fontSize = 18.scaledSp(), fontWeight = FontWeight.Bold, color = SecondaryTeal
                    )
                    Text(
                        text = "共 12 週",
                        fontSize = 18.scaledSp(), color = TextSub
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { currentWeek.toFloat() / 12f },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                    color = SecondaryTeal,
                    trackColor = SecondaryTeal.copy(alpha = 0.2f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            DigitalTwinElevatedCard()
        }
    }
}

@Composable
fun StatsFilledCardsRow(streakDays: Int, currentPoints: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilledCard(
            modifier = Modifier.weight(1f),
            label = "連續訓練",
            value = streakDays.toString(),
            unit = "天",
            containerColor = StatsPastelBlue,
            icon = Icons.Default.Whatshot,
            iconColor = Color(0xFF1976D2)
        )
        FilledCard(
            modifier = Modifier.weight(1f),
            label = "累積點數",
            value = currentPoints.toString(),
            unit = "P",
            containerColor = StatsPastelOrange,
            icon = Icons.Default.MonetizationOn,
            iconColor = Color(0xFFF57C00)
        )
    }
}

@Composable
fun HealthRadarChart(modifier: Modifier = Modifier) {
    val labels = listOf("力量", "平衡", "靈活", "耐力", "速度")
    val data = listOf(0.8f, 0.7f, 0.9f, 0.6f, 0.75f)
    val textMeasurer = rememberTextMeasurer()
    val fontScale = LocalFontScale.current

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = size.minDimension / 2 * 0.7f

        for (i in 1..3) {
            val radius = maxRadius * (i / 3f)
            val path = Path()
            for (j in 0 until 5) {
                val angle = Math.toRadians(j * 72.0 - 90.0).toFloat()
                val x = centerX + radius * cos(angle)
                val y = centerY + radius * sin(angle)
                if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = Color.LightGray.copy(alpha = 0.5f), style = Stroke(width = 2f))
        }

        labels.forEachIndexed { j, label ->
            val angle = Math.toRadians(j * 72.0 - 90.0).toFloat()
            val x = centerX + maxRadius * cos(angle)
            val y = centerY + maxRadius * sin(angle)

            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(centerX, centerY),
                end = Offset(x, y),
                strokeWidth = 2f
            )

            val labelRadius = maxRadius + 24.dp.toPx()
            val labelX = centerX + labelRadius * cos(angle)
            val labelY = centerY + labelRadius * sin(angle)

            val textLayoutResult = textMeasurer.measure(
                text = label,
                style = TextStyle(
                    fontSize = (12 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
            )

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(labelX - textLayoutResult.size.width / 2, labelY - textLayoutResult.size.height / 2)
            )
        }

        val dataPath = Path()
        for (j in 0 until 5) {
            val radius = maxRadius * data[j]
            val angle = Math.toRadians(j * 72.0 - 90.0).toFloat()
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            if (j == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()

        drawPath(dataPath, color = SecondaryTeal.copy(alpha = 0.3f))
        drawPath(dataPath, color = SecondaryTeal, style = Stroke(width = 6f))

        for (j in 0 until 5) {
            val radius = maxRadius * data[j]
            val angle = Math.toRadians(j * 72.0 - 90.0).toFloat()
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            drawCircle(color = SecondaryTeal, radius = 6f, center = Offset(x, y))
        }
    }
}

@Composable
fun DigitalTwinElevatedCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().heightIn(min = 280.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            HealthRadarChart(modifier = Modifier.size(240.dp))
        }
    }
}

@Composable
fun FilledCard(
    modifier: Modifier,
    label: String,
    value: String,
    unit: String,
    containerColor: Color,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        modifier = modifier.heightIn(min = 140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = label,
                    fontSize = 16.scaledSp(),
                    color = TextSub,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        fontSize = 28.scaledSp(),
                        fontWeight = FontWeight.ExtraBold,
                        color = TextMain
                    )
                    Text(
                        text = " $unit",
                        fontSize = 16.scaledSp(),
                        color = TextSub,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ElderlyNavigationBar(
    selectedItem: Int,
    items: List<String>,
    icons: List<ImageVector>,
    onItemSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = item,
                        modifier = Modifier.size(32.dp)
                    )
                },
                label = {
                    Text(
                        text = item,
                        fontSize = 14.scaledSp(),
                        fontWeight = if (selectedItem == index) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = selectedItem == index,
                onClick = { onItemSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryPeach,
                    selectedTextColor = PrimaryPeach,
                    indicatorColor = PrimaryPeach.copy(alpha = 0.15f),
                    unselectedIconColor = TextMain.copy(alpha = 0.5f),
                    unselectedTextColor = TextMain.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun DashboardPreview() {
    GraduationProjectTheme {
        ElderlyDashboard(accountId = 1)
    }
}

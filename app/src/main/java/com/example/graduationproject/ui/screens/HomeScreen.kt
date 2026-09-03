package com.example.graduationproject.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.DataClass.GetFitnessRadarRequest
import com.example.graduationproject.DataClass.GetPointsRequest
import com.example.graduationproject.api.ApiClient
import com.example.graduationproject.fitness.FitnessCategory
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

private fun getGreetingText(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..10 -> "早安"
        in 11..16 -> "午安"
        else -> "晚安"
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
    var selectedItem by remember { mutableIntStateOf(0) }
    var localIsSurveyComplete by remember(isSurveyComplete) { mutableStateOf(isSurveyComplete) }

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
                        IconButton(onClick = { /* TODO */ }) {
                            BadgedBox(badge = { Badge { Text(text = "3", fontSize = 10.scaledSp()) } }) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = "通知中心", modifier = Modifier.size(32.dp), tint = TextMain)
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
                    accountId = accountId,
                    elderName = elderName,
                    elderLevel = elderLevel,
                    currentPoints = currentPoints,
                    streakDays = streakDays,
                    currentWeek = currentWeek,
                    isSurveyComplete = localIsSurveyComplete,
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

@Composable
fun DashboardContent(
    accountId: Int,
    elderName: String,
    elderLevel: Int,
    currentPoints: Int,
    streakDays: Int,
    currentWeek: Int,
    isSurveyComplete: Boolean,
    onNavigateToSurvey: () -> Unit
) {
    // 修改處：新增問候語狀態，並使用 LaunchedEffect 搭配 delay 定期更新
    var greetingText by remember { mutableStateOf(getGreetingText()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60000) // 每分鐘檢查並更新一次問候語
            greetingText = getGreetingText()
        }
    }

    var radarProgress by remember { mutableStateOf<Map<FitnessCategory, Float>>(emptyMap()) }

    LaunchedEffect(accountId) {
        if (accountId <= 0) return@LaunchedEffect
        try {
            val response = ApiClient.apiService.getFitnessRadar(GetFitnessRadarRequest(accountId))
            if (response.isSuccessful && response.body()?.success == true) {
                val raw = response.body()?.radar ?: emptyMap()
                radarProgress = FitnessCategory.entries.associateWith { category ->
                    (raw[category.apiKey] ?: 0f) / 100f
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp)
    ) {
        item {
            Column {
                // 修改處：將原本寫死的「早安」改為動態的 greetingText
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
                            text = "健康狀態：優良",
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
            DigitalTwinElevatedCard(progress = radarProgress)
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
fun HealthRadarChart(
    modifier: Modifier = Modifier,
    progress: Map<FitnessCategory, Float> = emptyMap()
) {
    val orderedCategories = FitnessCategory.entries
    val labels = orderedCategories.map { it.label }
    val data = orderedCategories.map { progress[it] ?: 0f }
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
fun DigitalTwinElevatedCard(progress: Map<FitnessCategory, Float> = emptyMap()) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            HealthRadarChart(modifier = Modifier.size(240.dp), progress = progress)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
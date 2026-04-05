package com.example.graduationproject.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.ui.theme.GraduationProjectTheme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderlyDashboard(accountId: Int) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("首頁", "任務集", "社群", "獎勵")
    val icons = listOf(
        Icons.Default.Home,
        Icons.AutoMirrored.Filled.Assignment,
        Icons.Default.Groups,
        Icons.Default.EmojiEvents
    )
    RewardScreen(accountId = accountId)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BeigeBg,
        topBar = {
            if (selectedItem == 0) {
                CenterAlignedTopAppBar(
                    title = { },
                    actions = {
                        IconButton(onClick = { /* TODO: 通知中心 */ }) {
                            BadgedBox(
                                badge = { Badge { Text("3") } }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "通知中心",
                                    modifier = Modifier.size(32.dp),
                                    tint = TextMain
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        floatingActionButton = {
            if (selectedItem == 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "💡 預計 15 分鐘，請準備一張穩固的椅子",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPeach,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    StartTrainingFAB()
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
                0 -> DashboardContent()
                1 -> AssignmentScreen()
                2 -> CommunityScreen()
                3 -> RewardScreen(accountId = accountId)
            }
        }
    }
}

@Composable
fun DashboardContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp)
    ) {
        item {
            Text(
                text = "早安，陳爺爺！",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextMain
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "目前進度：第 2 週",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryTeal
                    )
                    Text(
                        text = "共 12 週",
                        fontSize = 16.sp,
                        color = TextSub
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 2f / 12f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = SecondaryTeal,
                    trackColor = SecondaryTeal.copy(alpha = 0.2f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            DigitalTwinElevatedCard()
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            StatsFilledCardsRow()
        }
    }
}

@Composable
fun HealthRadarChart(modifier: Modifier = Modifier) {
    val labels = listOf("力量", "平衡", "靈活", "耐力", "速度")
    val data = listOf(0.8f, 0.7f, 0.9f, 0.6f, 0.75f) 
    val textMeasurer = rememberTextMeasurer()
    
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = size.minDimension / 2 * 0.7f 
        
        // 1. 背景網格 (3層五邊形)
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
        
        // 2. 軸線與標籤
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
            
            // 標籤文字定位
            val labelRadius = maxRadius + 24.dp.toPx()
            val labelX = centerX + labelRadius * cos(angle)
            val labelY = centerY + labelRadius * sin(angle)
            
            val textLayoutResult = textMeasurer.measure(
                text = label,
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMain)
            )
            
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(labelX - textLayoutResult.size.width / 2, labelY - textLayoutResult.size.height / 2)
            )
        }
        
        // 3. 數據區域 (SecondaryTeal)
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
        
        // 4. 頂點小圓點
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
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側區域 (雷達圖) - 靠左並給予足夠空間
            Box(
                modifier = Modifier
                    .weight(1.4f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                HealthRadarChart(modifier = Modifier.size(210.dp))
            }

            // 右側區域 (狀態與勳章) - 靠右上對齊
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 12.dp, end = 8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "健康狀態：優良",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Visible
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    color = SecondaryTeal,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Lv.3 活力長青",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Visible
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsFilledCardsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilledCard(
            modifier = Modifier.weight(1f),
            label = "連續訓練",
            value = "12",
            unit = "天",
            containerColor = StatsPastelBlue,
            icon = Icons.Default.Whatshot,
            iconColor = Color(0xFF1976D2)
        )
        FilledCard(
            modifier = Modifier.weight(1f),
            label = "累積點數",
            value = "850",
            unit = "P",
            containerColor = StatsPastelOrange,
            icon = Icons.Default.MonetizationOn,
            iconColor = Color(0xFFF57C00)
        )
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
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    fontSize = 16.sp, 
                    color = TextSub, 
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextMain
                    )
                    Text(
                        text = " $unit",
                        fontSize = 16.sp,
                        color = TextSub,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StartTrainingFAB() {
    ExtendedFloatingActionButton(
        onClick = { /* TODO */ },
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(80.dp),
        containerColor = PrimaryPeach,
        contentColor = Color.White,
        shape = RoundedCornerShape(40.dp),
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🏃‍♂️", fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "開始今日訓練",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
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
        tonalElevation = 8.dp,
        modifier = Modifier.height(100.dp)
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = item,
                        modifier = Modifier.size(32.dp) // 放大圖示
                    )
                },
                label = {
                    Text(
                        text = item,
                        fontSize = 14.sp,
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

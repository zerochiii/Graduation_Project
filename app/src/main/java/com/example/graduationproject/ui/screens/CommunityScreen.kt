package com.example.graduationproject.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import java.util.Calendar

// 延續專案色調
private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val SecondaryTeal = Color(0xFF4DB6AC)
private val TextMain = Color(0xFF201A18)
private val TextSub = Color(0xFF5D5D5D)

data class CommunityUser(
    val name: String,
    val level: String,
    val avatarColor: Color,
    val weeklyExercise: Int,
    val weeklyExp: Int, // 原 todaySteps 改為 weeklyExp
    val initialLikes: Int,
    val rank: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("社區排行榜", "我的好友")

    val leaderboardUsers = listOf(
        CommunityUser("張奶奶", "Lv.12 訓練達人", Color(0xFFFFCDD2), 5, 2150, 42, 1),
        CommunityUser("李爺爺", "Lv.10 復健宗師", Color(0xFFC8E6C9), 4, 1980, 38, 2),
        CommunityUser("王大叔", "Lv.8 活力楷模", Color(0xFFBBDEFB), 6, 1750, 25, 3),
        CommunityUser("林阿姨", "Lv.7 全能長青樹", Color(0xFFF0F4C3), 3, 1520, 19, 4)
    )

    val currentUser = CommunityUser("陳爺爺", "Lv.3 活力長青", PrimaryPeach.copy(alpha = 0.2f), 2, 1250, 12, 15)

    // 動態計算本週日期範圍 (週一到週日)，相容 API 24
    val dateRangeStr = remember {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        // 往前找最近的一個週一
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, -1)
        }
        val startMonth = calendar.get(Calendar.MONTH) + 1
        val startDay = calendar.get(Calendar.DAY_OF_MONTH)

        // 週一加六天即為週日
        calendar.add(Calendar.DATE, 6)
        val endMonth = calendar.get(Calendar.MONTH) + 1
        val endDay = calendar.get(Calendar.DAY_OF_MONTH)

        "$startMonth/$startDay - $endMonth/$endDay"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeBg)
    ) {
        // 增加 TabRow 高度，並讓文字有更大的點擊面積
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = BeigeBg,
            contentColor = PrimaryPeach,
            modifier = Modifier.height(72.dp),
            indicator = { TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                width = 80.dp,
                color = PrimaryPeach
            )}
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTabIndex = index },
                    modifier = Modifier.fillMaxHeight(),
                    text = {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PrimaryPeach else TextSub
                        )
                    }
                )
            }
        }

        // 固定顯示「我的排名」與週期提示
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "社區排行榜",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    text = "本週排行 ($dateRangeStr)",
                    fontSize = 14.sp,
                    color = TextSub
                )
            }
            Text(
                text = "排行榜將於週日 23:59 結算",
                fontSize = 12.sp,
                color = TextSub.copy(alpha = 0.7f)
            )
        }

        MyRankHeader(user = currentUser)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(leaderboardUsers) { user ->
                CommunityUserCard(user)
            }
        }
    }
}

@Composable
fun MyRankHeader(user: CommunityUser) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        color = SecondaryTeal.copy(alpha = 0.1f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, SecondaryTeal.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "第 ${user.rank} 名",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SecondaryTeal
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "本週累積表現", fontSize = 14.sp, color = TextSub)
                    Text(text = user.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextMain)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    /*Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = SecondaryTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))*/
                    Text(
                        text = "${user.weeklyExp} 經驗值",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = SecondaryTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 超越提示
            Surface(
                color = Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "💡 再獲得 50 經驗值即可超越上一名！",
                    fontSize = 13.sp,
                    color = TextSub,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityUserCard(user: CommunityUser) {
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableIntStateOf(user.initialLikes) }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("關閉", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text(text = "${user.name} 的訓練成就", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFD700), // 金色獎盃
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "本週運動：${user.weeklyExercise} 次",
                            fontSize = 20.sp,
                            color = TextMain
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = PrimaryPeach,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "本週經驗值：${user.weeklyExp} 經驗值",
                            fontSize = 20.sp,
                            color = TextMain
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = PrimaryPeach,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "收到愛心：$likesCount 個",
                            fontSize = 20.sp,
                            color = TextMain
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    OutlinedCard(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder(enabled = true).copy(width = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 排名顯示
            Text(
                text = user.rank.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = if (user.rank <= 3) PrimaryPeach else TextSub.copy(alpha = 0.3f),
                modifier = Modifier.width(36.dp)
            )

            // 頭像
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(user.avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SentimentVerySatisfied, null, modifier = Modifier.size(36.dp), tint = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextMain)
                Text(text = user.level, fontSize = 14.sp, color = SecondaryTeal, fontWeight = FontWeight.Medium)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = PrimaryPeach,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${user.weeklyExp} 經驗值",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryPeach
                    )
                }
            }

            // 愛心按鈕視覺層級調淡
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        if (isLiked) likesCount++ else likesCount--
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "送愛心",
                        tint = if (isLiked) PrimaryPeach else TextSub.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = likesCount.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isLiked) PrimaryPeach else TextSub.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun CommunityScreenPreview() {
    GraduationProjectTheme {
        CommunityScreen()
    }
}
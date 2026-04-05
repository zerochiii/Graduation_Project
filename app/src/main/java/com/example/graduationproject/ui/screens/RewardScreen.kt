package com.example.graduationproject.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.DataClass.GetPointsRequest
import com.example.graduationproject.api.ApiClient
import com.example.graduationproject.DataClass.RedeemRequest
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import kotlinx.coroutines.launch

// 延續專案色調
private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val SecondaryTeal = Color(0xFF4DB6AC)
private val TextMain = Color(0xFF201A18)
private val TextSub = Color(0xFF5D5D5D)
private val PositiveGreen = Color(0xFF4CAF50)
private val NegativeRed = Color(0xFFE57373)

data class RewardItem(
    val id: Int,
    val name: String,
    val points: Int,
    val icon: ImageVector,
    val iconColor: Color
)

data class PointRecord(
    val title: String,
    val points: Int,
    val date: String,
    val isEarning: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardScreen(accountId: Int ) {
    var currentPoints by remember { mutableIntStateOf(0) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var redeemingId by remember { mutableStateOf<Int?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(accountId) {
        if (accountId <= 0) return@LaunchedEffect

        try {
            val request = GetPointsRequest(accountId = accountId)
            val response = ApiClient.apiService.getPoints(request)

            if (response.isSuccessful && response.body()?.success == true) {
                currentPoints = response.body()?.points ?: 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val rewards = listOf(
        RewardItem(1, "運動排汗衫", 500, Icons.Default.Checkroom, Color(0xFF64B5F6)),
        RewardItem(2, "綜合維他命", 1200, Icons.Default.LocalPharmacy, Color(0xFF81C784)),
        RewardItem(3, "時尚遮陽帽", 300, Icons.Default.Checkroom, Color(0xFFFFB74D)),
        RewardItem(4, "健康按摩球", 800, Icons.Default.CardGiftcard, Color(0xFFBA68C8))
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BeigeBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // 頂部導航區域：標題與歷史紀錄按鈕
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showBottomSheet = true },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "點數明細",
                        tint = TextMain
                    )
                }
            }

            // 頂部：金幣插畫與點數顯示
            RewardHeader(currentPoints = currentPoints)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "可兌換獎勵",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 雙欄位網格列表
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(rewards) { item ->
                    RewardCard(
                        item = item,
                        currentPoints = currentPoints,
                        isLoading = redeemingId == item.id,
                        onRedeemClick = {
                            coroutineScope.launch {
                                redeemingId = item.id
                                try {
                                    val request = RedeemRequest(account_id = accountId, reward_id = item.id)
                                    val response = ApiClient.apiService.redeemReward(request)

                                    if (response.isSuccessful && response.body()?.success == true) {
                                        currentPoints = response.body()?.remaining_points ?: (currentPoints - item.points)
                                        Toast.makeText(context, "兌換成功！", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val errorString = response.errorBody()?.string()
                                        val errorMessage = try {
                                            org.json.JSONObject(errorString!!).getString("message")
                                        } catch (e: Exception) { "兌換失敗" }
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "網路連線失敗", Toast.LENGTH_SHORT).show()
                                } finally {
                                    redeemingId = null
                                }
                            }
                        }
                    )
                }
            }
        }

        // 點數明細 BottomSheet
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = BeigeBg,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                dragHandle = { BottomSheetDefaults.DragHandle(color = TextSub.copy(alpha = 0.3f)) }
            ) {
                PointsDetailContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsDetailContent() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("獲得紀錄", "兌換紀錄")

    val earningRecords = listOf(
        PointRecord("完成每日訓練", 50, "2023.10.27", true),
        PointRecord("每日簽到", 10, "2023.10.27", true),
        PointRecord("上週排名獎勵", 100, "2023.10.26", true),
        PointRecord("邀請好友", 200, "2023.10.25", true)
    )

    val redemptionRecords = listOf(
        PointRecord("兌換運動排汗衫", 500, "2023.10.24", false)
    )

    Column(
        modifier = Modifier
            .fillMaxHeight(0.8f)
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "點數明細",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = PrimaryPeach,
            indicator = { TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                color = PrimaryPeach
            )}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTabIndex == index) PrimaryPeach else TextSub
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            val currentRecords = if (selectedTabIndex == 0) earningRecords else redemptionRecords
            if (currentRecords.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暫無紀錄", fontSize = 18.sp, color = TextSub)
                    }
                }
            } else {
                items(currentRecords) { record ->
                    RecordItem(record)
                }
            }
        }
    }
}

@Composable
fun RecordItem(record: PointRecord) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = record.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    text = record.date,
                    fontSize = 16.sp,
                    color = TextSub
                )
            }
            Text(
                text = "${if (record.isEarning) "+" else "-"}${record.points} P",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (record.isEarning) PositiveGreen else NegativeRed
            )
        }
    }
}

@Composable
fun RewardHeader(currentPoints: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFFFD54F).copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFFFB300)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "累積點數",
            fontSize = 18.sp,
            color = TextSub
        )
        Text(
            text = "$currentPoints P",
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            color = TextMain
        )
    }
}

@Composable
fun RewardCard(item: RewardItem, currentPoints: Int,isLoading: Boolean,onRedeemClick: () -> Unit) {
    val canAfford = currentPoints >= item.points
    val pointsNeeded = item.points - currentPoints

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 扁平化插畫
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(item.iconColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = item.iconColor
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canAfford) TextMain else TextSub.copy(alpha = 0.7f)
                )
                Text(
                    text = "${item.points} P",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (canAfford) PrimaryPeach else TextSub.copy(alpha = 0.5f)
                )

                // 兌換進度/差額提示
                if (!canAfford) {
                    Text(
                        text = "還差 $pointsNeeded P",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE57373),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // 按鈕狀態區隔
            Button(
                onClick = onRedeemClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = canAfford && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPeach,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFEEEEEE),
                    disabledContentColor = TextSub.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = if (canAfford && !isLoading) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = PrimaryPeach, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = if (canAfford) "立即兌換" else "點數不足", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun RewardScreenPreview() {
    GraduationProjectTheme {
        RewardScreen(accountId = 1)
    }
}

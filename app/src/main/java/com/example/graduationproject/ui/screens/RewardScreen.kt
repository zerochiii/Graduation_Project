package com.example.graduationproject.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.google.gson.annotations.SerializedName

private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val SecondaryTeal = Color(0xFF4DB6AC)
private val TextMain = Color(0xFF201A18)
private val TextSub = Color(0xFF5D5D5D)
private val PositiveGreen = Color(0xFF4CAF50)
private val NegativeRed = Color(0xFFE57373)

// 修改處：改成對應資料庫真實商品，imageUrl 為 null 時 UI 會 fallback 成禮物盒圖示
data class RewardItem(
    val id: Int,
    val name: String,
    val points: Int,
    val quantity: Int,
    val imageUrl: String?
)

data class PointRecord(
    val title: String,
    val points: Int,
    val date: String,
    @SerializedName("is_earning") val isEarning: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun RewardScreen(accountId: Int, currentPoints: Int, onPointsUpdated: (Int) -> Unit) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var redeemingId by remember { mutableStateOf<Int?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    // 修改處：改由後端讀取真實商品資料（含圖片），不再寫死
    var rewards by remember { mutableStateOf<List<RewardItem>>(emptyList()) }
    var isLoadingRewards by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoadingRewards = true
        try {
            val response = com.example.graduationproject.api.ApiClient.apiService.getRewards()
            if (response.isSuccessful && response.body()?.success == true) {
                rewards = response.body()?.data?.map { item ->
                    RewardItem(
                        id = item.id,
                        name = item.name,
                        points = item.price,
                        quantity = item.quantity,
                        imageUrl = item.image
                    )
                } ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoadingRewards = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BeigeBg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
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
                    Icon(imageVector = Icons.Default.History, contentDescription = "點數明細", tint = TextMain)
                }
            }

            RewardHeader(currentPoints = currentPoints)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "可兌換獎勵",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoadingRewards) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPeach)
                }
            } else if (rewards.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                    Text("目前沒有可兌換的獎勵", fontSize = 18.sp, color = TextSub)
                }
            } else {
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
                                        val request = com.example.graduationproject.DataClass.RedeemRequest(
                                            account_id = accountId,
                                            reward_id = item.id
                                        )
                                        val response = com.example.graduationproject.api.ApiClient.apiService.redeemReward(request)

                                        if (response.isSuccessful && response.body()?.success == true) {
                                            val newPoints = response.body()?.remaining_points ?: (currentPoints - item.points)
                                            onPointsUpdated(newPoints)

                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("兌換成功！")
                                            }
                                        } else {
                                            val errorMsg = response.body()?.message ?: "兌換失敗"
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(errorMsg)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("網路連線失敗")
                                        }
                                    } finally {
                                        redeemingId = null
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = BeigeBg,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                dragHandle = { BottomSheetDefaults.DragHandle(color = TextSub.copy(alpha = 0.3f)) }
            ) {
                PointsDetailContent(accountId = accountId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsDetailContent(accountId: Int) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("獲得紀錄", "兌換紀錄")

    var earningRecords by remember { mutableStateOf<List<PointRecord>>(emptyList()) }
    var redemptionRecords by remember { mutableStateOf<List<PointRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(accountId) {
        if (accountId <= 0) return@LaunchedEffect
        isLoading = true
        try {
            val request = com.example.graduationproject.DataClass.GetPointHistoryRequest(account_id = accountId)
            val response = com.example.graduationproject.api.ApiClient.apiService.getPointHistory(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val allRecords = response.body()?.records ?: emptyList()
                earningRecords = allRecords.filter { it.isEarning }
                redemptionRecords = allRecords.filter { !it.isEarning }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }
    Column(
        modifier = Modifier
            .fillMaxHeight(0.8f)
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "點數明細",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextMain,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = PrimaryPeach
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = PrimaryPeach,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val currentRecords =
                    if (selectedTabIndex == 0) earningRecords else redemptionRecords

                if (currentRecords.isEmpty()) {
                    Text(
                        text = "暫無紀錄",
                        fontSize = 18.sp,
                        color = TextSub,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        val currentRecords2 =
                            if (selectedTabIndex == 0) earningRecords else redemptionRecords
                        if (currentRecords2.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("暫無紀錄", fontSize = 18.sp, color = TextSub)
                                }
                            }
                        } else {
                            items(currentRecords2) { record ->
                                RecordItem(record)
                            }
                        }
                    }
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
        // 巨大的金幣插畫
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
fun RewardCard(
    item: RewardItem,
    currentPoints: Int,
    isLoading: Boolean,
    onRedeemClick: () -> Unit
) {
    val canAfford = currentPoints >= item.points && item.quantity > 0
    val pointsNeeded = item.points - currentPoints

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(280.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 修改處：優先顯示管理員上傳的真實商品照片，沒有圖片才 fallback 成禮物盒圖示
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryPeach.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUrl != null) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = PrimaryPeach
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.name, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    color = if (canAfford) TextMain else TextSub.copy(alpha = 0.7f)
                )
                Text(
                    text = "${item.points} P", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (canAfford) PrimaryPeach else TextSub.copy(alpha = 0.5f)
                )

                if (item.quantity <= 0) {
                    Text(
                        text = "已兌換完畢", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFFE57373), modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (!canAfford) {
                    Text(
                        text = "還差 $pointsNeeded P", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFFE57373), modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Button(
                onClick = {
                    if (!isLoading) { // 防止重複點擊
                        onRedeemClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPeach,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFEEEEEE),
                    disabledContentColor = TextSub.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = if (canAfford) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = PrimaryPeach, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = when {
                            item.quantity <= 0 -> "已兌換完畢"
                            canAfford -> "立即兌換"
                            else -> "點數不足"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun RewardScreenPreview() {
    GraduationProjectTheme {
        RewardScreen(accountId = 1, currentPoints = 850, onPointsUpdated = {})
    }
}
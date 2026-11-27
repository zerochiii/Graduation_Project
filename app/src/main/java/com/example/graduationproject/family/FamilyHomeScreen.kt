package com.example.graduationproject.family

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.profile.AvatarSource
import com.example.graduationproject.profile.DEFAULT_PROFILE_AVATAR_COLOR_ID
import com.example.graduationproject.profile.DEFAULT_PROFILE_AVATAR_ID
import com.example.graduationproject.ui.components.ProfileAvatar
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import java.text.SimpleDateFormat
import java.util.Locale

internal val FamilyPurple = Color(0xFF5B3F8C)
internal val FamilyPurpleLight = Color(0xFFF1ECFA)
internal val FamilyBackground = Color(0xFFFAF9FC)
internal val FamilyText = Color(0xFF211B2D)
internal val FamilyTextSecondary = Color(0xFF686270)
internal val RiskSafe = Color(0xFF2E7D5B)
private val RiskSafeBackground = Color(0xFFE7F5EE)
internal val RiskWarning = Color(0xFFB53A45)
private val RiskWarningBackground = Color(0xFFFCEBED)
private val RiskUnknown = Color(0xFF6D6674)
private val RiskUnknownBackground = Color(0xFFF0EDF2)

@Composable
fun FamilyHomeScreen(
    accountId: Int,
    displayName: String,
    viewModel: FamilyHomeViewModel,
    refreshRequest: Int = 0,
    onBindElder: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedElderIndex by rememberSaveable(accountId) { mutableStateOf<Int?>(null) }

    LaunchedEffect(accountId) {
        selectedElderIndex = null
        viewModel.bindAccount(accountId)
    }

    LaunchedEffect(refreshRequest) {
        if (refreshRequest > 0) viewModel.refresh()
    }

    val selectedIndex = selectedElderIndex
    if (selectedIndex != null) {
        val elder = (state as? FamilyHomeUiState.Content)?.elders?.getOrNull(selectedIndex)
        if (elder == null) {
            ElderUnavailableScreen(onBack = { selectedElderIndex = null })
        } else {
            ElderDetailScreen(elder = elder, onBack = { selectedElderIndex = null })
        }
        return
    }

    FamilyHomeContent(
        displayName = displayName,
        state = state,
        onRetry = viewModel::retry,
        onOpenElder = { selectedElderIndex = it },
        onBindElder = onBindElder,
        onOpenSettings = onOpenSettings
    )
}

@Composable
internal fun FamilyHomeContent(
    displayName: String,
    state: FamilyHomeUiState,
    onRetry: () -> Unit,
    onOpenElder: (Int) -> Unit,
    onBindElder: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Scaffold(
        containerColor = FamilyBackground,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Rounded.Home, "首頁") },
                    label = { Text("首頁") },
                    colors = familyNavigationColors()
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenSettings,
                    icon = { Icon(Icons.Rounded.Settings, "設定") },
                    label = { Text("設定") },
                    colors = familyNavigationColors()
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            FamilyHeader(onOpenSettings)
            when (state) {
                FamilyHomeUiState.Loading -> FamilyLoading()
                FamilyHomeUiState.Empty -> FamilyEmpty(displayName, onBindElder)
                is FamilyHomeUiState.Error -> FamilyError(onRetry)
                is FamilyHomeUiState.Content -> FamilyElderList(
                    displayName = displayName,
                    elders = state.elders,
                    onOpenElder = onOpenElder,
                    onBindElder = onBindElder
                )
            }
        }
    }
}

@Composable
private fun FamilyHeader(onOpenSettings: () -> Unit) {
    Surface(color = FamilyPurple) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 82.dp).padding(horizontal = 22.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("FitCare", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("家屬專區", color = Color.White.copy(alpha = 0.84f), fontSize = 16.sp)
            }
            IconButton(onClick = onOpenSettings, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.AccountCircle, "開啟帳號設定", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun FamilyElderList(
    displayName: String,
    elders: List<FamilyElderSummary>,
    onOpenElder: (Int) -> Unit,
    onBindElder: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("您好，${displayName.ifBlank { "家屬" }} 👋", color = FamilyText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("一起關心家人的健康與活動狀況。", color = FamilyTextSecondary, fontSize = 16.sp)
        }
        item { BoundElderCountCard(elders.size) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("我的長輩", modifier = Modifier.weight(1f), color = FamilyText, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onBindElder, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Rounded.Add, null)
                    Spacer(Modifier.width(4.dp))
                    Text("新增", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        itemsIndexed(elders) { index, elder ->
            FamilyElderCard(elder = elder, onClick = { onOpenElder(index) })
        }
    }
}

@Composable
private fun BoundElderCountCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = FamilyPurpleLight),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("已綁定長輩", Modifier.weight(1f), fontSize = 18.sp, color = FamilyText)
            Text("$count 位", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = FamilyPurple)
        }
    }
}

@Composable
private fun FamilyElderCard(elder: FamilyElderSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "查看${elder.displayName}的詳細資料" },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    avatarSource = AvatarSource.Preset(DEFAULT_PROFILE_AVATAR_ID),
                    avatarColorId = DEFAULT_PROFILE_AVATAR_COLOR_ID,
                    size = 66.dp,
                    contentDescription = "${elder.displayName}的預設頭像"
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(elder.displayName, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = FamilyText)
                    elder.realName?.let { Spacer(Modifier.height(4.dp)); Text(it, fontSize = 15.sp, color = FamilyTextSecondary) }
                }
                Text("查看", color = FamilyPurple, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AssessmentPill(elder.sppbScore?.let { "SPPB $it / 12" } ?: "尚無評估", Modifier.weight(1f))
                RiskChip(elder.hasFallRisk, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Text("最近評估：${formatAssessmentDate(elder.testDate)}", fontSize = 15.sp, color = FamilyTextSecondary)
        }
    }
}

@Composable
private fun AssessmentPill(text: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = FamilyPurpleLight, shape = RoundedCornerShape(14.dp)) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 10.dp), color = FamilyPurple, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun RiskChip(risk: Boolean?, modifier: Modifier = Modifier) {
    val (label, foreground, background) = when (risk) {
        false -> Triple("跌倒風險：低", RiskSafe, RiskSafeBackground)
        true -> Triple("有跌倒風險", RiskWarning, RiskWarningBackground)
        null -> Triple("風險尚未評估", RiskUnknown, RiskUnknownBackground)
    }
    Surface(modifier, color = background, shape = RoundedCornerShape(14.dp)) {
        Text(label, Modifier.padding(horizontal = 10.dp, vertical = 10.dp), color = foreground, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable private fun FamilyLoading() = StateMessage(null, "正在取得長輩資料…", "請稍候，正在同步家屬照護資訊。", loading = true)
@Composable
private fun FamilyEmpty(displayName: String, onBindElder: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("您好，${displayName.ifBlank { "家屬" }} 👋", color = FamilyText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("一起關心家人的健康與活動狀況。", color = FamilyTextSecondary, fontSize = 16.sp)
        }
        item { BoundElderCountCard(0) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(shape = CircleShape, color = FamilyPurpleLight) {
                        Icon(Icons.Rounded.Info, null, tint = FamilyPurple, modifier = Modifier.padding(16.dp).size(40.dp))
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("目前尚未綁定長輩", fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    Text("尚未有可查看的長輩資料。", fontSize = 16.sp, color = FamilyTextSecondary, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(22.dp))
                    Button(
                        onClick = onBindElder,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FamilyPurple),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Rounded.Key, null)
                        Spacer(Modifier.width(8.dp))
                        Text("輸入綁定碼", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
@Composable private fun FamilyError(onRetry: () -> Unit) = StateMessage(
    Icons.Rounded.ErrorOutline,
    "無法取得長輩資料",
    "目前無法連線至服務，請稍後再試。",
    action = {
        Button(onClick = onRetry, modifier = Modifier.heightIn(min = 52.dp), colors = ButtonDefaults.buttonColors(containerColor = FamilyPurple)) {
            Text("重新整理", fontSize = 17.sp)
        }
    }
)

@Composable
private fun StateMessage(icon: ImageVector?, title: String, message: String, loading: Boolean = false, action: (@Composable () -> Unit)? = null) {
    Column(Modifier.fillMaxSize().padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        when {
            loading -> CircularProgressIndicator(color = FamilyPurple, modifier = Modifier.size(50.dp))
            icon != null -> Surface(shape = CircleShape, color = FamilyPurpleLight) {
                Icon(icon, null, tint = FamilyPurple, modifier = Modifier.padding(16.dp).size(40.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FamilyText, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(message, fontSize = 16.sp, color = FamilyTextSecondary, textAlign = TextAlign.Center)
        action?.let { Spacer(Modifier.height(22.dp)); it() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ElderDetailScreen(elder: FamilyElderSummary, onBack: () -> Unit) {
    var selectedTab by rememberSaveable(elder.displayName, elder.testDate) { mutableIntStateOf(0) }
    val tabs = listOf("總覽", "評估紀錄", "訓練狀況")
    Scaffold(
        containerColor = FamilyBackground,
        topBar = {
            Surface(color = FamilyPurple) {
                Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回家屬首頁", tint = Color.White) }
                    Text("長輩詳細資料", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            ElderProfileSummary(elder)
            PrimaryTabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = FamilyPurple) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, modifier = Modifier.heightIn(min = 54.dp), text = {
                        Text(title, fontSize = 16.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium)
                    })
                }
            }
            when (selectedTab) {
                0 -> ElderOverview(elder)
                1 -> AssessmentHistoryPlaceholder(elder)
                else -> TrainingStatusPlaceholder()
            }
        }
    }
}

@Composable
private fun ElderProfileSummary(elder: FamilyElderSummary) {
    Row(Modifier.fillMaxWidth().background(Color.White).padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
        ProfileAvatar(
            avatarSource = AvatarSource.Preset(DEFAULT_PROFILE_AVATAR_ID),
            avatarColorId = DEFAULT_PROFILE_AVATAR_COLOR_ID,
            size = 76.dp,
            contentDescription = "${elder.displayName}的預設頭像"
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(elder.displayName, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = FamilyText)
            elder.realName?.let { Text(it, fontSize = 16.sp, color = FamilyTextSecondary) }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(0.85f)) { RiskChip(elder.hasFallRisk) }
        }
    }
}

@Composable
private fun ElderOverview(elder: FamilyElderSummary) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { LatestAssessmentCard(elder) }
        item { SectionCard("體能能力分析") { ElderAbilityRadarChart(metrics = null) } }
    }
}

@Composable
private fun LatestAssessmentCard(elder: FamilyElderSummary) {
    SectionCard("最近一次體能評估") {
        if (elder.sppbScore == null) Text("尚無評估資料", fontSize = 18.sp, color = FamilyTextSecondary)
        else {
            Text("${elder.sppbScore} / 12", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = FamilyPurple)
            Spacer(Modifier.height(8.dp))
            Text("評估日期：${formatAssessmentDate(elder.testDate)}", fontSize = 16.sp, color = FamilyTextSecondary)
        }
    }
}

@Composable
private fun AssessmentHistoryPlaceholder(elder: FamilyElderSummary) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (elder.sppbScore != null) item { LatestAssessmentCard(elder) }
        item { PlaceholderCard(Icons.Rounded.Assessment, "評估紀錄", "完整評估紀錄將於資料服務支援後顯示。") }
    }
}

@Composable
private fun TrainingStatusPlaceholder() {
    Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
        PlaceholderCard(Icons.Rounded.SportsGymnastics, "訓練狀況", "目前尚未提供家屬端的訓練摘要資料。\n後續服務支援後，即可在此查看長輩的訓練計畫與完成狀況。")
    }
}

@Composable
private fun PlaceholderCard(icon: ImageVector, title: String, message: String) {
    SectionCard(title) {
        Icon(icon, null, tint = FamilyPurple, modifier = Modifier.size(44.dp))
        Spacer(Modifier.height(14.dp))
        Text(message, fontSize = 16.sp, color = FamilyTextSecondary, lineHeight = 24.sp)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), CardDefaults.cardColors(Color.White), CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(20.dp)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FamilyText)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun ElderUnavailableScreen(onBack: () -> Unit) {
    Scaffold(containerColor = FamilyBackground) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Rounded.Shield, null, tint = FamilyPurple, modifier = Modifier.size(54.dp))
            Spacer(Modifier.height(18.dp))
            Text("無法顯示長輩資料", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("資料可能已更新，請返回家屬首頁重新載入。", color = FamilyTextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(22.dp))
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = FamilyPurple)) { Text("返回家屬首頁") }
        }
    }
}

@Composable
private fun familyNavigationColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = FamilyPurple,
    selectedTextColor = FamilyPurple,
    indicatorColor = FamilyPurpleLight,
    unselectedIconColor = FamilyTextSecondary,
    unselectedTextColor = FamilyTextSecondary
)

internal fun formatAssessmentDate(value: String?): String {
    val source = value?.trim()?.takeIf { it.isNotEmpty() } ?: return "尚無評估日期"
    val datePart = source.take(10)
    val parsed = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(datePart) }.getOrNull() ?: return source
    return SimpleDateFormat("yyyy/MM/dd", Locale.US).format(parsed)
}

private val previewElder = FamilyElderSummary("王奶奶", "王麗華", 10, false, "2026-10-20 09:30:00")

@Preview(name = "Family Loading", showBackground = true) @Composable private fun FamilyLoadingPreview() = GraduationProjectTheme { FamilyHomeContent("林小姐", FamilyHomeUiState.Loading, {}, {}, {}, {}) }
@Preview(name = "Family One Elder", showBackground = true) @Composable private fun FamilyOnePreview() = GraduationProjectTheme { FamilyHomeContent("林小姐", FamilyHomeUiState.Content(listOf(previewElder)), {}, {}, {}, {}) }
@Preview(name = "Family Two Elders", showBackground = true) @Composable private fun FamilyTwoPreview() = GraduationProjectTheme {
    FamilyHomeContent(
        "林小姐",
        FamilyHomeUiState.Content(
            listOf(
                previewElder,
                previewElder.copy(
                    displayName = "陳爺爺",
                    realName = null,
                    sppbScore = null,
                    hasFallRisk = null,
                    testDate = null
                )
            )
        ),
        {},
        {},
        {},
        {}
    )
}
@Preview(name = "Family Multiple Elders", showBackground = true) @Composable private fun FamilyMultiplePreview() = GraduationProjectTheme {
    FamilyHomeContent("林小姐", FamilyHomeUiState.Content(listOf(previewElder, previewElder.copy(displayName = "陳爺爺", realName = null, sppbScore = null, hasFallRisk = null, testDate = null), previewElder.copy(displayName = "李阿姨", sppbScore = 5, hasFallRisk = true), previewElder.copy(displayName = "張叔叔", realName = null, sppbScore = 8))), {}, {}, {}, {})
}
@Preview(name = "Family Empty", showBackground = true) @Composable private fun FamilyEmptyPreview() = GraduationProjectTheme { FamilyHomeContent("林小姐", FamilyHomeUiState.Empty, {}, {}, {}, {}) }
@Preview(name = "Family Error", showBackground = true) @Composable private fun FamilyErrorPreview() = GraduationProjectTheme { FamilyHomeContent("林小姐", FamilyHomeUiState.Error("preview"), {}, {}, {}, {}) }
@Preview(name = "Elder Detail", showBackground = true) @Composable private fun ElderDetailPreview() = GraduationProjectTheme { ElderDetailScreen(previewElder, {}) }
@Preview(name = "Elder Detail Missing Assessment", showBackground = true) @Composable private fun ElderDetailMissingPreview() = GraduationProjectTheme { ElderDetailScreen(previewElder.copy(sppbScore = null, hasFallRisk = null, testDate = null), {}) }
@Preview(name = "Elder Detail Fall Risk", showBackground = true) @Composable private fun ElderDetailRiskPreview() = GraduationProjectTheme { ElderDetailScreen(previewElder.copy(hasFallRisk = true), {}) }

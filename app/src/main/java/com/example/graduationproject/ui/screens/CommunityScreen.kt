package com.example.graduationproject.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.DataClass.CommunityUser
import com.example.graduationproject.DataClass.FriendActionRequest
import com.example.graduationproject.DataClass.FriendRequest
import com.example.graduationproject.DataClass.GetPointsRequest
import com.example.graduationproject.api.ApiClient
import com.example.graduationproject.profile.DEFAULT_PROFILE_AVATAR_ID
import com.example.graduationproject.profile.DEFAULT_PROFILE_AVATAR_COLOR_ID
import com.example.graduationproject.profile.AvatarSource
import com.example.graduationproject.ui.components.ProfileAvatar
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

private val BeigeBg = Color(0xFFFDFCF9)
private val PrimaryPeach = Color(0xFFFF8A65)
private val SecondaryTeal = Color(0xFF4DB6AC)
private val TextMain = Color(0xFF201A18)
private val TextSub = Color(0xFF5D5D5D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    accountId: Int,
    currentUserAvatarId: String = DEFAULT_PROFILE_AVATAR_ID,
    currentUserAvatarColorId: String = DEFAULT_PROFILE_AVATAR_COLOR_ID,
    currentUserAvatarSource: AvatarSource = AvatarSource.Preset(currentUserAvatarId),
    currentUserDisplayName: String = ""
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("社區排行榜", "我的好友")

    val leaderboardList = remember { mutableStateListOf<CommunityUser>() }
    val friendList = remember { mutableStateListOf<CommunityUser>() }
    val pendingRequests = remember { mutableStateListOf<FriendRequest>() }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val avatarColors = listOf(Color(0xFFCE93D8), Color(0xFF9FA8DA), Color(0xFF90CAF9), Color(0xFF80CBC4), Color(0xFFFFCC80))

    val refreshData = {
        if (accountId > 0) {
            coroutineScope.launch {
                try {
                    val response = ApiClient.apiService.getCommunityData(GetPointsRequest(accountId))
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()!!
                        leaderboardList.clear()
                        friendList.clear()
                        pendingRequests.clear()

                        leaderboardList.addAll(data.leaderboard.map { it.copy(avatarColor = avatarColors[Random.nextInt(avatarColors.size)]) })
                        friendList.addAll(data.friends.map { it.copy(avatarColor = avatarColors[Random.nextInt(avatarColors.size)]) })
                        pendingRequests.addAll(data.pendingRequests.map { it.copy(senderAvatarColor = avatarColors[Random.nextInt(avatarColors.size)]) })
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    val sendAction: (String, Int?, String?) -> Unit = { actionStr, target, phoneStr ->
        coroutineScope.launch {
            try {
                val response = ApiClient.apiService.handleFriendAction(
                    FriendActionRequest(accountId = accountId, action = actionStr, targetId = target, phone = phoneStr)
                )
                if (response.isSuccessful) {
                    Toast.makeText(context, response.body()?.message ?: "操作成功", Toast.LENGTH_SHORT).show()
                    refreshData()
                }
            } catch (e: Exception) { Toast.makeText(context, "網路連線失敗", Toast.LENGTH_SHORT).show() }
        }
    }

    LaunchedEffect(accountId, selectedTabIndex) {
        refreshData()
    }

    val currentUserFromApi = leaderboardList.find { it.id == accountId.toString() }
        ?: CommunityUser(
            id = accountId.toString(),
            name = "專屬長輩",
            level = "Lv.--",
            avatarColor = PrimaryPeach.copy(alpha = 0.2f),
            rank = 0,
            weeklyExp = 0,
            weeklyExercise = 0
        )
    val currentUser = currentUserFromApi.copy(
        name = currentUserDisplayName.ifBlank { currentUserFromApi.name }
    )

    val dateRangeStr = remember {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) { calendar.add(Calendar.DATE, -1) }
        val startMonth = calendar.get(Calendar.MONTH) + 1
        val startDay = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.add(Calendar.DATE, 6)
        "$startMonth/$startDay - ${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

    Column(modifier = Modifier.fillMaxSize().background(BeigeBg)) {
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex, containerColor = BeigeBg, contentColor = PrimaryPeach, modifier = Modifier.height(72.dp),
            indicator = { TabRowDefaults.PrimaryIndicator(modifier = Modifier.tabIndicatorOffset(selectedTabIndex), width = 80.dp, color = PrimaryPeach)}
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Tab(selected = isSelected, onClick = { selectedTabIndex = index }, modifier = Modifier.fillMaxHeight(),
                    text = { Text(text = title, fontSize = 20.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) PrimaryPeach else TextSub) }
                )
            }
        }

        if (selectedTabIndex == 0) {
            LeaderboardTab(
                currentUser = currentUser,
                currentUserAvatarId = currentUserAvatarId,
                currentUserAvatarColorId = currentUserAvatarColorId,
                currentUserAvatarSource = currentUserAvatarSource,
                dateRangeStr = dateRangeStr,
                leaderboardList = leaderboardList,
                onAction = sendAction
            )
        } else {
            FriendsTab(friendList, pendingRequests, sendAction)
        }
    }
}

@Composable
fun LeaderboardTab(
    currentUser: CommunityUser,
    currentUserAvatarId: String,
    currentUserAvatarColorId: String,
    currentUserAvatarSource: AvatarSource,
    dateRangeStr: String,
    leaderboardList: List<CommunityUser>,
    onAction: (String, Int?, String?) -> Unit
) {
    Column {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(text = "社區排行榜", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextMain)
                Text(text = "本週排行 ($dateRangeStr)", fontSize = 14.sp, color = TextSub)
            }
            Text(text = "排行榜將於週日 23:59 結算", fontSize = 12.sp, color = TextSub.copy(alpha = 0.7f))
        }

        MyRankHeader(
            user = currentUser,
            avatarId = currentUserAvatarId,
            avatarColorId = currentUserAvatarColorId,
            avatarSource = currentUserAvatarSource
        )

        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(items = leaderboardList, key = { it.id }) { user ->
                val displayedUser = if (user.id == currentUser.id) {
                    user.copy(name = currentUser.name)
                } else {
                    user
                }
                CommunityUserCard(
                    user = displayedUser,
                    isMe = displayedUser.id == currentUser.id,
                    avatarId = avatarIdForCommunityUser(
                        currentAccountId = currentUser.id,
                        displayedUserId = displayedUser.id,
                        currentUserAvatarId = currentUserAvatarId
                    ),
                    avatarColorId = avatarColorIdForCommunityUser(
                        currentAccountId = currentUser.id,
                        displayedUserId = displayedUser.id,
                        currentUserAvatarColorId = currentUserAvatarColorId
                    ),
                    avatarSource = avatarSourceForCommunityUser(
                        currentAccountId = currentUser.id,
                        displayedUserId = displayedUser.id,
                        currentUserAvatarSource = currentUserAvatarSource
                    ),
                    onAddFriend = { onAction("send", displayedUser.id.toInt(), null) },
                    onDeleteFriend = { onAction("delete", displayedUser.id.toInt(), null) }
                )
            }
        }
    }
}

@Composable
fun FriendsTab(
    friendList: List<CommunityUser>,
    pendingRequests: List<FriendRequest>,
    onAction: (String, Int?, String?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "新增好友", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextMain)
                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    placeholder = { Text(text = "輸入完整手機號碼以傳送好友邀請", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            if (searchQuery.isNotEmpty()) {
                                onAction("send_by_phone", null, searchQuery)
                                searchQuery = ""
                            }
                        }) { Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "搜尋", tint = PrimaryPeach) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPeach, unfocusedBorderColor = TextSub.copy(alpha = 0.3f))
                )
            }
        }

        if (pendingRequests.isNotEmpty()) {
            item { SectionTitle(title = "新的交友邀請", icon = Icons.Rounded.Notifications) }
            items(pendingRequests) { request ->
                FriendRequestCard(
                    request = request,
                    onAccept = { onAction("accept", request.id.toInt(), null) },
                    onReject = { onAction("reject", request.id.toInt(), null) }
                )
            }
        }

        item { SectionTitle(title = "我的好友", icon = Icons.Rounded.People) }

        if (friendList.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                    Text(text = "目前還沒有好友喔！\n趕快從上方新增，或到排行榜認識新朋友吧！", fontSize = 18.sp, color = TextSub, lineHeight = 28.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            items(items = friendList, key = { it.id }) { friend ->
                CommunityUserCard(user = friend, isMe = false, onAddFriend = {}, onDeleteFriend = { onAction("delete", friend.id.toInt(), null) })
            }
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryPeach,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain
        )
    }
}

@Composable
fun FriendRequestCard(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder(enabled = true).copy(width = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(request.senderAvatarColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.senderName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    text = request.senderLevel,
                    fontSize = 14.sp,
                    color = TextSub
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(
                    onClick = onReject,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFEEEEEE),
                        contentColor = TextSub
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "拒絕邀請",
                        modifier = Modifier.size(22.dp)
                    )
                }

                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPeach,
                        contentColor = Color.White
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "接受邀請",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MyRankHeader(
    user: CommunityUser,
    avatarId: String,
    avatarColorId: String,
    avatarSource: AvatarSource = AvatarSource.Preset(avatarId)
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        color = SecondaryTeal.copy(alpha = 0.1f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, SecondaryTeal.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    avatarSource = avatarSource,
                    avatarColorId = avatarColorId,
                    size = 56.dp,
                    contentDescription = "${user.name}的社群頭像"
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                    Text(
                        text = "本週累積表現",
                        fontSize = 14.sp,
                        color = TextSub
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "第 ${user.rank} 名",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SecondaryTeal
                    )
                    Text(
                        text = "${user.weeklyExp} 經驗值",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = SecondaryTeal
                    )
                }
            }
        }
    }
}

@Composable
fun CommunityUserCard(
    user: CommunityUser,
    isMe: Boolean,
    avatarId: String? = null,
    avatarColorId: String? = null,
    avatarSource: AvatarSource? = null,
    onAddFriend: () -> Unit,
    onDeleteFriend: () -> Unit
) {
    var showAchievementDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val stableOnDelete = rememberUpdatedState(onDeleteFriend)

    if (showAchievementDialog) {
        AchievementDialog(user, onDismiss = { showAchievementDialog = false })
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = "刪除好友", fontWeight = FontWeight.Bold) },
            text = { Text("確定要將 ${user.name} 從好友名單中移除嗎？") },
            confirmButton = {
                TextButton(onClick = {
                    stableOnDelete.value()
                    showDeleteConfirmDialog = false
                }) {
                    Text("確定刪除", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("取消", color = TextSub)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    OutlinedCard(
        onClick = { showAchievementDialog = true },
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
            Text(
                text = user.rank.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = if (user.rank <= 3) PrimaryPeach else TextSub.copy(alpha = 0.3f),
                modifier = Modifier.width(36.dp)
            )

            if (avatarId != null || avatarSource != null) {
                ProfileAvatar(
                    avatarSource = avatarSource ?: AvatarSource.Preset(avatarId!!),
                    avatarColorId = avatarColorId ?: DEFAULT_PROFILE_AVATAR_COLOR_ID,
                    size = 56.dp,
                    contentDescription = "${user.name}的社群頭像"
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(user.avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    text = user.level,
                    fontSize = 14.sp,
                    color = SecondaryTeal,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.MilitaryTech,
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

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                when {
                    isMe -> {
                        Spacer(modifier = Modifier.width(0.dp))
                    }
                    user.isFriend -> {
                        TextButton(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = TextSub),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "刪除好友",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "刪除",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    user.hasPendingRequestSent -> {
                        Text(
                            text = "已送出",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSub.copy(alpha = 0.5f),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    else -> {
                        Button(
                            onClick = onAddFriend,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryPeach.copy(alpha = 0.1f),
                                contentColor = PrimaryPeach
                            ),
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "加好友",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun avatarIdForCommunityUser(
    currentAccountId: String,
    displayedUserId: String,
    currentUserAvatarId: String
): String? = currentUserAvatarId.takeIf { currentAccountId == displayedUserId }

internal fun avatarColorIdForCommunityUser(
    currentAccountId: String,
    displayedUserId: String,
    currentUserAvatarColorId: String
): String? = currentUserAvatarColorId.takeIf { currentAccountId == displayedUserId }

internal fun avatarSourceForCommunityUser(
    currentAccountId: String,
    displayedUserId: String,
    currentUserAvatarSource: AvatarSource
): AvatarSource? = currentUserAvatarSource.takeIf { currentAccountId == displayedUserId }

@Composable
fun AchievementDialog(
    user: CommunityUser,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "關閉",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPeach
                )
            }
        },
        title = {
            Text(
                text = "${user.name} 的訓練成就",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AchievementRow(
                    icon = Icons.Rounded.EmojiEvents,
                    iconColor = Color(0xFFFFD700),
                    text = "本週運動：${user.weeklyExercise} 次"
                )
                AchievementRow(
                    icon = Icons.Rounded.MilitaryTech,
                    iconColor = PrimaryPeach,
                    text = "本週經驗值：${user.weeklyExp} 經驗值"
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun AchievementRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 20.sp,
            color = TextMain
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun CommunityScreenPreview() {
    GraduationProjectTheme {
        CommunityScreen(accountId = 1)
    }
}

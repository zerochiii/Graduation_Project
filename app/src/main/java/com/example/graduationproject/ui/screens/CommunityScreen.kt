package com.example.graduationproject.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    currentUserDisplayName: String = "",
    openChatFriendId: Int? = null,
    onChatOpened: () -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedChatFriend by remember(accountId) {
        mutableStateOf<CommunityUser?>(null)
    }
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
    LaunchedEffect(openChatFriendId, friendList.toList()) {
        if (openChatFriendId != null) {
            val friend = friendList.firstOrNull {
                it.id.toIntOrNull() == openChatFriendId
            }
            if (friend != null) {
                selectedTabIndex = 1
                selectedChatFriend = friend
                onChatOpened()
            }
        }
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

    selectedChatFriend?.let { friend ->
        ChatScreen(
            accountId = accountId,
            friend = friend,
            onBack = { selectedChatFriend = null }
        )
        return
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
            FriendsTab(
                friendList = friendList,
                pendingRequests = pendingRequests,
                onAction = sendAction,
                onOpenChat = { friend ->
                    selectedChatFriend = friend
                }
            )
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
    onAction: (String, Int?, String?) -> Unit,
    onOpenChat: (CommunityUser) -> Unit
){
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
                CommunityUserCard(
                    user = friend,
                    isMe = false,
                    onAddFriend = {},
                    onDeleteFriend = {
                        onAction("delete", friend.id.toInt(), null)
                    },
                    onOpenChat = {
                        onOpenChat(friend)
                    }
                )
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
    onDeleteFriend: () -> Unit,
    onOpenChat: (() -> Unit)? = null
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

    if (onOpenChat != null) {
        FriendCommunityUserCard(
            user = user,
            avatarId = avatarId,
            avatarColorId = avatarColorId,
            avatarSource = avatarSource,
            onOpenChat = onOpenChat,
            onDeleteFriend = { showDeleteConfirmDialog = true }
        )
        return
    }

    OutlinedCard(
        onClick = {
            showAchievementDialog = true
        },
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
                    color = TextMain,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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

            Column(horizontalAlignment = Alignment.End) {
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

@Composable
private fun FriendCommunityUserCard(
    user: CommunityUser,
    avatarId: String?,
    avatarColorId: String?,
    avatarSource: AvatarSource?,
    onOpenChat: () -> Unit,
    onDeleteFriend: () -> Unit
) {
    val cardShape = RoundedCornerShape(28.dp)

    Card(
        onClick = onOpenChat,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                onClick(
                    label = "與${user.name}聊天",
                    action = null
                )
            },
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 132.dp)
                .clip(cardShape)
        ) {
            FriendCardCloudDecoration()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 132.dp)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.rank.toString(),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryPeach,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(24.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                if (avatarId != null || avatarSource != null) {
                    ProfileAvatar(
                        avatarSource = avatarSource ?: AvatarSource.Preset(avatarId!!),
                        avatarColorId = avatarColorId ?: DEFAULT_PROFILE_AVATAR_COLOR_ID,
                        size = 60.dp,
                        contentDescription = "${user.name}的社群頭像"
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(user.avatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(38.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = user.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.level,
                        fontSize = 14.sp,
                        color = SecondaryTeal,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                            color = PrimaryPeach,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(114.dp)
                ) {
                    FriendChatBubble(
                        friendName = user.name,
                        onClick = onOpenChat,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 4.dp)
                    )

                    IconButton(
                        onClick = onDeleteFriend,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(48.dp)
                            .semantics {
                                contentDescription = "刪除${user.name}"
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(
                                    elevation = 2.dp,
                                    shape = CircleShape,
                                    clip = false,
                                    ambientColor = Color(0x1F000000),
                                    spotColor = Color(0x29000000)
                                )
                                .background(Color(0xFFFFF8F5), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null,
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF9A9A9A),
                    modifier = Modifier
                        .size(22.dp)
                        .clearAndSetSemantics { }
                )
            }
        }
    }
}

@Composable
private fun FriendChatBubble(
    friendName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bubbleColor = Color(0xFFFF806F)

    Box(
        modifier = modifier
            .width(100.dp)
            .height(58.dp)
            .semantics {
                contentDescription = "與${friendName}聊天"
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(48.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(50),
                    clip = false,
                    ambientColor = Color(0x33000000),
                    spotColor = Color(0x40000000)
                )
                .clearAndSetSemantics { }
        )

        Canvas(
            modifier = Modifier
                .matchParentSize()
                .clearAndSetSemantics { }
        ) {
            val tailHeight = 10.dp.toPx()
            val bodyHeight = size.height - tailHeight
            drawRoundRect(
                color = bubbleColor,
                topLeft = Offset.Zero,
                size = Size(size.width, bodyHeight),
                cornerRadius = CornerRadius(bodyHeight / 2f, bodyHeight / 2f)
            )
            val tailPath = Path().apply {
                moveTo(18.dp.toPx(), bodyHeight - 2.dp.toPx())
                lineTo(35.dp.toPx(), bodyHeight - 2.dp.toPx())
                lineTo(22.dp.toPx(), size.height)
                close()
            }
            drawPath(path = tailPath, color = bubbleColor)
        }

        Row(
            modifier = Modifier.padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Chat,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "聊天",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BoxScope.FriendCardCloudDecoration() {
    Canvas(
        modifier = Modifier
            .matchParentSize()
            .clearAndSetSemantics { }
    ) {
        val cream = Color(0xFFFFDFAE)
        val blush = Color(0xFFFFD5D0)
        val peachIvory = Color(0xFFFFE7DD)
        fun softTint(color: Color, strength: Float): Color =
            lerp(Color.White, color, strength)
        val bottomRightCloud = softTint(cream, 0.34f)

        drawOval(
            color = softTint(cream, 0.42f),
            topLeft = Offset(-size.width * 0.08f, -size.height * 0.48f),
            size = Size(size.width * 0.21f, size.height * 0.68f)
        )
        drawOval(
            color = softTint(cream, 0.34f),
            topLeft = Offset(size.width * 0.03f, -size.height * 0.4f),
            size = Size(size.width * 0.17f, size.height * 0.56f)
        )
        drawOval(
            color = softTint(peachIvory, 0.28f),
            topLeft = Offset(size.width * 0.14f, -size.height * 0.23f),
            size = Size(size.width * 0.1f, size.height * 0.28f)
        )

        drawOval(
            color = softTint(blush, 0.32f),
            topLeft = Offset(size.width * 0.86f, -size.height * 0.34f),
            size = Size(size.width * 0.16f, size.height * 0.48f)
        )
        drawOval(
            color = softTint(peachIvory, 0.26f),
            topLeft = Offset(size.width * 0.95f, -size.height * 0.15f),
            size = Size(size.width * 0.11f, size.height * 0.32f)
        )

        drawOval(
            color = softTint(blush, 0.42f),
            topLeft = Offset(-size.width * 0.13f, size.height * 0.84f),
            size = Size(size.width * 0.29f, size.height * 0.44f)
        )
        drawOval(
            color = softTint(blush, 0.34f),
            topLeft = Offset(size.width * 0.04f, size.height * 0.88f),
            size = Size(size.width * 0.24f, size.height * 0.36f)
        )
        drawOval(
            color = softTint(peachIvory, 0.28f),
            topLeft = Offset(size.width * 0.19f, size.height * 0.93f),
            size = Size(size.width * 0.17f, size.height * 0.24f)
        )

        drawOval(
            color = bottomRightCloud,
            topLeft = Offset(size.width * 0.5f, size.height * 0.76f),
            size = Size(size.width * 0.58f, size.height * 0.58f)
        )
        drawOval(
            color = bottomRightCloud,
            topLeft = Offset(size.width * 0.7f, size.height * 0.62f),
            size = Size(size.width * 0.38f, size.height * 0.52f)
        )
        drawOval(
            color = bottomRightCloud,
            topLeft = Offset(size.width * 0.88f, size.height * 0.68f),
            size = Size(size.width * 0.32f, size.height * 0.48f)
        )
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

@Preview(showBackground = true, widthDp = 360, fontScale = 1.5f)
@Composable
private fun FriendCardLongNamePreview() {
    GraduationProjectTheme {
        Box(
            modifier = Modifier
                .background(BeigeBg)
                .padding(16.dp)
        ) {
            CommunityUserCard(
                user = CommunityUser(
                    id = "preview-friend",
                    name = "測試好友名稱比較長",
                    level = "Lv.12 活力長青",
                    avatarColor = Color(0xFF84C8F4),
                    weeklyExercise = 5,
                    weeklyExp = 0,
                    rank = 1,
                    isFriend = true
                ),
                isMe = false,
                onAddFriend = {},
                onDeleteFriend = {},
                onOpenChat = {}
            )
        }
    }
}

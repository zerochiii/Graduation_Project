package com.example.graduationproject.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.graduationproject.DataClass.ChatMessage
import com.example.graduationproject.DataClass.CommunityUser
import kotlinx.coroutines.delay

private val ChatCanvas = Color(0xFFFAF8F4)
private val ChatMine = Color(0xFF536A92)
private val ChatFriend = Color(0xFFFFFDF9)
private val ChatHeader = Color(0xFFFAF9F6)
private val ChatInputBar = Color(0xFFFBFAF8)
private val ChatInputField = Color(0xFFF3F0EC)
private val ChatBorder = Color(0xFFD6CEC6)
private val ChatText = Color(0xFF27364A)
private val ChatSecondary = Color(0xFF665F5A)
private val ChatAccent = Color(0xFFFF8566)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    accountId: Int,
    friend: CommunityUser,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val token = remember(accountId) {
        context.getSharedPreferences(
            "ElderCarePrefs",
            Context.MODE_PRIVATE
        ).getString("CHAT_TOKEN", "").orEmpty()
    }
    val friendId = friend.id.toIntOrNull() ?: return
    val chat: ChatViewModel = viewModel(key = "chat_${accountId}_$friendId")
    var draft by remember(accountId, friendId) { mutableStateOf("") }
    val listState = rememberLazyListState()
    val wallpaperPreferences = remember(context.applicationContext) {
        ChatWallpaperPreferences(context.applicationContext)
    }
    var appliedWallpaperId by remember(accountId, friendId) {
        mutableStateOf(wallpaperPreferences.getWallpaperId(accountId, friendId))
    }
    var previewWallpaperId by remember(accountId, friendId) {
        mutableStateOf(appliedWallpaperId)
    }
    var showWallpaperPicker by remember(accountId, friendId) { mutableStateOf(false) }
    val visibleWallpaper = ChatWallpaperCatalog.resolve(
        if (showWallpaperPicker) previewWallpaperId else appliedWallpaperId
    )

    BackHandler(onBack = onBack)

    LaunchedEffect(chat, friendId, token) {
        chat.configure(friendId, token)
        while (true) {
            chat.refresh()
            delay(3500)
        }
    }

    LaunchedEffect(chat.messages.lastOrNull()?.id) {
        if (chat.messages.isNotEmpty()) {
            listState.animateScrollToItem(
                chat.messages.lastIndex + if (chat.hasOlder) 1 else 0
            )
        }
    }

    if (showWallpaperPicker) {
        ChatWallpaperPicker(
            selectedWallpaperId = previewWallpaperId,
            onWallpaperSelected = { previewWallpaperId = it },
            onApply = {
                val resolved = ChatWallpaperCatalog.resolve(previewWallpaperId)
                wallpaperPreferences.setWallpaperId(accountId, friendId, resolved.id)
                appliedWallpaperId = resolved.id
                previewWallpaperId = resolved.id
                showWallpaperPicker = false
            },
            onDismiss = {
                previewWallpaperId = appliedWallpaperId
                showWallpaperPicker = false
            }
        )
    }

    Scaffold(
        containerColor = ChatCanvas,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(friend.avatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(29.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = friend.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChatText
                            )
                            Text(
                                text = friend.level,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = ChatSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(52.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回好友列表",
                            modifier = Modifier.size(30.dp),
                            tint = ChatText
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            previewWallpaperId = appliedWallpaperId
                            showWallpaperPicker = true
                        },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoLibrary,
                            contentDescription = "更換聊天室背景",
                            modifier = Modifier.size(28.dp),
                            tint = ChatMine
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ChatHeader,
                    titleContentColor = ChatText,
                    navigationIconContentColor = ChatText,
                    actionIconContentColor = ChatMine
                )
            )
        },
        bottomBar = {
            Surface(color = ChatInputBar, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it.take(1000) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 58.dp),
                        placeholder = {
                            Text("輸入訊息…", fontSize = 18.sp, color = ChatSecondary)
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 18.sp,
                            lineHeight = 24.sp
                        ),
                        shape = RoundedCornerShape(26.dp),
                        enabled = !chat.sending,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ChatInputField,
                            unfocusedContainerColor = ChatInputField,
                            disabledContainerColor = ChatInputField,
                            focusedTextColor = ChatText,
                            unfocusedTextColor = ChatText,
                            focusedBorderColor = ChatMine,
                            unfocusedBorderColor = ChatBorder,
                            cursorColor = ChatMine
                        )
                    )
                    Spacer(Modifier.width(10.dp))
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        color = ChatMine,
                        shadowElevation = 2.dp
                    ) {
                        IconButton(
                            onClick = { chat.send(draft) { draft = "" } },
                            enabled = draft.isNotBlank() && !chat.sending && token.isNotBlank()
                        ) {
                            if (chat.sending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "送出訊息",
                                    modifier = Modifier.size(26.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            ChatWallpaperBackground(visibleWallpaper)
            Column(modifier = Modifier.fillMaxSize()) {
                chat.error?.let { ChatErrorCard(it) }
                when {
                    chat.loading && chat.messages.isEmpty() -> ChatLoadingState()
                    chat.messages.isEmpty() -> ChatEmptyState()
                    else -> LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        if (chat.hasOlder) {
                            item(key = "load_older") {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    TextButton(
                                        onClick = chat::loadOlder,
                                        enabled = !chat.loadingOlder,
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        if (chat.loadingOlder) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = ChatMine,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        Text(
                                            if (chat.loadingOlder) "載入中…" else "載入更早的訊息",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ChatMine
                                        )
                                    }
                                }
                            }
                        }
                        items(items = chat.messages, key = { it.id }) { message ->
                            ChatMessageBubble(
                                message = message,
                                mine = message.senderId == accountId
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatWallpaperBackground(wallpaper: ChatWallpaperOption) {
    Box(Modifier.fillMaxSize().background(ChatCanvas)) {
        wallpaper.drawableRes?.let { drawableRes ->
            Image(
                painter = painterResource(drawableRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier.fillMaxSize().background(
                    Color.White.copy(alpha = wallpaper.overlayAlpha)
                )
            )
        }
    }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage, mine: Boolean) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val bubbleMaxWidth = maxWidth * 0.76f
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start
        ) {
            Column(horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
                Surface(
                    modifier = Modifier.widthIn(max = bubbleMaxWidth),
                    color = if (mine) ChatMine else ChatFriend.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomEnd = if (mine) 5.dp else 20.dp,
                        bottomStart = if (mine) 20.dp else 5.dp
                    ),
                    border = if (mine) null else BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.85f)
                    ),
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = message.content,
                        modifier = Modifier.padding(horizontal = 17.dp, vertical = 12.dp),
                        color = if (mine) Color.White else ChatText,
                        fontSize = 18.sp,
                        lineHeight = 26.sp
                    )
                }
                Spacer(Modifier.height(5.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = message.sentAt.takeLast(8).take(5),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ChatSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatLoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            color = Color.White.copy(alpha = 0.9f),
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = ChatMine,
                    strokeWidth = 3.dp
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "正在載入聊天訊息…",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = ChatText
                )
            }
        }
    }
}

@Composable
private fun ChatEmptyState() {
    Box(
        Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.9f),
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                    tint = ChatAccent
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "還沒有訊息\n傳送第一則訊息吧！",
                    fontSize = 19.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChatText
                )
            }
        }
    }
}

@Composable
private fun ChatErrorCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        color = Color(0xFFFFE6E0).copy(alpha = 0.96f),
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Error,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = Color(0xFFA13F33)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                message,
                fontSize = 16.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF87362D)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatWallpaperPicker(
    selectedWallpaperId: String,
    onWallpaperSelected: (String) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFFFDFB),
        contentColor = ChatText
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Text("聊天室背景", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "先預覽喜歡的背景，再按下套用。",
                fontSize = 16.sp,
                lineHeight = 23.sp,
                color = ChatSecondary
            )
            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                gridItems(items = ChatWallpaperCatalog.options, key = { it.id }) { wallpaper ->
                    val isSelected = wallpaper.id == selectedWallpaperId
                    Surface(
                        onClick = { onWallpaperSelected(wallpaper.id) },
                        modifier = Modifier.semantics {
                            contentDescription = if (isSelected) {
                                "${wallpaper.displayName}聊天室背景，已選取"
                            } else {
                                "${wallpaper.displayName}聊天室背景"
                            }
                            selected = isSelected
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(
                            if (isSelected) 3.dp else 1.dp,
                            if (isSelected) ChatAccent else ChatBorder
                        ),
                        shadowElevation = if (isSelected) 3.dp else 1.dp
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(142.dp)
                                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                                    .background(ChatCanvas),
                                contentAlignment = Alignment.Center
                            ) {
                                wallpaper.drawableRes?.let { drawableRes ->
                                    Image(
                                        painter = painterResource(drawableRes),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        Modifier.fillMaxSize().background(
                                            Color.White.copy(alpha = wallpaper.overlayAlpha)
                                        )
                                    )
                                } ?: Icon(
                                    Icons.Rounded.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(42.dp),
                                    tint = ChatMine.copy(alpha = 0.72f)
                                )

                                if (isSelected) {
                                    Surface(
                                        modifier = Modifier.align(Alignment.TopEnd).padding(9.dp),
                                        shape = CircleShape,
                                        color = Color.White,
                                        shadowElevation = 2.dp
                                    ) {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.padding(3.dp).size(28.dp),
                                            tint = ChatAccent
                                        )
                                    }
                                }
                            }
                            Text(
                                wallpaper.displayName,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 11.dp),
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = ChatText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).heightIn(min = 54.dp),
                    shape = RoundedCornerShape(27.dp),
                    border = BorderStroke(1.dp, ChatBorder)
                ) {
                    Text(
                        "取消",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChatSecondary
                    )
                }
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1.35f).heightIn(min = 54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChatAccent,
                        contentColor = Color.White
                    )
                ) {
                    Text("套用背景", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

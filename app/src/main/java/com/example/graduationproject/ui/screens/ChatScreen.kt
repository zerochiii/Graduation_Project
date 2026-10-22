
package com.example.graduationproject.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.graduationproject.DataClass.CommunityUser
import kotlinx.coroutines.delay

private val ChatBeige = Color(0xFFFDFCF9)
private val ChatMine = Color(0xFF4F648D)
private val ChatFriend = Color(0xFFD7DEE7)
private val ChatHeader = Color(0xFFE5DAD0)
private val ChatInputBar = Color(0xFFE7DCD2)
private val ChatInputField = Color(0xFFF4EDE7)
private val ChatBorder = Color(0xFF9C8F87)
private val ChatText = Color(0xFF283548)
private val ChatSecondary = Color(0xFF625C60)

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
    var draft by remember(accountId, friendId) {
        mutableStateOf("")
    }
    val listState = rememberLazyListState()

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
                chat.messages.lastIndex +
                        if (chat.hasOlder) 1 else 0
            )
        }
    }

    Scaffold(
        containerColor = ChatBeige,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(friend.avatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = friend.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChatText
                            )
                            Text(
                                text = friend.level,
                                fontSize = 13.sp,
                                color = ChatSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回好友列表",
                            tint = ChatText
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ChatHeader,
                    titleContentColor = ChatText,
                    navigationIconContentColor = ChatText
                )
            )
        },
        bottomBar = {
            Surface(
                color = ChatInputBar,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = {
                            draft = it.take(1000)
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = "輸入訊息...",
                                color = ChatSecondary
                            )
                        },
                        shape = RoundedCornerShape(22.dp),
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
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        color = ChatMine
                    ) {
                        IconButton(
                            onClick = {
                                chat.send(draft) {
                                    draft = ""
                                }
                            },
                            enabled = draft.isNotBlank() &&
                                    !chat.sending &&
                                    token.isNotBlank()
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
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatBeige)
                .padding(padding)
        ) {
            chat.error?.let { message ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color(0xFFF1D7D3),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = message,
                        color = Color(0xFF8D3028),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (chat.loading && chat.messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ChatMine)
                }
            } else if (chat.messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "還沒有訊息，傳送第一則訊息吧！",
                        color = ChatSecondary,
                        fontSize = 17.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (chat.hasOlder) {
                        item(key = "load_older") {
                            TextButton(
                                onClick = chat::loadOlder,
                                enabled = !chat.loadingOlder
                            ) {
                                Text(
                                    text = if (chat.loadingOlder) {
                                        "載入中..."
                                    } else {
                                        "載入更早的訊息"
                                    },
                                    color = ChatMine
                                )
                            }
                        }
                    }

                    items(
                        items = chat.messages,
                        key = { it.id }
                    ) { message ->
                        val mine = message.senderId == accountId
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (mine) {
                                Arrangement.End
                            } else {
                                Arrangement.Start
                            }
                        ) {
                            Column(
                                horizontalAlignment = if (mine) {
                                    Alignment.End
                                } else {
                                    Alignment.Start
                                }
                            ) {
                                Surface(
                                    color = if (mine) ChatMine else ChatFriend,
                                    shape = RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomEnd = if (mine) 4.dp else 18.dp,
                                        bottomStart = if (mine) 18.dp else 4.dp
                                    ),
                                    shadowElevation = 2.dp
                                ) {
                                    Text(
                                        text = message.content,
                                        modifier = Modifier
                                            .widthIn(max = 270.dp)
                                            .padding(
                                                horizontal = 15.dp,
                                                vertical = 11.dp
                                            ),
                                        color = if (mine) {
                                            Color.White
                                        } else {
                                            ChatText
                                        },
                                        fontSize = 18.sp,
                                        lineHeight = 26.sp
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = message.sentAt.takeLast(8).take(5),
                                    fontSize = 12.sp,
                                    color = ChatSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

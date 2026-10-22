
package com.example.graduationproject.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.graduationproject.DataClass.ChatFetchRequest
import com.example.graduationproject.DataClass.ChatMessage
import com.example.graduationproject.DataClass.ChatSendRequest
import com.example.graduationproject.api.ApiClient
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel : ViewModel() {
    var messages by mutableStateOf<List<ChatMessage>>(emptyList())
        private set
    var hasOlder by mutableStateOf(false)
        private set
    var loading by mutableStateOf(true)
        private set
    var loadingOlder by mutableStateOf(false)
        private set
    var sending by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var friendId = -1
    private var token = ""
    private var polling = false
    private var pendingText: String? = null
    private var pendingId: String? = null

    fun configure(friendId: Int, token: String) {
        if (this.friendId == friendId && this.token == token) return
        this.friendId = friendId
        this.token = token
        messages = emptyList()
        hasOlder = false
        loading = true
        loadingOlder = false
        sending = false
        error = null
        pendingText = null
        pendingId = null
    }

    fun refresh() {
        if (polling || friendId <= 0) return
        if (token.isBlank()) {
            loading = false
            error = "請先登出並重新登入，以啟用好友聊天"
            return
        }
        polling = true
        val id = friendId
        val auth = "Bearer $token"
        val after = messages.lastOrNull()?.id
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getChatMessages(
                    auth,
                    ChatFetchRequest(friendId = id, afterId = after)
                )
                if (id != friendId || auth != "Bearer $token") return@launch
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    messages = (messages + body.messages).distinctBy { it.id }.sortedBy { it.id }
                    if (after == null) hasOlder = body.hasOlder
                    error = null
                } else {
                    error = body?.message ?: "無法讀取聊天紀錄（HTTP ${response.code()}）"
                }
            } catch (e: Exception) {
                error = "讀取聊天紀錄失敗：${e.localizedMessage ?: "網路錯誤"}"
            } finally {
                loading = false
                polling = false
            }
        }
    }

    fun loadOlder() {
        if (!hasOlder || loadingOlder || polling || token.isBlank()) return
        val id = friendId
        val before = messages.firstOrNull()?.id ?: return
        val auth = "Bearer $token"
        loadingOlder = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getChatMessages(
                    auth,
                    ChatFetchRequest(friendId = id, beforeId = before)
                )
                if (id != friendId || auth != "Bearer $token") return@launch
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    messages = (body.messages + messages).distinctBy { it.id }.sortedBy { it.id }
                    hasOlder = body.hasOlder
                    error = null
                } else {
                    error = body?.message ?: "無法載入較早的訊息"
                }
            } catch (e: Exception) {
                error = "載入較早訊息失敗：${e.localizedMessage ?: "網路錯誤"}"
            } finally {
                loadingOlder = false
            }
        }
    }

    fun send(text: String, onSuccess: () -> Unit) {
        val content = text.trim()
        if (sending || content.isEmpty() || content.length > 1000 || friendId <= 0) return
        if (token.isBlank()) {
            error = "請先登出並重新登入，以啟用好友聊天"
            return
        }
        val id = friendId
        val auth = "Bearer $token"
        val messageId = if (pendingText == content && pendingId != null) {
            pendingId!!
        } else {
            UUID.randomUUID().toString().also {
                pendingText = content
                pendingId = it
            }
        }
        sending = true
        error = null
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.sendChatMessage(
                    auth,
                    ChatSendRequest(id, content, messageId)
                )
                if (id != friendId || auth != "Bearer $token") return@launch
                if (response.isSuccessful && response.body()?.success == true) {
                    pendingText = null
                    pendingId = null
                    onSuccess()
                    refresh()
                } else {
                    error = response.body()?.message ?: "訊息傳送失敗"
                }
            } catch (e: Exception) {
                error = "訊息傳送失敗：${e.localizedMessage ?: "網路錯誤"}"
            } finally {
                sending = false
            }
        }
    }
}

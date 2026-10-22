package com.example.graduationproject.DataClass

import com.google.gson.annotations.SerializedName

data class ChatFetchRequest(
    @SerializedName("friend_id") val friendId: Int,
    @SerializedName("before_id") val beforeId: Long? = null,
    @SerializedName("after_id") val afterId: Long? = null
)

data class ChatSendRequest(
    @SerializedName("friend_id") val friendId: Int,
    val content: String,
    @SerializedName("client_message_id") val clientMessageId: String
)

data class ChatMessage(
    val id: Long,
    @SerializedName("sender_id") val senderId: Int,
    val content: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("sent_at") val sentAt: String
)

data class ChatMessagesResponse(
    val success: Boolean,
    val message: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    @SerializedName("has_older") val hasOlder: Boolean = false
)

data class ChatSendResponse(
    val success: Boolean,
    val message: String? = null,
    @SerializedName("message_id") val messageId: Long? = null
)

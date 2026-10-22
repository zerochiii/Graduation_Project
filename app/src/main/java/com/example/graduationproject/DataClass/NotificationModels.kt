
package com.example.graduationproject.DataClass

import com.google.gson.annotations.SerializedName

data class NotificationApiItem(
    val id: Int,
    val eventKey: String = "",
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val createdAtMillis: Long
)

data class GetNotificationsResponse(
    val success: Boolean,
    val message: String? = null,
    val notifications: List<NotificationApiItem> = emptyList()
)

data class MarkNotificationReadRequest(
    @SerializedName("account_id") val accountId: Int,
    @SerializedName("notification_id") val notificationId: Int
)

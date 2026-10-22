
package com.example.graduationproject.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.graduationproject.DataClass.GetPointsRequest
import com.example.graduationproject.DataClass.MarkNotificationReadRequest
import com.example.graduationproject.api.ApiClient
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    var notifications by mutableStateOf<List<HomeNotification>>(emptyList())
        private set
    var deletingIds by mutableStateOf<Set<Int>>(emptySet())
        private set
    private var currentAccountId = -1
    private var requestVersion = 0

    fun load(accountId: Int) {
        if (accountId <= 0) {
            reset()
            return
        }
        if (currentAccountId != accountId) {
            currentAccountId = accountId
            notifications = emptyList()
            deletingIds = emptySet()
        }
        val version = ++requestVersion
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getNotifications(
                    GetPointsRequest(accountId)
                )
                if (version != requestVersion || currentAccountId != accountId) return@launch
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    notifications = body.notifications.map { item ->
                        val notificationType = runCatching {
                            NotificationType.valueOf(item.type.uppercase())
                        }.getOrDefault(NotificationType.SYSTEM)
                        val friendId = if (notificationType == NotificationType.CHAT) {
                            item.eventKey.split("_").getOrNull(1)?.toIntOrNull()
                        } else {
                            null
                        }
                        HomeNotification(
                            id = item.id,
                            title = item.title,
                            message = item.message,
                            createdAtMillis = item.createdAtMillis,
                            type = notificationType,
                            isRead = item.isRead,
                            friendAccountId = friendId
                        )
                    }
                } else {
                    Log.e("NotificationViewModel", body?.message ?: "通知載入失敗")
                }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "通知載入失敗", e)
            }
        }
    }

    fun markAsRead(accountId: Int, notificationId: Int) {
        if (accountId != currentAccountId) return
        val target = notifications.firstOrNull { it.id == notificationId } ?: return
        if (target.isRead || notificationId in deletingIds) return
        notifications = notifications.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
        viewModelScope.launch {
            val success = try {
                val response = ApiClient.apiService.markNotificationRead(
                    MarkNotificationReadRequest(accountId, notificationId)
                )
                response.isSuccessful && response.body()?.success == true
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "通知已讀同步失敗", e)
                false
            }
            if (currentAccountId == accountId && !success) {
                notifications = notifications.map {
                    if (it.id == notificationId) it.copy(isRead = false) else it
                }
            }
        }
    }

    fun deleteNotification(
        accountId: Int,
        notificationId: Int,
        onResult: (Boolean) -> Unit = {}
    ) {
        if (accountId != currentAccountId || notificationId <= 0) {
            onResult(false)
            return
        }
        if (notificationId in deletingIds) return
        deletingIds = deletingIds + notificationId
        viewModelScope.launch {
            val success = try {
                val response = ApiClient.apiService.deleteNotification(
                    MarkNotificationReadRequest(accountId, notificationId)
                )
                response.isSuccessful && response.body()?.success == true
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "刪除通知失敗", e)
                false
            }
            if (currentAccountId == accountId) {
                if (success) {
                    requestVersion++
                    notifications = notifications.filterNot {
                        it.id == notificationId
                    }
                }
                deletingIds = deletingIds - notificationId
                onResult(success)
            }
        }
    }

    fun reset() {
        requestVersion++
        currentAccountId = -1
        notifications = emptyList()
        deletingIds = emptySet()
    }
}

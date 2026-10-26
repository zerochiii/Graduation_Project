package com.example.graduationproject.ui.screens

import android.content.Context
import androidx.annotation.DrawableRes
import com.example.graduationproject.R

data class ChatWallpaperOption(
    val id: String,
    val displayName: String,
    @DrawableRes val drawableRes: Int?,
    val overlayAlpha: Float
)

object ChatWallpaperCatalog {
    const val DEFAULT_ID = "default"

    val options: List<ChatWallpaperOption> = listOf(
        ChatWallpaperOption(DEFAULT_ID, "預設背景", null, 0f),
        ChatWallpaperOption(
            id = "sunny_flower_field",
            displayName = "晴空花野",
            drawableRes = R.drawable.chat_wallpaper_sunny_flower_field,
            overlayAlpha = 0.08f
        ),
        ChatWallpaperOption(
            id = "warm_sun_garden",
            displayName = "暖陽花園",
            drawableRes = R.drawable.chat_wallpaper_warm_sun_garden,
            overlayAlpha = 0.06f
        ),
        ChatWallpaperOption(
            id = "moonset_lake",
            displayName = "星月晚霞",
            drawableRes = R.drawable.chat_wallpaper_moonset_lake,
            overlayAlpha = 0.10f
        ),
        ChatWallpaperOption(
            id = "moonlight_flower_lake",
            displayName = "月夜花海",
            drawableRes = R.drawable.chat_wallpaper_moonlight_flower_lake,
            overlayAlpha = 0.12f
        ),
        ChatWallpaperOption(
            id = "pink_heart_landscape",
            displayName = "粉紅心語",
            drawableRes = R.drawable.chat_wallpaper_pink_heart_landscape,
            overlayAlpha = 0.06f
        ),
        ChatWallpaperOption(
            id = "flower_paper_plane",
            displayName = "花語紙飛機",
            drawableRes = R.drawable.chat_wallpaper_flower_paper_plane,
            overlayAlpha = 0.06f
        ),
        ChatWallpaperOption(
            id = "summer_lakeside",
            displayName = "夏日湖畔",
            drawableRes = R.drawable.chat_wallpaper_summer_lakeside,
            overlayAlpha = 0.16f
        ),
        ChatWallpaperOption(
            id = "cloud_cat",
            displayName = "雲朵小貓",
            drawableRes = R.drawable.chat_wallpaper_cloud_cat,
            overlayAlpha = 0.04f
        ),
        ChatWallpaperOption(
            id = "fresh_mountain_field",
            displayName = "清新山野",
            drawableRes = R.drawable.chat_wallpaper_fresh_mountain_field,
            overlayAlpha = 0.06f
        )
    )

    fun resolve(id: String?): ChatWallpaperOption =
        options.firstOrNull { it.id == id } ?: options.first()

    fun preferenceKey(accountId: Int, friendId: Int): String =
        "CHAT_BACKGROUND_${accountId}_$friendId"
}

class ChatWallpaperPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_FILE,
        Context.MODE_PRIVATE
    )

    fun getWallpaperId(accountId: Int, friendId: Int): String {
        val stored = preferences.getString(
            ChatWallpaperCatalog.preferenceKey(accountId, friendId),
            ChatWallpaperCatalog.DEFAULT_ID
        )
        return ChatWallpaperCatalog.resolve(stored).id
    }

    fun setWallpaperId(accountId: Int, friendId: Int, wallpaperId: String) {
        val key = ChatWallpaperCatalog.preferenceKey(accountId, friendId)
        val resolvedId = ChatWallpaperCatalog.resolve(wallpaperId).id
        preferences.edit().apply {
            if (resolvedId == ChatWallpaperCatalog.DEFAULT_ID) {
                remove(key)
            } else {
                putString(key, resolvedId)
            }
        }.apply()
    }

    private companion object {
        const val PREFERENCES_FILE = "FitCareChatWallpapers"
    }
}

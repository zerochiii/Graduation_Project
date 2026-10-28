package com.example.graduationproject.ui.screens

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.graduationproject.R

data class ChatBubblePalette(
    val friendBubbleColor: Color,
    val friendTextColor: Color,
    val ownBubbleColor: Color,
    val ownTextColor: Color,
    val timestampColor: Color
)

private val DefaultChatBubblePalette = ChatBubblePalette(
    friendBubbleColor = Color(0xFFFFFDF9).copy(alpha = 0.96f),
    friendTextColor = Color(0xFF27364A),
    ownBubbleColor = Color(0xFF536A92),
    ownTextColor = Color.White,
    timestampColor = Color(0xFF665F5A)
)

data class ChatWallpaperOption(
    val id: String,
    val displayName: String,
    @DrawableRes val drawableRes: Int?,
    val overlayAlpha: Float,
    val bubblePalette: ChatBubblePalette = DefaultChatBubblePalette
)

object ChatWallpaperCatalog {
    const val DEFAULT_ID = "default"

    val options: List<ChatWallpaperOption> = listOf(
        ChatWallpaperOption(DEFAULT_ID, "預設背景", null, 0f),
        ChatWallpaperOption(
            id = "sunny_flower_field",
            displayName = "晴空花野",
            drawableRes = R.drawable.chat_wallpaper_sunny_flower_field,
            overlayAlpha = 0.08f,
            bubblePalette = ChatBubblePalette(
                friendBubbleColor = Color(0xFFF8FBFC),
                friendTextColor = Color(0xFF24364B),
                ownBubbleColor = Color(0xFF58789D),
                ownTextColor = Color.White,
                timestampColor = Color(0xFF586B7A)
            )
        ),
        ChatWallpaperOption(
            id = "warm_sun_garden",
            displayName = "暖陽花園",
            drawableRes = R.drawable.chat_wallpaper_warm_sun_garden,
            overlayAlpha = 0.06f,
            bubblePalette = ChatBubblePalette(
                friendBubbleColor = Color(0xFFFFF9F1),
                friendTextColor = Color(0xFF3D342D),
                ownBubbleColor = Color(0xFF8C664C),
                ownTextColor = Color.White,
                timestampColor = Color(0xFF766658)
            )
        ),
        ChatWallpaperOption(
            id = "moonset_lake",
            displayName = "星月晚霞",
            drawableRes = R.drawable.chat_wallpaper_moonset_lake,
            overlayAlpha = 0.10f,
            bubblePalette = ChatBubblePalette(
                friendBubbleColor = Color(0xFFFBF8FF),
                friendTextColor = Color(0xFF303347),
                ownBubbleColor = Color(0xFF6C6796),
                ownTextColor = Color.White,
                timestampColor = Color(0xFF69657C)
            )
        ),
        ChatWallpaperOption(
            id = "moonlight_flower_lake",
            displayName = "月夜花海",
            drawableRes = R.drawable.chat_wallpaper_moonlight_flower_lake,
            overlayAlpha = 0.12f,
            bubblePalette = ChatBubblePalette(
                friendBubbleColor = Color(0xFFF9F7FF),
                friendTextColor = Color(0xFF303247),
                ownBubbleColor = Color(0xFF625F88),
                ownTextColor = Color.White,
                timestampColor = Color(0xFF66647A)
            )
        ),
        ChatWallpaperOption(
            id = "pink_heart_landscape",
            displayName = "粉紅心語",
            drawableRes = R.drawable.chat_wallpaper_pink_heart_landscape,
            overlayAlpha = 0.06f,
            bubblePalette = ChatBubblePalette(
                friendBubbleColor = Color(0xFFFFF7FA),
                friendTextColor = Color(0xFF3D3038),
                ownBubbleColor = Color(0xFF9A5D72),
                ownTextColor = Color.White,
                timestampColor = Color(0xFF735F68)
            )
        ),
        ChatWallpaperOption(
            id = "flower_paper_plane",
            displayName = "花語紙飛機",
            drawableRes = R.drawable.chat_wallpaper_flower_paper_plane,
            overlayAlpha = 0.06f,
            bubblePalette = ChatBubblePalette(
                friendBubbleColor = Color(0xFFFFF8F3),
                friendTextColor = Color(0xFF3B312D),
                ownBubbleColor = Color(0xFFA05F4D),
                ownTextColor = Color.White,
                timestampColor = Color(0xFF765F57)
            )
        ),
        ChatWallpaperOption(
            id = "summer_lakeside",
            displayName = "夏日湖畔",
            drawableRes = R.drawable.chat_wallpaper_summer_lakeside,
            overlayAlpha = 0.16f,
            bubblePalette = ChatBubblePalette(
                friendBubbleColor = Color(0xFFF7FBFC),
                friendTextColor = Color(0xFF23364A),
                ownBubbleColor = Color(0xFF4F7396),
                ownTextColor = Color.White,
                timestampColor = Color(0xFF586A79)
            )
        ),
        ChatWallpaperOption(
            id = "cloud_cat",
            displayName = "雲朵小貓",
            drawableRes = R.drawable.chat_wallpaper_cloud_cat,
            overlayAlpha = 0.04f,
            bubblePalette = ChatBubblePalette(
                friendBubbleColor = Color(0xFFFFF9F5),
                friendTextColor = Color(0xFF3C3135),
                ownBubbleColor = Color(0xFFA96169),
                ownTextColor = Color.White,
                timestampColor = Color(0xFF776368)
            )
        ),
        ChatWallpaperOption(
            id = "fresh_mountain_field",
            displayName = "清新山野",
            drawableRes = R.drawable.chat_wallpaper_fresh_mountain_field,
            overlayAlpha = 0.06f,
            bubblePalette = ChatBubblePalette(
                friendBubbleColor = Color(0xFFF7FBF8),
                friendTextColor = Color(0xFF2C3B36),
                ownBubbleColor = Color(0xFF527B70),
                ownTextColor = Color.White,
                timestampColor = Color(0xFF5F726C)
            )
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

package com.example.graduationproject.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatWallpaperCatalogTest {
    @Test
    fun catalog_containsDefaultAndNineWallpapers() {
        assertEquals(10, ChatWallpaperCatalog.options.size)
        assertEquals(ChatWallpaperCatalog.DEFAULT_ID, ChatWallpaperCatalog.options.first().id)
        assertNull(ChatWallpaperCatalog.options.first().drawableRes)
    }

    @Test
    fun catalog_stableIdsAreUnique() {
        val ids = ChatWallpaperCatalog.options.map { it.id }

        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun preferenceKey_isScopedByAccountAndFriend() {
        assertEquals(
            "CHAT_BACKGROUND_12_34",
            ChatWallpaperCatalog.preferenceKey(accountId = 12, friendId = 34)
        )
        assertTrue(
            ChatWallpaperCatalog.preferenceKey(12, 34) !=
                ChatWallpaperCatalog.preferenceKey(12, 35)
        )
        assertTrue(
            ChatWallpaperCatalog.preferenceKey(12, 34) !=
                ChatWallpaperCatalog.preferenceKey(13, 34)
        )
    }

    @Test
    fun resolve_unknownIdFallsBackToDefault() {
        assertEquals(
            ChatWallpaperCatalog.DEFAULT_ID,
            ChatWallpaperCatalog.resolve("removed_wallpaper").id
        )
    }
}

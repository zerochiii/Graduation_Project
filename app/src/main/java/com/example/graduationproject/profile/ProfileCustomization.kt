package com.example.graduationproject.profile

import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Elderly
import androidx.compose.material.icons.rounded.EmojiNature
import androidx.compose.material.icons.rounded.EmojiPeople
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.FlutterDash
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal const val DEFAULT_PROFILE_AVATAR_ID = "avatar_default"
internal const val DEFAULT_PROFILE_AVATAR_COLOR_ID = "avatar_color_peach"
internal const val MAX_DISPLAY_NAME_LENGTH = 20

enum class LocalImageSource {
    GALLERY,
    CAMERA
}

sealed interface AvatarSource {
    data class Preset(val avatarId: String) : AvatarSource
    data class LocalImage(val path: String, val sourceType: LocalImageSource) : AvatarSource
}

internal enum class AvatarCategory(val displayName: String) {
    ALL("全部"), PERSON("人物"), ANIMAL("動物"), LIFE("生活"), NATURE("自然")
}

internal data class AvatarOption(
    val id: String,
    val displayName: String,
    val icon: ImageVector,
    val category: AvatarCategory,
    val legacyColorId: String = DEFAULT_PROFILE_AVATAR_COLOR_ID
)

internal data class AvatarColorOption(val id: String, val displayName: String, val color: Color)

internal object AvatarCatalog {
    val options: List<AvatarOption> = listOf(
        AvatarOption(DEFAULT_PROFILE_AVATAR_ID, "經典人物", Icons.Rounded.Person, AvatarCategory.PERSON),
        AvatarOption("avatar_02", "微笑人物", Icons.Rounded.Face, AvatarCategory.PERSON, "avatar_color_teal"),
        AvatarOption("avatar_person_03", "開心人物", Icons.Rounded.Mood, AvatarCategory.PERSON, "avatar_color_blue"),
        AvatarOption("avatar_person_04", "活力人物", Icons.Rounded.EmojiPeople, AvatarCategory.PERSON, "avatar_color_orange"),
        AvatarOption("avatar_person_05", "健康人物", Icons.Rounded.AccessibilityNew, AvatarCategory.PERSON, "avatar_color_green"),
        AvatarOption("avatar_person_06", "樂齡人物", Icons.Rounded.Elderly, AvatarCategory.PERSON, "avatar_color_purple"),
        AvatarOption("avatar_04", "友善夥伴", Icons.Rounded.Pets, AvatarCategory.ANIMAL, "avatar_color_blue"),
        AvatarOption("avatar_06", "活力自然", Icons.Rounded.EmojiNature, AvatarCategory.ANIMAL, "avatar_color_orange"),
        AvatarOption("avatar_penguin", "企鵝夥伴", Icons.Rounded.FlutterDash, AvatarCategory.ANIMAL, "avatar_color_teal"),
        AvatarOption("avatar_ladybug", "瓢蟲夥伴", Icons.Rounded.BugReport, AvatarCategory.ANIMAL, "avatar_color_peach"),
        AvatarOption("avatar_home", "溫暖小屋", Icons.Rounded.Home, AvatarCategory.LIFE, "avatar_color_orange"),
        AvatarOption("avatar_cake", "幸福蛋糕", Icons.Rounded.Cake, AvatarCategory.LIFE, "avatar_color_purple"),
        AvatarOption("avatar_03", "溫暖花朵", Icons.Rounded.LocalFlorist, AvatarCategory.NATURE, "avatar_color_purple"),
        AvatarOption("avatar_05", "自然樹木", Icons.Rounded.Park, AvatarCategory.NATURE, "avatar_color_green"),
        AvatarOption("avatar_sun", "暖陽", Icons.Rounded.WbSunny, AvatarCategory.NATURE, "avatar_color_orange"),
        AvatarOption("avatar_mountain", "青山", Icons.Rounded.Landscape, AvatarCategory.NATURE, "avatar_color_teal")
    )

    val colors = listOf(
        AvatarColorOption("avatar_color_peach", "暖珊瑚", Color(0xFFFFAB91)),
        AvatarColorOption("avatar_color_teal", "湖水綠", Color(0xFF80CBC4)),
        AvatarColorOption("avatar_color_purple", "柔和紫", Color(0xFFCE93D8)),
        AvatarColorOption("avatar_color_blue", "天空藍", Color(0xFF90CAF9)),
        AvatarColorOption("avatar_color_green", "自然綠", Color(0xFFA5D6A7)),
        AvatarColorOption("avatar_color_orange", "活力橘", Color(0xFFFFCC80))
    )

    val default = options.first()
    val defaultColor = colors.first()
    fun find(id: String?) = options.firstOrNull { it.id == id } ?: default
    fun findColor(id: String?) = colors.firstOrNull { it.id == id } ?: defaultColor
    fun contains(id: String) = options.any { it.id == id }
    fun containsColor(id: String) = colors.any { it.id == id }
    fun legacyColorFor(avatarId: String) = findColor(find(avatarId).legacyColorId)
    fun optionsFor(category: AvatarCategory) =
        if (category == AvatarCategory.ALL) options else options.filter { it.category == category }
}

internal interface ProfileSettingsStore {
    fun readAvatarSource(accountId: Int): AvatarSource?
    fun readAvatarColorId(accountId: Int): String?
    fun readDisplayName(accountId: Int): String?
    fun writeAvatar(accountId: Int, source: AvatarSource, colorId: String): Boolean
    fun writeDisplayName(accountId: Int, displayName: String): Boolean
}

internal class SharedPreferencesProfileSettingsStore(
    private val preferences: SharedPreferences
) : ProfileSettingsStore {
    override fun readAvatarSource(accountId: Int): AvatarSource? {
        return when (preferences.getString("PROFILE_AVATAR_KIND_$accountId", null)) {
            "LOCAL" -> {
                val path = preferences.getString("PROFILE_AVATAR_LOCAL_PATH_$accountId", null)
                val sourceType = preferences.getString("PROFILE_AVATAR_LOCAL_SOURCE_$accountId", null)
                    ?.let { runCatching { LocalImageSource.valueOf(it) }.getOrNull() }
                if (!path.isNullOrBlank() && sourceType != null) {
                    AvatarSource.LocalImage(path, sourceType)
                } else {
                    null
                }
            }
            else -> preferences.getString("PROFILE_AVATAR_$accountId", null)
                ?.let(AvatarSource::Preset)
        }
    }

    override fun readAvatarColorId(accountId: Int) = preferences.getString("PROFILE_AVATAR_COLOR_$accountId", null)
    override fun readDisplayName(accountId: Int) = preferences.getString("PROFILE_DISPLAY_NAME_$accountId", null)

    override fun writeAvatar(accountId: Int, source: AvatarSource, colorId: String): Boolean {
        val editor = preferences.edit().putString("PROFILE_AVATAR_COLOR_$accountId", colorId)
        when (source) {
            is AvatarSource.Preset -> editor
                .putString("PROFILE_AVATAR_KIND_$accountId", "PRESET")
                .putString("PROFILE_AVATAR_$accountId", source.avatarId)
                .remove("PROFILE_AVATAR_LOCAL_PATH_$accountId")
                .remove("PROFILE_AVATAR_LOCAL_SOURCE_$accountId")
            is AvatarSource.LocalImage -> editor
                .putString("PROFILE_AVATAR_KIND_$accountId", "LOCAL")
                .putString("PROFILE_AVATAR_LOCAL_PATH_$accountId", source.path)
                .putString("PROFILE_AVATAR_LOCAL_SOURCE_$accountId", source.sourceType.name)
        }
        return editor.commit()
    }

    override fun writeDisplayName(accountId: Int, displayName: String) =
        preferences.edit().putString("PROFILE_DISPLAY_NAME_$accountId", displayName).commit()
}

internal data class ProfileCustomizationState(
    val accountId: Int? = null,
    val avatarSource: AvatarSource = AvatarSource.Preset(DEFAULT_PROFILE_AVATAR_ID),
    val avatarColorId: String = DEFAULT_PROFILE_AVATAR_COLOR_ID,
    val customDisplayName: String? = null,
    val accountName: String? = null,
    val errorMessage: String? = null
) {
    val displayName: String get() = customDisplayName ?: accountName.orEmpty()
    val avatarId: String
        get() = (avatarSource as? AvatarSource.Preset)?.avatarId ?: DEFAULT_PROFILE_AVATAR_ID
}

internal sealed interface DisplayNameValidation {
    data class Valid(val normalized: String) : DisplayNameValidation
    data class Invalid(val message: String) : DisplayNameValidation
}

internal fun validateDisplayName(value: String): DisplayNameValidation {
    val normalized = value.trim().replace(Regex("[\\s\\u3000]+"), " ")
    return when {
        normalized.isEmpty() -> DisplayNameValidation.Invalid("顯示名稱不能空白。")
        normalized.length > MAX_DISPLAY_NAME_LENGTH ->
            DisplayNameValidation.Invalid("顯示名稱最多 $MAX_DISPLAY_NAME_LENGTH 個字元。")
        normalized.any { !it.isLetterOrDigit() && it != ' ' } ->
            DisplayNameValidation.Invalid("顯示名稱只能使用中文、英文、數字與空格。")
        else -> DisplayNameValidation.Valid(normalized)
    }
}

internal class ProfileCustomizationViewModel(
    private val settingsStore: ProfileSettingsStore,
    private val avatarFiles: ProfileAvatarFileManager
) : ViewModel() {
    var uiState by mutableStateOf(ProfileCustomizationState())
        private set

    fun bindAccount(accountId: Int) {
        if (accountId <= 0) {
            reset()
            return
        }
        if (uiState.accountId == accountId) return
        val storedSource = settingsStore.readAvatarSource(accountId)
        val avatarSource = when (storedSource) {
            is AvatarSource.Preset -> AvatarSource.Preset(AvatarCatalog.find(storedSource.avatarId).id)
            is AvatarSource.LocalImage -> storedSource.takeIf {
                avatarFiles.isSavedImageAvailable(accountId, it)
            } ?: AvatarSource.Preset(DEFAULT_PROFILE_AVATAR_ID)
            null -> AvatarSource.Preset(DEFAULT_PROFILE_AVATAR_ID)
        }
        val storedColorId = settingsStore.readAvatarColorId(accountId)
        val color = if (AvatarCatalog.containsColor(storedColorId.orEmpty())) {
            AvatarCatalog.findColor(storedColorId)
        } else {
            AvatarCatalog.legacyColorFor(
                (avatarSource as? AvatarSource.Preset)?.avatarId ?: DEFAULT_PROFILE_AVATAR_ID
            )
        }
        uiState = ProfileCustomizationState(
            accountId = accountId,
            avatarSource = avatarSource,
            avatarColorId = color.id,
            customDisplayName = settingsStore.readDisplayName(accountId)?.takeIf { it.isNotBlank() }
        )
    }

    fun updateAccountName(accountId: Int, accountName: String) {
        if (uiState.accountId == accountId) {
            uiState = uiState.copy(accountName = accountName.trim().takeIf { it.isNotEmpty() })
        }
    }

    fun saveAvatar(accountId: Int, source: AvatarSource, colorId: String): Boolean {
        if (uiState.accountId != accountId || !AvatarCatalog.containsColor(colorId)) {
            return reportError("無法儲存這個頭像，請重新選擇。")
        }
        if (source is AvatarSource.Preset && !AvatarCatalog.contains(source.avatarId)) {
            return reportError("無法儲存這個頭像，請重新選擇。")
        }

        val persistedSource = when (source) {
            is AvatarSource.Preset -> source
            is AvatarSource.LocalImage -> {
                if (avatarFiles.isSavedImageAvailable(accountId, source)) source
                else avatarFiles.persistCandidate(accountId, source).getOrElse {
                    return reportError("照片儲存失敗，請稍後再試。")
                }
            }
        }
        val newlyPersisted = persistedSource is AvatarSource.LocalImage && persistedSource != source
        if (!settingsStore.writeAvatar(accountId, persistedSource, colorId)) {
            if (newlyPersisted) avatarFiles.deleteSavedImage(persistedSource as AvatarSource.LocalImage)
            return reportError("頭像儲存失敗，請稍後再試。")
        }
        val previousSource = uiState.avatarSource
        if (previousSource is AvatarSource.LocalImage && previousSource != persistedSource) {
            avatarFiles.deleteSavedImage(previousSource)
        }
        uiState = uiState.copy(
            avatarSource = persistedSource,
            avatarColorId = colorId,
            errorMessage = null
        )
        return true
    }

    fun saveDisplayName(accountId: Int, value: String): Boolean {
        if (uiState.accountId != accountId) return reportError("帳號狀態已改變，請重新操作。")
        val validation = validateDisplayName(value)
        if (validation is DisplayNameValidation.Invalid) return reportError(validation.message)
        val normalized = (validation as DisplayNameValidation.Valid).normalized
        if (!settingsStore.writeDisplayName(accountId, normalized)) {
            return reportError("顯示名稱儲存失敗，請稍後再試。")
        }
        uiState = uiState.copy(customDisplayName = normalized, errorMessage = null)
        return true
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    fun reset() {
        uiState = ProfileCustomizationState()
    }

    private fun reportError(message: String): Boolean {
        uiState = uiState.copy(errorMessage = message)
        return false
    }
}

internal class ProfileCustomizationViewModelFactory(
    private val settingsStore: ProfileSettingsStore,
    private val avatarFiles: ProfileAvatarFileManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ProfileCustomizationViewModel::class.java))
        return ProfileCustomizationViewModel(settingsStore, avatarFiles) as T
    }
}

package com.example.graduationproject.profile

import com.example.graduationproject.ui.screens.avatarColorIdForCommunityUser
import com.example.graduationproject.ui.screens.avatarIdForCommunityUser
import com.example.graduationproject.ui.screens.avatarSourceForCommunityUser
import java.io.File
import java.io.InputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileCustomizationViewModelTest {
    @Test
    fun newAccount_usesDefaultsAndBackendAccountName() {
        val viewModel = createViewModel()
        viewModel.bindAccount(11)
        viewModel.updateAccountName(11, "王奶奶")
        assertEquals(11, viewModel.uiState.accountId)
        assertEquals(AvatarSource.Preset(DEFAULT_PROFILE_AVATAR_ID), viewModel.uiState.avatarSource)
        assertEquals(DEFAULT_PROFILE_AVATAR_COLOR_ID, viewModel.uiState.avatarColorId)
        assertEquals("王奶奶", viewModel.uiState.displayName)
    }

    @Test
    fun presetAvatarSelection_updatesStateAndPersistence() {
        val store = FakeProfileSettingsStore()
        val viewModel = createViewModel(store)
        viewModel.bindAccount(11)
        val source = AvatarSource.Preset("avatar_03")
        assertTrue(viewModel.saveAvatar(11, source, "avatar_color_blue"))
        assertEquals(source, viewModel.uiState.avatarSource)
        assertEquals("avatar_color_blue", viewModel.uiState.avatarColorId)
        assertEquals(source, store.readAvatarSource(11))
    }

    @Test
    fun localAvatar_isPromotedAndRestoredAfterViewModelRecreation() {
        val store = FakeProfileSettingsStore()
        val files = FakeProfileAvatarFileManager()
        val candidate = AvatarSource.LocalImage("/cache/11/candidate.jpg", LocalImageSource.CAMERA)
        files.candidates += candidate.path
        val first = createViewModel(store, files)
        first.bindAccount(11)
        assertTrue(first.saveAvatar(11, candidate, "avatar_color_green"))
        val saved = first.uiState.avatarSource as AvatarSource.LocalImage
        assertEquals("/files/11/avatar.jpg", saved.path)

        val recreated = createViewModel(store, files)
        recreated.bindAccount(11)
        assertEquals(saved, recreated.uiState.avatarSource)
        assertEquals("avatar_color_green", recreated.uiState.avatarColorId)
    }

    @Test
    fun missingSavedImage_fallsBackToDefaultPreset() {
        val missing = AvatarSource.LocalImage("/files/11/missing.jpg", LocalImageSource.GALLERY)
        val store = FakeProfileSettingsStore(avatarValues = mutableMapOf(11 to missing))
        val viewModel = createViewModel(store, FakeProfileAvatarFileManager())
        viewModel.bindAccount(11)
        assertEquals(AvatarSource.Preset(DEFAULT_PROFILE_AVATAR_ID), viewModel.uiState.avatarSource)
    }

    @Test
    fun legacyStoredAvatar_withoutColor_keepsItsPreviousFixedColor() {
        val viewModel = createViewModel(
            FakeProfileSettingsStore(
                avatarValues = mutableMapOf(11 to AvatarSource.Preset("avatar_05"))
            )
        )
        viewModel.bindAccount(11)
        assertEquals("avatar_05", viewModel.uiState.avatarId)
        assertEquals("avatar_color_green", viewModel.uiState.avatarColorId)
    }

    @Test
    fun accountsKeepIndependentSettings() {
        val store = FakeProfileSettingsStore()
        val viewModel = createViewModel(store)
        viewModel.bindAccount(11)
        assertTrue(viewModel.saveAvatar(11, AvatarSource.Preset("avatar_02"), "avatar_color_purple"))
        assertTrue(viewModel.saveDisplayName(11, "帳號十一"))
        viewModel.bindAccount(22)
        assertEquals(AvatarSource.Preset(DEFAULT_PROFILE_AVATAR_ID), viewModel.uiState.avatarSource)
        assertTrue(viewModel.saveAvatar(22, AvatarSource.Preset("avatar_05"), "avatar_color_orange"))
        assertTrue(viewModel.saveDisplayName(22, "帳號二十二"))
        viewModel.bindAccount(11)
        assertEquals(AvatarSource.Preset("avatar_02"), viewModel.uiState.avatarSource)
        assertEquals("avatar_color_purple", viewModel.uiState.avatarColorId)
        assertEquals("帳號十一", viewModel.uiState.displayName)
        assertEquals("帳號二十二", store.readDisplayName(22))
    }

    @Test
    fun localAvatarFromAnotherAccount_isRejected() {
        val store = FakeProfileSettingsStore()
        val files = FakeProfileAvatarFileManager()
        val account11Image = AvatarSource.LocalImage("/files/11/avatar.jpg", LocalImageSource.GALLERY)
        files.savedByAccount[11] = account11Image.path
        store.avatarValues[22] = account11Image
        val viewModel = createViewModel(store, files)
        viewModel.bindAccount(22)
        assertEquals(AvatarSource.Preset(DEFAULT_PROFILE_AVATAR_ID), viewModel.uiState.avatarSource)
    }

    @Test
    fun displayNameValidation_normalizesWhitespaceAndRejectsInvalidInput() {
        assertEquals(DisplayNameValidation.Valid("樂活 長輩 2"), validateDisplayName("  樂活　長輩   2  "))
        assertTrue(validateDisplayName("   ") is DisplayNameValidation.Invalid)
        assertTrue(validateDisplayName("超過二十個字元的顯示名稱不應該被儲存到設定中123") is DisplayNameValidation.Invalid)
        assertTrue(validateDisplayName("長輩@example") is DisplayNameValidation.Invalid)
    }

    @Test
    fun customDisplayName_overridesBackendName() {
        val viewModel = createViewModel()
        viewModel.bindAccount(11)
        viewModel.updateAccountName(11, "後端姓名")
        assertTrue(viewModel.saveDisplayName(11, "社群名稱"))
        assertEquals("社群名稱", viewModel.uiState.displayName)
        assertEquals("後端姓名", viewModel.uiState.accountName)
    }

    @Test
    fun persistenceFailure_deletesNewSavedFileAndKeepsPreviousUiState() {
        val files = FakeProfileAvatarFileManager()
        val candidate = AvatarSource.LocalImage("/cache/11/candidate.jpg", LocalImageSource.CAMERA)
        files.candidates += candidate.path
        val viewModel = createViewModel(FakeProfileSettingsStore(writeSucceeds = false), files)
        viewModel.bindAccount(11)
        assertFalse(viewModel.saveAvatar(11, candidate, "avatar_color_blue"))
        assertEquals(AvatarSource.Preset(DEFAULT_PROFILE_AVATAR_ID), viewModel.uiState.avatarSource)
        assertEquals(listOf("/files/11/avatar.jpg"), files.deletedSaved)
        assertEquals("頭像儲存失敗，請稍後再試。", viewModel.uiState.errorMessage)
    }

    @Test
    fun reset_clearsAccountSpecificRuntimeState() {
        val viewModel = createViewModel()
        viewModel.bindAccount(11)
        viewModel.updateAccountName(11, "王奶奶")
        viewModel.reset()
        assertNull(viewModel.uiState.accountId)
        assertEquals("", viewModel.uiState.displayName)
        assertEquals(AvatarSource.Preset(DEFAULT_PROFILE_AVATAR_ID), viewModel.uiState.avatarSource)
    }

    @Test
    fun communityAvatar_appliesOnlyToCurrentUser() {
        val source = AvatarSource.LocalImage("/files/11/avatar.jpg", LocalImageSource.GALLERY)
        assertEquals("avatar_05", avatarIdForCommunityUser("11", "11", "avatar_05"))
        assertNull(avatarIdForCommunityUser("11", "22", "avatar_05"))
        assertEquals("avatar_color_green", avatarColorIdForCommunityUser("11", "11", "avatar_color_green"))
        assertEquals(source, avatarSourceForCommunityUser("11", "11", source))
        assertNull(avatarSourceForCommunityUser("11", "22", source))
    }

    private fun createViewModel(
        store: FakeProfileSettingsStore = FakeProfileSettingsStore(),
        files: FakeProfileAvatarFileManager = FakeProfileAvatarFileManager()
    ) = ProfileCustomizationViewModel(store, files)
}

private class FakeProfileSettingsStore(
    val avatarValues: MutableMap<Int, AvatarSource> = mutableMapOf(),
    private val colorValues: MutableMap<Int, String> = mutableMapOf(),
    private val displayNames: MutableMap<Int, String> = mutableMapOf(),
    private val writeSucceeds: Boolean = true
) : ProfileSettingsStore {
    override fun readAvatarSource(accountId: Int) = avatarValues[accountId]
    override fun readAvatarColorId(accountId: Int) = colorValues[accountId]
    override fun readDisplayName(accountId: Int) = displayNames[accountId]
    override fun writeAvatar(accountId: Int, source: AvatarSource, colorId: String): Boolean {
        if (!writeSucceeds) return false
        avatarValues[accountId] = source
        colorValues[accountId] = colorId
        return true
    }
    override fun writeDisplayName(accountId: Int, displayName: String): Boolean {
        if (!writeSucceeds) return false
        displayNames[accountId] = displayName
        return true
    }
}

private class FakeProfileAvatarFileManager : ProfileAvatarFileManager {
    val candidates = mutableSetOf<String>()
    val savedByAccount = mutableMapOf<Int, String>()
    val deletedSaved = mutableListOf<String>()
    override fun stageGalleryImage(accountId: Int, input: InputStream) = Result.failure<AvatarSource.LocalImage>(UnsupportedOperationException())
    override fun createCameraCandidate(accountId: Int) = Result.failure<AvatarSource.LocalImage>(UnsupportedOperationException())
    override fun persistCandidate(accountId: Int, source: AvatarSource.LocalImage): Result<AvatarSource.LocalImage> {
        if (source.path !in candidates) return Result.failure(IllegalArgumentException())
        val saved = source.copy(path = "/files/$accountId/avatar.jpg")
        candidates -= source.path
        savedByAccount[accountId] = saved.path
        return Result.success(saved)
    }
    override fun isSavedImageAvailable(accountId: Int, source: AvatarSource.LocalImage) = savedByAccount[accountId] == source.path
    override fun discardCandidate(source: AvatarSource.LocalImage) { candidates -= source.path }
    override fun deleteSavedImage(source: AvatarSource.LocalImage) {
        deletedSaved += source.path
        savedByAccount.entries.removeAll { it.value == source.path }
    }
    override fun fileFor(source: AvatarSource.LocalImage) = File(source.path)
}

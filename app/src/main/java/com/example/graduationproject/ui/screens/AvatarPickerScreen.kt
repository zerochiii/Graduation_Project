package com.example.graduationproject.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.graduationproject.profile.AvatarCatalog
import com.example.graduationproject.profile.AvatarCategory
import com.example.graduationproject.profile.AvatarSource
import com.example.graduationproject.profile.LocalImageSource
import com.example.graduationproject.profile.ProfileAvatarFileManager
import com.example.graduationproject.ui.components.ProfileAvatar
import com.example.graduationproject.ui.components.ScaleButton
import com.example.graduationproject.ui.theme.scaledSp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun AvatarPickerScreen(
    accountId: Int,
    currentAvatarSource: AvatarSource,
    currentAvatarColorId: String,
    avatarFiles: ProfileAvatarFileManager,
    errorMessage: String?,
    onSave: (AvatarSource, String) -> Boolean,
    onClearError: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentSourceKey = currentAvatarSource.storageKey()
    var pendingPresetId by rememberSaveable(accountId, currentSourceKey) {
        mutableStateOf((currentAvatarSource as? AvatarSource.Preset)?.avatarId)
    }
    var pendingLocalPath by rememberSaveable(accountId, currentSourceKey) {
        mutableStateOf((currentAvatarSource as? AvatarSource.LocalImage)?.path)
    }
    var pendingLocalSource by rememberSaveable(accountId, currentSourceKey) {
        mutableStateOf((currentAvatarSource as? AvatarSource.LocalImage)?.sourceType?.name)
    }
    var pendingCameraPath by rememberSaveable(accountId) { mutableStateOf<String?>(null) }
    var pendingAvatarColorId by remember(accountId, currentAvatarColorId) {
        mutableStateOf(currentAvatarColorId)
    }
    var selectedCategory by remember { mutableStateOf(AvatarCategory.ALL) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val pendingAvatarSource = pendingLocalPath?.let { path ->
        pendingLocalSource?.let { sourceName ->
            runCatching { LocalImageSource.valueOf(sourceName) }.getOrNull()
                ?.let { AvatarSource.LocalImage(path, it) }
        }
    } ?: AvatarSource.Preset(pendingPresetId ?: AvatarCatalog.default.id)

    fun replacePendingSource(source: AvatarSource) {
        val previous = pendingAvatarSource
        if (previous is AvatarSource.LocalImage && previous != currentAvatarSource && previous != source) {
            avatarFiles.discardCandidate(previous)
        }
        when (source) {
            is AvatarSource.Preset -> {
                pendingPresetId = source.avatarId
                pendingLocalPath = null
                pendingLocalSource = null
            }
            is AvatarSource.LocalImage -> {
                pendingPresetId = null
                pendingLocalPath = source.path
                pendingLocalSource = source.sourceType.name
            }
        }
    }

    fun showMessage(message: String) {
        coroutineScope.launch { snackbarHostState.showSnackbar(message) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) {
            showMessage("未選取照片")
        } else {
            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        val input = context.contentResolver.openInputStream(uri)
                            ?: error("Unable to open selected image")
                        avatarFiles.stageGalleryImage(accountId, input).getOrThrow()
                    }
                }
                result.onSuccess(::replacePendingSource)
                    .onFailure { showMessage("無法讀取所選照片") }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { captured ->
        val candidate = pendingCameraPath?.let {
            AvatarSource.LocalImage(it, LocalImageSource.CAMERA)
        }
        pendingCameraPath = null
        if (captured && candidate != null && avatarFiles.fileFor(candidate).length() > 0L) {
            replacePendingSource(candidate)
        } else {
            candidate?.let(avatarFiles::discardCandidate)
            showMessage("拍攝已取消")
        }
    }

    val latestPendingSource by rememberUpdatedState(pendingAvatarSource)
    val latestCameraPath by rememberUpdatedState(pendingCameraPath)
    DisposableEffect(accountId, currentSourceKey) {
        onDispose {
            if (context.findActivity()?.isChangingConfigurations != true) {
                val pending = latestPendingSource
                if (pending is AvatarSource.LocalImage && pending != currentAvatarSource) {
                    avatarFiles.discardCandidate(pending)
                }
                latestCameraPath?.let {
                    avatarFiles.discardCandidate(AvatarSource.LocalImage(it, LocalImageSource.CAMERA))
                }
            }
        }
    }

    val navigateBackWithCleanup = {
        if (pendingAvatarSource is AvatarSource.LocalImage && pendingAvatarSource != currentAvatarSource) {
            avatarFiles.discardCandidate(pendingAvatarSource)
        }
        pendingCameraPath?.let {
            avatarFiles.discardCandidate(AvatarSource.LocalImage(it, LocalImageSource.CAMERA))
        }
        onNavigateBack()
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onClearError()
        }
    }

    SettingsScaffold(
        title = "社群頭像設定",
        onNavigateBack = navigateBackWithCleanup,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ScaleButton(
                onClick = {
                    if (onSave(pendingAvatarSource, pendingAvatarColorId)) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("社群頭像已更新")
                        }
                    }
                },
                text = "儲存頭像",
                enabled = accountId > 0,
                modifier = Modifier.padding(horizontal = SettingsPagePadding, vertical = SettingsSectionSpacing),
                containerColor = SettingsPrimaryPeach,
                minHeight = SettingsPrimaryCtaHeight
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            state = rememberLazyGridState(),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = SettingsPagePadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "將顯示於社群與互動功能中",
                        fontSize = 16.scaledSp(),
                        color = SettingsTextSub
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileAvatar(
                        avatarSource = pendingAvatarSource,
                        avatarColorId = pendingAvatarColorId,
                        size = 112.dp,
                        contentDescription = when (pendingAvatarSource) {
                            is AvatarSource.Preset ->
                                "目前預覽：${AvatarCatalog.find(pendingAvatarSource.avatarId).displayName}"
                            is AvatarSource.LocalImage -> "目前預覽：自訂照片"
                        },
                        onImageLoadError = { showMessage("照片載入失敗") }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "使用自己的照片",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 19.scaledSp(),
                        fontWeight = FontWeight.Bold,
                        color = SettingsTextMain
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SettingsCard {
                        SettingsNavigationRow(
                            icon = Icons.Rounded.CameraAlt,
                            title = "拍攝照片",
                            subtitle = "使用相機拍攝新的社群頭像",
                            iconTint = SettingsPrimaryPeach,
                            onClick = {
                                avatarFiles.createCameraCandidate(accountId)
                                    .onSuccess { candidate ->
                                        pendingCameraPath = candidate.path
                                        runCatching {
                                            FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                avatarFiles.fileFor(candidate)
                                            )
                                        }.onSuccess { uri -> cameraLauncher.launch(uri) }
                                            .onFailure {
                                                pendingCameraPath = null
                                                avatarFiles.discardCandidate(candidate)
                                                showMessage("無法開啟相機")
                                            }
                                    }
                                    .onFailure {
                                        showMessage("無法建立拍攝檔案")
                                    }
                            }
                        )
                        SettingsDivider()
                        SettingsNavigationRow(
                            icon = Icons.Rounded.PhotoLibrary,
                            title = "從相簿選擇",
                            subtitle = "從裝置照片中選擇頭像",
                            iconTint = SettingsSecondaryTeal,
                            onClick = {
                                runCatching {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }.onFailure {
                                    showMessage("無法開啟相簿")
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "選擇預設頭像",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 19.scaledSp(),
                        fontWeight = FontWeight.Bold,
                        color = SettingsTextMain
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "頭像背景色",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 17.scaledSp(),
                        fontWeight = FontWeight.Bold,
                        color = SettingsTextMain
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(
                            count = AvatarCatalog.colors.size,
                            key = { AvatarCatalog.colors[it].id }
                        ) { index ->
                            val colorOption = AvatarCatalog.colors[index]
                            val isColorSelected = pendingAvatarColorId == colorOption.id
                            Surface(
                                modifier = Modifier
                                    .width(52.dp)
                                    .selectable(
                                        selected = isColorSelected,
                                        role = Role.RadioButton,
                                        onClick = { pendingAvatarColorId = colorOption.id }
                                    )
                                    .semantics {
                                        contentDescription = colorOption.displayName
                                        selected = isColorSelected
                                        stateDescription = if (isColorSelected) "已選擇" else "未選擇"
                                    },
                                shape = CircleShape,
                                color = colorOption.color,
                                border = BorderStroke(
                                    width = if (isColorSelected) 4.dp else 1.dp,
                                    color = if (isColorSelected) SettingsTextMain else SettingsTextSub.copy(alpha = 0.2f)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isColorSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "${colorOption.displayName}，已選擇",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            count = AvatarCategory.entries.size,
                            key = { AvatarCategory.entries[it].name }
                        ) { index ->
                            val category = AvatarCategory.entries[index]
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = {
                                    Text(
                                        category.displayName,
                                        fontSize = 15.scaledSp(),
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SettingsPrimaryPeach,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            items(AvatarCatalog.optionsFor(selectedCategory), key = { it.id }) { avatar ->
                val isSelected = pendingAvatarSource is AvatarSource.Preset &&
                    pendingAvatarSource.avatarId == avatar.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            role = Role.RadioButton,
                            onClick = { replacePendingSource(AvatarSource.Preset(avatar.id)) }
                        )
                        .semantics {
                            selected = isSelected
                            stateDescription = if (isSelected) "已選擇" else "未選擇"
                        },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) SettingsPrimaryPeach else SettingsTextSub.copy(alpha = 0.18f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box {
                            ProfileAvatar(
                                avatarId = avatar.id,
                                avatarColorId = pendingAvatarColorId,
                                size = 58.dp,
                                contentDescription = "${avatar.displayName}頭像"
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "已選擇",
                                    tint = SettingsPrimaryPeach,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = avatar.displayName,
                            fontSize = 12.scaledSp(),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = SettingsTextMain,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun AvatarSource.storageKey(): String = when (this) {
    is AvatarSource.Preset -> "PRESET:$avatarId"
    is AvatarSource.LocalImage -> "LOCAL:${sourceType.name}:$path"
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

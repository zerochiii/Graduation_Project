package com.example.graduationproject.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.SwitchAccount
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.example.graduationproject.profile.DEFAULT_PROFILE_AVATAR_COLOR_ID
import com.example.graduationproject.profile.DEFAULT_PROFILE_AVATAR_ID
import com.example.graduationproject.profile.AvatarSource
import com.example.graduationproject.ui.components.ProfileAvatar
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import com.example.graduationproject.ui.theme.scaledSp

internal val SettingsBeigeBg = Color(0xFFFDFCF9)
internal val SettingsPrimaryPeach = Color(0xFFFF8A65)
internal val SettingsSecondaryTeal = Color(0xFF4DB6AC)
internal val SettingsTextMain = Color(0xFF201A18)
internal val SettingsTextSub = Color(0xFF5D5D5D)
internal val SettingsErrorRed = Color(0xFFD32F2F)
internal val SettingsCardRadius = 24.dp
internal val SettingsPagePadding = 20.dp
internal val SettingsSectionSpacing = 16.dp
internal val SettingsIconContainerSize = 48.dp
internal val SettingsPrimaryCtaHeight = 64.dp

internal class SettingsBackNavigationCoordinator(
    private val settingsRoutes: Set<String> = DEFAULT_SETTINGS_ROUTES
) {
    private var currentDestination: String? = null
    private var destinationSettled = false
    private var navigationConsumed = false

    fun onDestinationChanged(destination: String?) {
        if (destination == currentDestination) return
        currentDestination = destination
        destinationSettled = false
        navigationConsumed = false
    }

    fun onDestinationSettled(destination: String?) {
        if (destination == currentDestination && destination in settingsRoutes) {
            destinationSettled = true
        }
    }

    fun onDestinationLeaving(destination: String?) {
        if (destination == currentDestination) {
            destinationSettled = false
        }
    }

    fun tryNavigate(destination: String?, action: () -> Unit): Boolean {
        if (
            destination !in settingsRoutes ||
            destination != currentDestination ||
            !destinationSettled ||
            navigationConsumed
        ) {
            return false
        }
        navigationConsumed = true
        destinationSettled = false
        action()
        return true
    }

    private companion object {
        val DEFAULT_SETTINGS_ROUTES = setOf(
            "settings",
            "avatar_picker",
            "display_name_settings",
            "community_visibility_settings",
            "profile_summary",
            "switch_account",
            "account_info",
            "logout"
        )
    }
}

internal fun dispatchSettingsBackRequest(
    backNavigationEnabled: Boolean,
    onNavigateBack: () -> Unit,
    onBackBlocked: () -> Unit
): Boolean = if (backNavigationEnabled) {
    onNavigateBack()
    true
} else {
    onBackBlocked()
    false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    accountId: Int,
    avatarId: String,
    avatarColorId: String,
    avatarSource: AvatarSource = AvatarSource.Preset(avatarId),
    displayName: String,
    onNavigateToAvatarPicker: () -> Unit,
    onNavigateToDisplayName: () -> Unit,
    onNavigateToCommunityVisibility: () -> Unit,
    onNavigateToProfileSummary: () -> Unit,
    onNavigateToSwitchAccount: () -> Unit,
    onNavigateToAccountInfo: () -> Unit,
    onNavigateToLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    SettingsScaffold(title = "個人化設定中心", onNavigateBack = onNavigateBack) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = SettingsPagePadding, vertical = SettingsSectionSpacing),
            verticalArrangement = Arrangement.spacedBy(SettingsSectionSpacing)
        ) {
            item {
                Text(
                    "打造屬於你的社群形象與使用體驗",
                    fontSize = 16.scaledSp(),
                    color = SettingsTextSub
                )
            }
            item { SettingsSectionTitle("個人資料") }
            item {
                SettingsCard {
                    SettingsNavigationRow(
                        icon = Icons.Rounded.AccountCircle,
                        title = "社群頭像",
                        subtitle = "顯示於社群與互動功能中",
                        onClick = onNavigateToAvatarPicker,
                        leadingContent = {
                            ProfileAvatar(
                                avatarSource = avatarSource,
                                avatarColorId = avatarColorId,
                                size = 56.dp,
                                contentDescription = "目前社群頭像"
                            )
                        }
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Rounded.Badge,
                        title = "顯示名稱",
                        subtitle = displayName.ifBlank { "尚未設定" },
                        onClick = onNavigateToDisplayName
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Rounded.Info,
                        title = "個人資料摘要",
                        subtitle = "查看帳號與使用資訊",
                        onClick = onNavigateToProfileSummary
                    )
                }
            }

            item { SettingsSectionTitle("社群設定") }
            item {
                SettingsCard {
                    SettingsNavigationRow(
                        icon = Icons.Rounded.Groups,
                        title = "社群顯示設定",
                        subtitle = "管理在社群中的顯示內容",
                        onClick = onNavigateToCommunityVisibility
                    )
                }
            }

            item { SettingsSectionTitle("帳號設定") }
            item {
                SettingsCard {
                    SettingsNavigationRow(
                        icon = Icons.Rounded.SwitchAccount,
                        title = "切換帳號",
                        subtitle = displayName.ifBlank { "目前登入帳號" },
                        onClick = onNavigateToSwitchAccount
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.Rounded.AccountCircle,
                        title = "帳號資訊",
                        subtitle = "查看基本識別資訊",
                        onClick = onNavigateToAccountInfo
                    )
                    SettingsDivider()
                    SettingsNavigationRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "登出",
                        subtitle = "安全離開目前帳號",
                        iconTint = SettingsErrorRed,
                        titleColor = SettingsErrorRed,
                        onClick = onNavigateToLogout
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    backNavigationEnabled: Boolean = true,
    onBackBlocked: () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val requestBack = {
        dispatchSettingsBackRequest(
            backNavigationEnabled = backNavigationEnabled,
            onNavigateBack = onNavigateBack,
            onBackBlocked = onBackBlocked
        )
    }

    BackHandler { requestBack() }

    Scaffold(
        containerColor = SettingsBeigeBg,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { requestBack() }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SettingsBeigeBg,
                    titleContentColor = SettingsTextMain
                )
            )
        },
        snackbarHost = snackbarHost,
        bottomBar = bottomBar,
        content = content
    )
}

@Composable
internal fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SettingsCardRadius),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(content = content)
    }
}

@Composable
internal fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: Color = SettingsPrimaryPeach,
    titleColor: Color = SettingsTextMain,
    leadingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 80.dp)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingContent != null) {
            leadingContent()
        } else {
            SettingsIconContainer(icon = icon, tint = iconTint)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 19.scaledSp(), fontWeight = FontWeight.Bold, color = titleColor)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, fontSize = 15.scaledSp(), color = SettingsTextSub)
        }
        Icon(
            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = "前往$title",
            tint = SettingsTextSub,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
internal fun SettingsDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = SettingsTextSub.copy(alpha = 0.12f)
    )
}

@Composable
internal fun SettingsSectionTitle(text: String) {
    Text(text, fontSize = 22.scaledSp(), fontWeight = FontWeight.Bold, color = SettingsTextMain)
}

@Composable
internal fun SettingsIconContainer(
    icon: ImageVector,
    tint: Color,
    size: Dp = SettingsIconContainerSize
) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = tint.copy(alpha = 0.14f)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.padding(10.dp).size(28.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    GraduationProjectTheme {
        SettingsScreen(
            accountId = 1,
            avatarId = DEFAULT_PROFILE_AVATAR_ID,
            avatarColorId = DEFAULT_PROFILE_AVATAR_COLOR_ID,
            displayName = "王小明",
            onNavigateToAvatarPicker = {},
            onNavigateToDisplayName = {},
            onNavigateToCommunityVisibility = {},
            onNavigateToProfileSummary = {},
            onNavigateToSwitchAccount = {},
            onNavigateToAccountInfo = {},
            onNavigateToLogout = {},
            onNavigateBack = {}
        )
    }
}

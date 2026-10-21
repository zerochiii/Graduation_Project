package com.example.graduationproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.graduationproject.ui.components.ProfileAvatar
import com.example.graduationproject.profile.AvatarSource
import com.example.graduationproject.ui.components.ScaleButton
import com.example.graduationproject.ui.theme.scaledSp

@Composable
internal fun SwitchAccountScreen(
    displayName: String,
    avatarId: String,
    avatarColorId: String,
    avatarSource: AvatarSource = AvatarSource.Preset(avatarId),
    onLoginAnotherAccount: () -> Unit,
    onNavigateBack: () -> Unit
) {
    SettingsScaffold(title = "切換帳號", onNavigateBack = onNavigateBack) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = SettingsPagePadding, vertical = SettingsSectionSpacing),
            verticalArrangement = Arrangement.spacedBy(SettingsSectionSpacing)
        ) {
            item { SettingsSectionTitle("目前帳號") }
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileAvatar(
                            avatarSource = avatarSource,
                            avatarColorId = avatarColorId,
                            size = 64.dp,
                            contentDescription = "目前帳號頭像"
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                displayName.ifBlank { "目前登入帳號" },
                                fontSize = 20.scaledSp(),
                                fontWeight = FontWeight.Bold,
                                color = SettingsTextMain
                            )
                            Text("目前登入的帳號", fontSize = 15.scaledSp(), color = SettingsTextSub)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = SettingsSecondaryTeal
                            )
                            Text(
                                "目前使用中",
                                fontSize = 13.scaledSp(),
                                fontWeight = FontWeight.Bold,
                                color = SettingsSecondaryTeal
                            )
                        }
                    }
                }
            }
            item { SettingsSectionTitle("其他") }
            item {
                SettingsCard {
                    SettingsNavigationRow(
                        icon = Icons.Rounded.AddCircle,
                        title = "使用其他帳號登入",
                        subtitle = "返回登入頁並登入其他帳號",
                        iconTint = androidx.compose.ui.graphics.Color(0xFF8E5BD9),
                        onClick = onLoginAnotherAccount
                    )
                }
            }
            item {
                Text(
                    "目前 App 不保存多組帳號密碼。登入其他帳號後，各帳號的個人化設定仍會分開保留。",
                    fontSize = 15.scaledSp(),
                    color = SettingsTextSub
                )
            }
        }
    }
}

@Composable
internal fun AccountInfoScreen(
    displayName: String,
    appVersion: String,
    onNavigateBack: () -> Unit
) {
    SettingsScaffold(title = "帳號資訊", onNavigateBack = onNavigateBack) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = SettingsPagePadding, vertical = SettingsSectionSpacing),
            verticalArrangement = Arrangement.spacedBy(SettingsSectionSpacing)
        ) {
            item {
                SettingsCard {
                    AccountInfoRow(Icons.Rounded.AccountCircle, "顯示名稱", displayName.ifBlank { "尚未設定" })
                    SettingsDivider()
                    AccountInfoRow(Icons.Rounded.Info, "App 版本", appVersion)
                }
            }
            item {
                Text(
                    "這裡只顯示目前帳號可辨識的基本資料；系統內部編號不需要記憶。",
                    fontSize = 15.scaledSp(),
                    color = SettingsTextSub
                )
            }
        }
    }
}

@Composable
private fun AccountInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconContainer(icon = icon, tint = SettingsSecondaryTeal)
        Spacer(Modifier.width(14.dp))
        Text(title, modifier = Modifier.weight(1f), fontSize = 17.scaledSp(), color = SettingsTextSub)
        Text(value, fontSize = 17.scaledSp(), fontWeight = FontWeight.Bold, color = SettingsTextMain)
    }
}

@Composable
internal fun LogoutScreen(onConfirmLogout: () -> Unit, onNavigateBack: () -> Unit) {
    SettingsScaffold(title = "登出", onNavigateBack = onNavigateBack) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = SettingsErrorRed,
                modifier = Modifier.size(88.dp).padding(18.dp)
            )
            Text("確定要登出嗎？", fontSize = 26.scaledSp(), fontWeight = FontWeight.Bold, color = SettingsTextMain)
            Spacer(Modifier.height(12.dp))
            Text(
                "登出後將返回登入頁，下次需重新登入才能使用。\n\n你的訓練紀錄與個人化設定不會被刪除。",
                fontSize = 17.scaledSp(),
                color = SettingsTextSub
            )
            Spacer(Modifier.height(28.dp))
            ScaleButton(
                onClick = onConfirmLogout,
                text = "確定登出",
                icon = Icons.AutoMirrored.Filled.Logout,
                containerColor = SettingsPrimaryPeach,
                minHeight = SettingsPrimaryCtaHeight
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth().height(SettingsPrimaryCtaHeight),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            ) {
                Text("取消", fontSize = 18.scaledSp(), color = SettingsTextMain)
            }
        }
    }
}

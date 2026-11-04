package com.example.graduationproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.graduationproject.ui.components.ProfileAvatar
import com.example.graduationproject.profile.AvatarSource
import com.example.graduationproject.ui.theme.scaledSp

internal data class ProfileSummaryData(
    val displayName: String,
    val avatarId: String,
    val avatarColorId: String,
    val avatarSource: AvatarSource = AvatarSource.Preset(avatarId),
    val level: Int? = null,
    val currentWeek: Int? = null,
    val points: Int? = null,
    val fitnessStatus: String? = null,
    val isLoading: Boolean = false,
    val loadError: Boolean = false
)

@Composable
internal fun ProfileSummaryScreen(data: ProfileSummaryData, onNavigateBack: () -> Unit) {
    SettingsScaffold(title = "個人資料摘要", onNavigateBack = onNavigateBack) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = SettingsPagePadding, vertical = SettingsSectionSpacing),
            verticalArrangement = Arrangement.spacedBy(SettingsSectionSpacing)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileAvatar(
                        avatarSource = data.avatarSource,
                        avatarColorId = data.avatarColorId,
                        size = 104.dp,
                        contentDescription = "${data.displayName}的社群頭像"
                    )
                    Text(
                        data.displayName.ifBlank { "尚未設定顯示名稱" },
                        modifier = Modifier.padding(top = 12.dp),
                        fontSize = 24.scaledSp(),
                        fontWeight = FontWeight.Bold,
                        color = SettingsTextMain
                    )
                }
            }
            item {
                when {
                    data.isLoading -> SettingsCard {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = SettingsSecondaryTeal)
                            Text(
                                "正在載入個人狀態",
                                modifier = Modifier.padding(top = 14.dp),
                                fontSize = 16.scaledSp(),
                                color = SettingsTextSub
                            )
                        }
                    }
                    data.loadError -> SettingsCard {
                        Text(
                            "無法載入資料，請返回首頁後稍後再試。",
                            modifier = Modifier.padding(20.dp),
                            fontSize = 17.scaledSp(),
                            color = SettingsErrorRed
                        )
                    }
                    else -> SettingsCard {
                        data.level?.let {
                            SummaryRow(Icons.Rounded.MilitaryTech, "目前級別", "Lv.$it")
                        }
                        data.fitnessStatus?.let {
                            if (data.level != null) SettingsDivider()
                            SummaryRow(Icons.Rounded.FitnessCenter, "體能狀態", it)
                        }
                        data.currentWeek?.let {
                            if (data.level != null || data.fitnessStatus != null) SettingsDivider()
                            SummaryRow(Icons.Rounded.CalendarMonth, "目前訓練週數", "第 $it 週")
                        }
                        data.points?.let {
                            if (data.level != null || data.fitnessStatus != null || data.currentWeek != null) {
                                SettingsDivider()
                            }
                            SummaryRow(Icons.Rounded.EmojiEvents, "累積點數", "$it P")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconContainer(icon = icon, tint = SettingsSecondaryTeal)
        Spacer(Modifier.width(14.dp))
        Text(label, modifier = Modifier.weight(1f), fontSize = 17.scaledSp(), color = SettingsTextSub)
        Text(value, fontSize = 18.scaledSp(), fontWeight = FontWeight.Bold, color = SettingsTextMain)
    }
}

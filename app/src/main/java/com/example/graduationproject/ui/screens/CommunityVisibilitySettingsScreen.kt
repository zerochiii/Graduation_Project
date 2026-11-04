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
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.graduationproject.ui.theme.scaledSp

@Composable
internal fun CommunityVisibilitySettingsScreen(onNavigateBack: () -> Unit) {
    SettingsScaffold(title = "社群顯示設定", onNavigateBack = onNavigateBack) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = SettingsPagePadding, vertical = SettingsSectionSpacing),
            verticalArrangement = Arrangement.spacedBy(SettingsSectionSpacing)
        ) {
            item {
                SettingsCard {
                    DeferredVisibilityRow(
                        icon = Icons.Rounded.EmojiEvents,
                        title = "顯示我的排行榜",
                        subtitle = "允許其他使用者在社群排行榜看到我"
                    )
                    SettingsDivider()
                    DeferredVisibilityRow(
                        icon = Icons.Rounded.FitnessCenter,
                        title = "顯示我的訓練紀錄",
                        subtitle = "允許好友看到我的訓練進度與成就"
                    )
                    SettingsDivider()
                    DeferredVisibilityRow(
                        icon = Icons.Rounded.MonetizationOn,
                        title = "顯示我的點數",
                        subtitle = "允許好友看到我的累積點數"
                    )
                }
            }
            item {
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    color = SettingsSecondaryTeal.copy(alpha = 0.10f)
                ) {
                    Text(
                        "社群隱私設定將於後續版本開放，目前不影響你的訓練與帳號資料。",
                        modifier = Modifier.padding(18.dp),
                        fontSize = 16.scaledSp(),
                        color = SettingsTextMain
                    )
                }
            }
        }
    }
}

@Composable
private fun DeferredVisibilityRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconContainer(icon = icon, tint = SettingsSecondaryTeal)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 18.scaledSp(), fontWeight = FontWeight.Bold, color = SettingsTextMain)
            Text(subtitle, fontSize = 15.scaledSp(), color = SettingsTextSub)
        }
        Surface(
            modifier = Modifier.semantics { contentDescription = "$title，即將開放" },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
            color = SettingsPrimaryPeach.copy(alpha = 0.14f)
        ) {
            Text(
                "即將開放",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                fontSize = 14.scaledSp(),
                fontWeight = FontWeight.Bold,
                color = SettingsPrimaryPeach
            )
        }
    }
}

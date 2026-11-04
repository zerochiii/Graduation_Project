package com.example.graduationproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.graduationproject.profile.MAX_DISPLAY_NAME_LENGTH
import com.example.graduationproject.profile.AvatarSource
import com.example.graduationproject.ui.components.ProfileAvatar
import com.example.graduationproject.ui.components.ScaleButton
import com.example.graduationproject.ui.theme.scaledSp
import kotlinx.coroutines.launch

@Composable
internal fun DisplayNameSettingsScreen(
    accountId: Int,
    avatarId: String,
    avatarColorId: String,
    avatarSource: AvatarSource = AvatarSource.Preset(avatarId),
    currentDisplayName: String,
    errorMessage: String?,
    onSave: (String) -> Boolean,
    onClearError: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var input by remember(accountId, currentDisplayName) { mutableStateOf(currentDisplayName) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val hasUnsavedChanges = input != currentDisplayName

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onClearError()
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("尚未儲存變更") },
            text = { Text("離開後，本次修改的顯示名稱將不會保留。") },
            confirmButton = {
                TextButton(onClick = onNavigateBack) { Text("捨棄", color = SettingsErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("繼續編輯") }
            }
        )
    }

    SettingsScaffold(
        title = "顯示名稱設定",
        onNavigateBack = onNavigateBack,
        backNavigationEnabled = !hasUnsavedChanges,
        onBackBlocked = { showDiscardDialog = true },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ScaleButton(
                onClick = {
                    if (onSave(input)) {
                        coroutineScope.launch { snackbarHostState.showSnackbar("顯示名稱已更新") }
                    }
                },
                text = "儲存顯示名稱",
                enabled = accountId > 0,
                modifier = Modifier.padding(horizontal = SettingsPagePadding, vertical = SettingsSectionSpacing),
                containerColor = SettingsPrimaryPeach,
                minHeight = SettingsPrimaryCtaHeight
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = SettingsPagePadding, vertical = SettingsSectionSpacing),
            verticalArrangement = Arrangement.spacedBy(SettingsSectionSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                item {
                    ProfileAvatar(
                        avatarSource = avatarSource,
                        avatarColorId = avatarColorId,
                        size = 96.dp,
                        contentDescription = "目前社群頭像"
                    )
                }
                item {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "目前顯示名稱",
                            fontSize = 19.scaledSp(),
                            fontWeight = FontWeight.Bold,
                            color = SettingsTextMain
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = input,
                            onValueChange = { if (it.length <= MAX_DISPLAY_NAME_LENGTH) input = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            supportingText = {
                                Text(
                                    "${input.length}/$MAX_DISPLAY_NAME_LENGTH",
                                    color = SettingsTextSub
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SettingsPrimaryPeach,
                                cursorColor = SettingsPrimaryPeach
                            )
                        )
                    }
                }
                item {
                    SettingsCard {
                        Text(
                            "這是你在社群中顯示的名稱，讓其他成員更容易認識你。",
                            modifier = Modifier.padding(18.dp),
                            fontSize = 16.scaledSp(),
                            color = SettingsTextSub
                        )
                    }
                }
        }
    }
}

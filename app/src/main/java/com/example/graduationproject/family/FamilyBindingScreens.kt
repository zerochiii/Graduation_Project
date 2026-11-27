package com.example.graduationproject.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.ui.screens.SettingsBeigeBg
import com.example.graduationproject.ui.screens.SettingsCard
import com.example.graduationproject.ui.screens.SettingsErrorRed
import com.example.graduationproject.ui.screens.SettingsPrimaryPeach
import com.example.graduationproject.ui.screens.SettingsScaffold
import com.example.graduationproject.ui.screens.SettingsTextMain
import com.example.graduationproject.ui.screens.SettingsTextSub
import com.example.graduationproject.ui.theme.GraduationProjectTheme

sealed interface ElderBindingCodeUiState {
    data object Loading : ElderBindingCodeUiState
    data class Available(val code: String) : ElderBindingCodeUiState
    data object Unavailable : ElderBindingCodeUiState
    data class Error(val message: String) : ElderBindingCodeUiState
}

@Composable
fun ElderFamilyBindingScreen(
    state: ElderBindingCodeUiState,
    onCopyCode: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    SettingsScaffold(title = "家屬綁定", onNavigateBack = onNavigateBack) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "讓您信任的家屬查看您分享的 FitCare 資訊。",
                    color = SettingsTextSub,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
            item {
                ElderBindingCodeCard(state = state, onCopyCode = onCopyCode)
            }
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Rounded.Shield, null, tint = SettingsPrimaryPeach, modifier = Modifier.size(30.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("安全提醒", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SettingsTextMain)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "請將此綁定碼提供給您信任的家屬。請勿將綁定碼提供給不認識的人。",
                                fontSize = 15.sp,
                                lineHeight = 23.sp,
                                color = SettingsTextSub
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ElderBindingCodeCard(
    state: ElderBindingCodeUiState,
    onCopyCode: (String) -> Unit
) {
    SettingsCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("我的綁定碼", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = SettingsTextMain)
            Spacer(Modifier.height(18.dp))
            when (state) {
                ElderBindingCodeUiState.Loading -> {
                    CircularProgressIndicator(color = SettingsPrimaryPeach, modifier = Modifier.size(44.dp))
                    Spacer(Modifier.height(14.dp))
                    Text("正在取得綁定碼…", color = SettingsTextSub, fontSize = 16.sp)
                }
                is ElderBindingCodeUiState.Available -> {
                    Surface(shape = RoundedCornerShape(18.dp), color = SettingsBeigeBg) {
                        Text(
                            state.code,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp,
                            color = SettingsTextMain
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = { onCopyCode(state.code) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SettingsPrimaryPeach)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, null)
                        Spacer(Modifier.width(8.dp))
                        Text("複製綁定碼", fontSize = 17.sp)
                    }
                }
                ElderBindingCodeUiState.Unavailable -> {
                    BindingStatusIcon(Icons.Rounded.LinkOff, SettingsPrimaryPeach)
                    Spacer(Modifier.height(14.dp))
                    Text("目前暫時無法取得綁定碼", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = SettingsTextMain, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text("家屬綁定服務目前尚未開放，請稍後再試。", fontSize = 16.sp, color = SettingsTextSub, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, null)
                        Spacer(Modifier.width(8.dp))
                        Text("複製綁定碼", fontSize = 17.sp)
                    }
                }
                is ElderBindingCodeUiState.Error -> {
                    BindingStatusIcon(Icons.Rounded.ErrorOutline, SettingsErrorRed)
                    Spacer(Modifier.height(14.dp))
                    Text("綁定碼載入失敗", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = SettingsTextMain)
                    Spacer(Modifier.height(8.dp))
                    Text(state.message, fontSize = 16.sp, color = SettingsTextSub, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun BindingStatusIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Surface(shape = CircleShape, color = tint.copy(alpha = 0.12f)) {
        Icon(icon, null, tint = tint, modifier = Modifier.padding(15.dp).size(38.dp))
    }
}

@Composable
fun BindElderScreen(
    viewModel: FamilyBindingViewModel,
    onBindingSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val form by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState == FamilyBindUiState.Success) {
            viewModel.consumeSuccess()
            onBindingSuccess()
        }
    }

    BindElderContent(
        form = form,
        uiState = uiState,
        onCodeChange = viewModel::updateBindingCode,
        onRelationshipSelected = viewModel::selectRelationship,
        onSubmit = viewModel::submit,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun BindElderContent(
    form: FamilyBindFormState,
    uiState: FamilyBindUiState,
    onCodeChange: (String) -> Unit,
    onRelationshipSelected: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val submitting = uiState == FamilyBindUiState.Submitting
    Scaffold(
        containerColor = FamilyBackground,
        topBar = {
            TopAppBar(
                title = { Text("綁定長輩", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !submitting, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回家屬首頁")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FamilyPurple,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize().imePadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    "請輸入長輩在 FitCare 個人化設定中心提供的綁定碼。",
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    color = FamilyTextSecondary
                )
            }
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        OutlinedTextField(
                            value = form.bindingCode,
                            onValueChange = onCodeChange,
                            label = { Text("綁定碼") },
                            placeholder = { Text("請輸入長輩提供的綁定碼") },
                            leadingIcon = { Icon(Icons.Rounded.Key, null) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                            enabled = !submitting,
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(Modifier.height(22.dp))
                        Text("與長輩的關係", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FamilyText)
                        Spacer(Modifier.height(10.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FAMILY_RELATIONSHIP_OPTIONS.forEach { relationship ->
                                val selected = form.relationship == relationship
                                FilterChip(
                                    selected = selected,
                                    onClick = { onRelationshipSelected(relationship) },
                                    enabled = !submitting,
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                    label = { Text(relationship, fontSize = 16.sp) },
                                    modifier = Modifier.heightIn(min = 48.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FamilyPurpleLight,
                                        selectedLabelColor = FamilyPurple
                                    )
                                )
                            }
                        }
                    }
                }
            }
            when (uiState) {
                is FamilyBindUiState.Error -> item { BindOperationMessage(Icons.Rounded.ErrorOutline, "綁定失敗", uiState.message, RiskWarning) }
                is FamilyBindUiState.Unavailable -> item { BindOperationMessage(Icons.Rounded.LinkOff, "目前無法完成綁定", uiState.message, FamilyPurple) }
                FamilyBindUiState.Success -> item { BindOperationMessage(Icons.Rounded.Shield, "綁定成功", "正在返回家屬首頁…", RiskSafe) }
                else -> Unit
            }
            item {
                Button(
                    onClick = onSubmit,
                    enabled = form.canSubmit && !submitting,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FamilyPurple)
                ) {
                    if (submitting) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("綁定中…", fontSize = 18.sp)
                    } else {
                        Text("確認綁定", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BindOperationMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    tint: Color
) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.09f))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = FamilyText)
                Spacer(Modifier.height(4.dp))
                Text(message, fontSize = 15.sp, lineHeight = 22.sp, color = FamilyTextSecondary)
            }
        }
    }
}

private val previewFilledForm = FamilyBindFormState("A7K4M9Q2", "母親")

@Preview(name = "Elder Binding - Loading", showBackground = true)
@Composable private fun ElderBindingLoadingPreview() = GraduationProjectTheme { ElderFamilyBindingScreen(ElderBindingCodeUiState.Loading, {}, {}) }

@Preview(name = "Elder Binding - Code", showBackground = true)
@Composable private fun ElderBindingCodePreview() = GraduationProjectTheme { ElderFamilyBindingScreen(ElderBindingCodeUiState.Available("A7K4M9Q2"), {}, {}) }

@Preview(name = "Elder Binding - Unavailable", showBackground = true)
@Composable private fun ElderBindingUnavailablePreview() = GraduationProjectTheme { ElderFamilyBindingScreen(ElderBindingCodeUiState.Unavailable, {}, {}) }

@Preview(name = "Elder Binding - Error", showBackground = true)
@Composable private fun ElderBindingErrorPreview() = GraduationProjectTheme { ElderFamilyBindingScreen(ElderBindingCodeUiState.Error("請稍後再試。"), {}, {}) }

@Preview(name = "Family Bind - Empty", showBackground = true)
@Composable private fun FamilyBindEmptyPreview() = GraduationProjectTheme { BindElderContent(FamilyBindFormState(), FamilyBindUiState.Idle, {}, {}, {}, {}) }

@Preview(name = "Family Bind - Filled", showBackground = true)
@Composable private fun FamilyBindFilledPreview() = GraduationProjectTheme { BindElderContent(previewFilledForm, FamilyBindUiState.Idle, {}, {}, {}, {}) }

@Preview(name = "Family Bind - Submitting", showBackground = true)
@Composable private fun FamilyBindSubmittingPreview() = GraduationProjectTheme { BindElderContent(previewFilledForm, FamilyBindUiState.Submitting, {}, {}, {}, {}) }

@Preview(name = "Family Bind - Error", showBackground = true)
@Composable private fun FamilyBindErrorPreview() = GraduationProjectTheme { BindElderContent(previewFilledForm, FamilyBindUiState.Error("綁定碼無效，請確認後再試。"), {}, {}, {}, {}) }

@Preview(name = "Family Bind - Backend unavailable", showBackground = true)
@Composable private fun FamilyBindUnavailablePreview() = GraduationProjectTheme { BindElderContent(previewFilledForm, FamilyBindUiState.Unavailable("目前暫時無法完成綁定，請稍後再試。"), {}, {}, {}, {}) }

@Preview(name = "Family Bind - Future success", showBackground = true)
@Composable private fun FamilyBindSuccessPreview() = GraduationProjectTheme { BindElderContent(previewFilledForm, FamilyBindUiState.Success, {}, {}, {}, {}) }

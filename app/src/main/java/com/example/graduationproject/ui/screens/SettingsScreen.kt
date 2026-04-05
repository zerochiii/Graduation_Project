package com.example.graduationproject.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import com.example.graduationproject.ui.theme.scaledSp

private val BeigeBg = Color(0xFFFDFCF9)
private val TextMain = Color(0xFF201A18)
private val TextSub = Color(0xFF5D5D5D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("系統設定", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BeigeBg,
                    titleContentColor = TextMain
                )
            )
        },
        containerColor = BeigeBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "個人化設定",
                fontSize = 24.scaledSp(),
                fontWeight = FontWeight.Bold,
                color = TextMain
            )

            Text(
                text = "目前暫無可調整的設定。",
                fontSize = 18.scaledSp(),
                color = TextSub
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    GraduationProjectTheme {
        SettingsScreen(onNavigateBack = {})
    }
}

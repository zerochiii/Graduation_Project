package com.example.graduationproject.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// 1. 建立 LocalFontScale
val LocalFontScale = compositionLocalOf { 1.0f }

// 2. 提供擴充函數 Int.scaledSp()
@Composable
@ReadOnlyComposable
fun Int.scaledSp(): TextUnit {
    return (this * LocalFontScale.current).sp
}

@Composable
fun GraduationProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    fontScale: Float = 1.0f, // 接受字體縮放倍率
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val currentDensity = LocalDensity.current

    // 實作限制總體字體縮放上限，防止極端排版崩潰
    // 當系統字體與 App 內縮放相乘後，限制最高倍率為 2.1f
    val totalFontScale = (currentDensity.fontScale * fontScale).coerceIn(1.0f, 2.1f)

    CompositionLocalProvider(
        LocalFontScale provides fontScale,
        LocalDensity provides Density(
            density = currentDensity.density,
            fontScale = totalFontScale // 使用受限制的倍率
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

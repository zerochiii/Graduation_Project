package com.example.graduationproject.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 針對高齡者優化的縮放按鈕
 * 1. 修正彈跳邏輯：精確縮小至 0.92f，使用物理感強的 Spring 動畫
 * 2. 強制加入觸覺回饋：確保點擊時有明顯震動
 * 3. 視覺強化：按下的陰影明顯減少 (8.dp -> 2.dp)
 * 4. 針對長輩優化：高度預設至少 64.dp，字體預設 22.sp 以上
 * 5. 狀態優化：禁用狀態也維持品牌色 (橘色)，僅透過透明度區分
 */
@Composable
fun ScaleButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = Color(0xFFFF8A65), // PrimaryPeach
    contentColor: Color = Color.White,
    contentDescription: String? = null,
    isElevated: Boolean = true,
    fontSize: androidx.compose.ui.unit.TextUnit = 22.sp,
    icon: ImageVector? = null,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    minHeight: Dp = 64.dp 
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    // 建立縮放動畫：按下時精確縮小至 0.92f，使用物理感強的 Spring 動畫
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    val buttonModifier = modifier
        .fillMaxWidth()
        .heightIn(min = minHeight)
        .scale(scale)
        .semantics {
            contentDescription?.let { this.contentDescription = it }
        }

    val onClickInternal = {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }
    }

    if (isElevated) {
        ElevatedButton(
            onClick = onClickInternal,
            modifier = buttonModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                // 修改：禁用狀態也呈現橘色，但稍微變淡以示區別
                disabledContainerColor = containerColor.copy(alpha = 0.5f),
                disabledContentColor = contentColor.copy(alpha = 0.7f)
            ),
            elevation = ButtonDefaults.elevatedButtonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 2.dp,
                disabledElevation = 0.dp
            ),
            interactionSource = interactionSource
        ) {
            ButtonContent(text, fontSize, icon)
        }
    } else {
        Button(
            onClick = onClickInternal,
            modifier = buttonModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                // 修改：禁用狀態也呈現橘色，但稍微變淡以示區別
                disabledContainerColor = containerColor.copy(alpha = 0.5f),
                disabledContentColor = contentColor.copy(alpha = 0.7f)
            ),
            interactionSource = interactionSource
        ) {
            ButtonContent(text, fontSize, icon)
        }
    }
}

@Composable
private fun ButtonContent(text: String, fontSize: androidx.compose.ui.unit.TextUnit, icon: ImageVector?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = text, 
            fontSize = fontSize, 
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ScaleFAB(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFFF8A65),
    contentColor: Color = Color.White,
    icon: @Composable (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fabScale"
    )
    val haptic = LocalHapticFeedback.current

    val onClickInternal = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onClick()
    }

    ExtendedFloatingActionButton(
        onClick = onClickInternal,
        modifier = modifier
            .scale(scale)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        containerColor = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(40.dp),
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        ),
        interactionSource = interactionSource
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

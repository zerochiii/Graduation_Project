package com.example.graduationproject.assessment

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.graduationproject.ui.theme.GraduationProjectTheme

private val DialogWarmWhite = Color(0xFFFFFBF6)
private val DialogWarmCream = Color(0xFFFFF4E9)
private val DialogTitle = Color(0xFF0B2D45)
private val DialogBody = Color(0xFF70757B)
private val ButtonOrangeStart = Color(0xFFFF8550)
private val ButtonOrangeEnd = Color(0xFFFF672F)
private val MedalYellow = Color(0xFFFFC83D)
private val MedalYellowLight = Color(0xFFFFD967)
private val MedalRing = Color(0xFFFFAA16)
private val RibbonRed = Color(0xFFFF4F35)
private val RibbonOrange = Color(0xFFFF6B3D)

private val AssessmentSpeechBubbleShape = SpeechBubbleShape(
    cornerRadius = 38.dp,
    tailWidth = 54.dp,
    tailHeight = 24.dp
)

@Composable
internal fun AssessmentCompletionDialog(onAcknowledge: () -> Unit) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
                .padding(horizontal = 18.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            AssessmentCompletionDialogCard(
                availableHeight = maxHeight,
                onAcknowledge = onAcknowledge
            )
        }
    }
}

@Composable
private fun AssessmentCompletionDialogCard(
    availableHeight: Dp,
    onAcknowledge: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val compactLayout = availableHeight < 680.dp || density.fontScale >= 1.45f
    val outerTopPadding = if (compactLayout) 24.dp else 34.dp
    val medalHeight = if (compactLayout) 142.dp else 174.dp
    val sectionSpacing = if (compactLayout) 12.dp else 18.dp

    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .widthIn(max = 430.dp)
            .heightIn(max = availableHeight * 0.9f)
            .shadow(
                elevation = 24.dp,
                shape = AssessmentSpeechBubbleShape,
                ambientColor = Color.Black.copy(alpha = 0.24f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(AssessmentSpeechBubbleShape)
            .background(
                Brush.verticalGradient(
                    0f to DialogWarmWhite,
                    0.68f to DialogWarmWhite,
                    1f to DialogWarmCream
                )
            )
    ) {
        PeachCloudDecoration(Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 22.dp,
                    top = outerTopPadding,
                    end = 22.dp,
                    bottom = 42.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AssessmentCompletionMedal(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(medalHeight)
            )

            Spacer(Modifier.height(sectionSpacing))

            Text(
                text = ASSESSMENT_COMPLETION_NOTICE_TITLE,
                color = DialogTitle,
                fontSize = if (compactLayout) 32.sp else 38.sp,
                lineHeight = if (compactLayout) 40.sp else 46.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(if (compactLayout) 12.dp else 18.dp))

            Text(
                text = ASSESSMENT_COMPLETION_NOTICE_BODY,
                color = DialogBody,
                fontSize = if (compactLayout) 18.sp else 21.sp,
                lineHeight = if (compactLayout) 27.sp else 31.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(if (compactLayout) 22.dp else 30.dp))

            AssessmentCompletionButton(onClick = onAcknowledge)
        }
    }
}

@Composable
private fun AssessmentCompletionButton(onClick: () -> Unit) {
    val buttonShape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .shadow(
                elevation = 10.dp,
                shape = buttonShape,
                ambientColor = ButtonOrangeEnd.copy(alpha = 0.3f),
                spotColor = ButtonOrangeEnd.copy(alpha = 0.38f)
            )
            .clip(buttonShape)
            .background(
                Brush.horizontalGradient(
                    listOf(ButtonOrangeStart, ButtonOrangeEnd)
                )
            )
            .semantics { role = Role.Button }
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 17.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ASSESSMENT_COMPLETION_NOTICE_ACTION,
            color = Color.White,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AssessmentCompletionMedal(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val scale = (size.height / 174.dp.toPx()).coerceAtMost(size.width / 330.dp.toPx())
        val medalRadius = 61.dp.toPx() * scale
        val medalCenter = Offset(centerX, 65.dp.toPx() * scale)

        fun scaled(value: Float) = value.dp.toPx() * scale

        val leftRibbon = Path().apply {
            moveTo(centerX - scaled(43f), scaled(94f))
            lineTo(centerX - scaled(74f), scaled(151f))
            lineTo(centerX - scaled(43f), scaled(145f))
            lineTo(centerX - scaled(29f), scaled(171f))
            lineTo(centerX - scaled(5f), scaled(112f))
            close()
        }
        val rightRibbon = Path().apply {
            moveTo(centerX + scaled(43f), scaled(94f))
            lineTo(centerX + scaled(74f), scaled(151f))
            lineTo(centerX + scaled(43f), scaled(145f))
            lineTo(centerX + scaled(29f), scaled(171f))
            lineTo(centerX + scaled(5f), scaled(112f))
            close()
        }
        drawPath(
            path = leftRibbon,
            brush = Brush.verticalGradient(
                listOf(RibbonOrange, RibbonRed),
                startY = scaled(92f),
                endY = scaled(171f)
            )
        )
        drawPath(
            path = rightRibbon,
            brush = Brush.verticalGradient(
                listOf(RibbonOrange, RibbonRed),
                startY = scaled(92f),
                endY = scaled(171f)
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MedalYellowLight, MedalYellow),
                center = medalCenter - Offset(scaled(18f), scaled(20f)),
                radius = medalRadius * 1.4f
            ),
            radius = medalRadius,
            center = medalCenter
        )
        drawCircle(
            color = MedalRing,
            radius = medalRadius - scaled(13f),
            center = medalCenter,
            style = Stroke(width = scaled(4f))
        )

        val checkPath = Path().apply {
            moveTo(centerX - scaled(26f), medalCenter.y + scaled(1f))
            lineTo(centerX - scaled(8f), medalCenter.y + scaled(19f))
            lineTo(centerX + scaled(31f), medalCenter.y - scaled(24f))
        }
        drawPath(
            path = checkPath,
            color = Color.White,
            style = Stroke(
                width = scaled(13f),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        drawConfetti(Offset(centerX - scaled(112f), scaled(46f)), Color(0xFFFF7377), -42f, scale)
        drawConfetti(Offset(centerX + scaled(111f), scaled(46f)), Color(0xFF5FAFF5), 42f, scale)
        drawConfetti(Offset(centerX - scaled(138f), scaled(104f)), Color(0xFF55CDBA), 38f, scale)
        drawConfetti(Offset(centerX + scaled(139f), scaled(100f)), Color(0xFFFF844A), -38f, scale)
        drawConfetti(Offset(centerX - scaled(108f), scaled(145f)), Color(0xFFFFB934), 45f, scale)
        drawConfetti(Offset(centerX + scaled(108f), scaled(143f)), Color(0xFFFFB934), -45f, scale)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawConfetti(
    center: Offset,
    color: Color,
    degrees: Float,
    scale: Float
) {
    val width = 22.dp.toPx() * scale
    val height = 10.dp.toPx() * scale
    rotate(degrees = degrees, pivot = center) {
        drawRoundRect(
            color = color,
            topLeft = Offset(center.x - width / 2f, center.y - height / 2f),
            size = Size(width, height),
            cornerRadius = CornerRadius(height / 2f)
        )
    }
}

@Composable
private fun PeachCloudDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val peach = Color(0xFFFFD7BA).copy(alpha = 0.12f)
        val peachSoft = Color(0xFFFFE7D5).copy(alpha = 0.22f)
        val y = size.height * 0.74f

        drawCircle(peachSoft, size.width * 0.18f, Offset(-size.width * 0.02f, y))
        drawCircle(peach, size.width * 0.14f, Offset(size.width * 0.13f, y + size.width * 0.07f))
        drawCircle(peachSoft, size.width * 0.18f, Offset(size.width * 1.02f, y))
        drawCircle(peach, size.width * 0.14f, Offset(size.width * 0.87f, y + size.width * 0.07f))
        drawCircle(peach.copy(alpha = 0.08f), size.width * 0.23f, Offset(-size.width * 0.02f, size.height * 0.96f))
        drawCircle(peach.copy(alpha = 0.08f), size.width * 0.23f, Offset(size.width * 1.02f, size.height * 0.96f))
    }
}

private class SpeechBubbleShape(
    private val cornerRadius: androidx.compose.ui.unit.Dp,
    private val tailWidth: androidx.compose.ui.unit.Dp,
    private val tailHeight: androidx.compose.ui.unit.Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radius = with(density) { cornerRadius.toPx() }.coerceAtMost(size.width / 2f)
        val tailHalf = with(density) { tailWidth.toPx() } / 2f
        val tail = with(density) { tailHeight.toPx() }
        val bodyBottom = size.height - tail
        val center = size.width / 2f

        val path = Path().apply {
            moveTo(radius, 0f)
            lineTo(size.width - radius, 0f)
            quadraticTo(size.width, 0f, size.width, radius)
            lineTo(size.width, bodyBottom - radius)
            quadraticTo(size.width, bodyBottom, size.width - radius, bodyBottom)
            lineTo(center + tailHalf, bodyBottom)
            quadraticTo(center + tailHalf * 0.62f, bodyBottom, center + 5f, size.height - 3f)
            quadraticTo(center, size.height + 1f, center - 5f, size.height - 3f)
            quadraticTo(center - tailHalf * 0.62f, bodyBottom, center - tailHalf, bodyBottom)
            lineTo(radius, bodyBottom)
            quadraticTo(0f, bodyBottom, 0f, bodyBottom - radius)
            lineTo(0f, radius)
            quadraticTo(0f, 0f, radius, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun AssessmentCompletionDialogPreview() {
    GraduationProjectTheme(dynamicColor = false) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF4D4D4D)),
            contentAlignment = Alignment.Center
        ) {
            AssessmentCompletionDialogCard(
                availableHeight = maxHeight,
                onAcknowledge = {}
            )
        }
    }
}

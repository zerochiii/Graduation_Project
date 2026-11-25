package com.example.graduationproject.family

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graduationproject.ui.theme.GraduationProjectTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class ElderAbilityMetric(
    val label: String,
    val value: Float
)

private val RadarPurple = Color(0xFF6C4BA5)
private val RadarGrid = Color(0xFFD9D1E7)
private val RadarText = Color(0xFF4E4658)

@Composable
fun ElderAbilityRadarChart(
    metrics: List<ElderAbilityMetric>?,
    modifier: Modifier = Modifier
) {
    val validMetrics = metrics?.takeIf { list ->
        list.size == 5 && list.all { it.value in 0f..1f }
    }
    if (validMetrics == null) {
        RadarNoData(modifier)
        return
    }

    val summary = validMetrics.joinToString("，") {
        "${it.label} ${(it.value * 100).toInt()} 分"
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "體能能力分析：$summary" }
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(280.dp)) {
            drawRadar(validMetrics)
        }
        Spacer(Modifier.height(10.dp))
        validMetrics.chunked(2).forEach { rowMetrics ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowMetrics.forEach { metric ->
                    Text(
                        text = "${metric.label} ${(metric.value * 100).toInt()}",
                        modifier = Modifier.weight(1f),
                        color = RadarText,
                        fontSize = 14.sp
                    )
                }
                if (rowMetrics.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RadarNoData(modifier: Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFF3EFF9),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.Insights, null, tint = RadarPurple, modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(12.dp))
            Text("尚無完整能力分析資料", fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "完成五項能力資料同步後，才能顯示可靠的體能分析圖。",
                fontSize = 15.sp,
                color = RadarText,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun DrawScope.drawRadar(metrics: List<ElderAbilityMetric>) {
    val center = Offset(size.width / 2f, size.height / 2f + 8.dp.toPx())
    val radius = min(size.width, size.height) * 0.31f
    val count = metrics.size

    for (level in 1..4) {
        val levelRadius = radius * level / 4f
        drawPath(
            path = radarPath(center, levelRadius, List(count) { 1f }),
            color = RadarGrid,
            style = Stroke(1.dp.toPx())
        )
    }
    repeat(count) { index ->
        drawLine(
            color = RadarGrid,
            start = center,
            end = radarPoint(center, radius, index, count),
            strokeWidth = 1.dp.toPx()
        )
    }

    val valuePath = radarPath(center, radius, metrics.map { it.value.coerceIn(0f, 1f) })
    drawPath(valuePath, RadarPurple.copy(alpha = 0.22f))
    drawPath(valuePath, RadarPurple, style = Stroke(2.dp.toPx()))
    metrics.forEachIndexed { index, metric ->
        drawCircle(
            color = RadarPurple,
            radius = 4.dp.toPx(),
            center = radarPoint(center, radius * metric.value.coerceIn(0f, 1f), index, count)
        )
    }

    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = RadarText.toArgb()
        textSize = 13.sp.toPx()
        textAlign = Paint.Align.CENTER
    }
    metrics.forEachIndexed { index, metric ->
        val point = radarPoint(center, radius + 31.dp.toPx(), index, count)
        drawContext.canvas.nativeCanvas.drawText(
            metric.label,
            point.x,
            point.y + labelPaint.textSize / 3f,
            labelPaint
        )
    }
}

private fun radarPath(center: Offset, radius: Float, values: List<Float>): Path = Path().apply {
    values.forEachIndexed { index, value ->
        val point = radarPoint(center, radius * value, index, values.size)
        if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
    }
    close()
}

private fun radarPoint(center: Offset, radius: Float, index: Int, count: Int): Offset {
    val angle = -Math.PI / 2.0 + 2.0 * Math.PI * index / count
    return Offset(
        x = center.x + (cos(angle) * radius).toFloat(),
        y = center.y + (sin(angle) * radius).toFloat()
    )
}

@Preview(name = "Radar Sample Preview", showBackground = true)
@Composable
private fun RadarSamplePreview() = GraduationProjectTheme {
    ElderAbilityRadarChart(
        metrics = listOf(
            ElderAbilityMetric("平衡能力", 0.78f),
            ElderAbilityMetric("協調能力", 0.62f),
            ElderAbilityMetric("步行能力", 0.84f),
            ElderAbilityMetric("下肢肌力", 0.70f),
            ElderAbilityMetric("起立坐下", 0.58f)
        ),
        modifier = Modifier.padding(20.dp)
    )
}

@Preview(name = "Radar No Data", showBackground = true)
@Composable
private fun RadarNoDataPreview() = GraduationProjectTheme {
    ElderAbilityRadarChart(metrics = null, modifier = Modifier.padding(20.dp))
}

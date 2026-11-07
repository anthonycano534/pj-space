package com.pjspace.launcher

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable fun LauncherIcon(settings: Boolean) {
    Canvas(Modifier.size(24.dp)) {
        val unit = size.minDimension / 24f
        if (settings) {
            val path = Path()
            repeat(32) { index ->
                val radius = if (index % 4 == 0 || index % 4 == 3) 10.5f else 8f
                val angle = index * Math.PI / 16
                val x = (12 + cos(angle) * radius).toFloat() * unit
                val y = (12 + sin(angle) * radius).toFloat() * unit
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, Color.White, style = Stroke(1.7f * unit))
            drawCircle(Color.White, radius = 3.3f * unit, style = Stroke(1.7f * unit))
        } else {
            drawLine(Color.White, Offset(10f * unit, 14f * unit), Offset(20f * unit, 4f * unit), strokeWidth = 4f * unit, cap = StrokeCap.Round)
            val brush = Path().apply {
                moveTo(9f * unit, 13f * unit); cubicTo(3f * unit, 13f * unit, 8f * unit, 19f * unit, 3f * unit, 21f * unit)
                cubicTo(10f * unit, 22f * unit, 15f * unit, 18f * unit, 9f * unit, 13f * unit); close()
            }
            drawPath(brush, Color.White)
        }
    }
}

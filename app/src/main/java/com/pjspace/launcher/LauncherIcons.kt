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

@Composable fun LauncherIcon(settings: Boolean, modifier: Modifier = Modifier.size(34.dp)) {
    Canvas(modifier) {
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

@Composable fun SearchIcon(color: Color = Color.White) {
    Canvas(Modifier.size(28.dp)) {
        val unit = size.minDimension / 24f
        drawCircle(color, 6.5f * unit, Offset(9.5f * unit, 9.5f * unit), style = Stroke(1.8f * unit))
        drawLine(color, Offset(14.5f * unit, 14.5f * unit), Offset(21f * unit, 21f * unit), 2f * unit, StrokeCap.Round)
    }
}

@Composable fun PhoneIcon() {
    Canvas(Modifier.size(34.dp)) {
        val u = size.minDimension / 24f
        val path = Path().apply {
            moveTo(5f*u, 2f*u); lineTo(9f*u, 7f*u); lineTo(6.5f*u, 10f*u)
            cubicTo(8f*u, 13.5f*u, 10.5f*u, 16f*u, 14f*u, 17.5f*u)
            lineTo(17f*u, 15f*u); lineTo(22f*u, 19f*u); lineTo(20f*u, 22f*u)
            cubicTo(10f*u, 23f*u, 1f*u, 14f*u, 2f*u, 4f*u); close()
        }
        drawPath(path, Color.White, style = Stroke(1.6f*u))
    }
}

@Composable fun CameraIcon() {
    Canvas(Modifier.size(34.dp)) {
        val u = size.minDimension / 24f
        val path = Path().apply {
            moveTo(2f*u, 7f*u); lineTo(7f*u, 7f*u); lineTo(9f*u, 4f*u)
            lineTo(15f*u, 4f*u); lineTo(17f*u, 7f*u); lineTo(22f*u, 7f*u)
            lineTo(22f*u, 21f*u); lineTo(2f*u, 21f*u); close()
        }
        drawPath(path, Color.White, style = Stroke(1.7f*u))
        drawCircle(Color.White, 4.5f*u, Offset(12f*u, 14f*u), style = Stroke(1.7f*u))
    }
}

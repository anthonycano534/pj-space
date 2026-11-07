package com.pjspace.launcher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable fun AccessSymbol(key: String, modifier: Modifier = Modifier.size(34.dp)) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        fun point(x: Float, y: Float) = Offset(x*u, y*u)
        fun line(x: Float, y: Float, x2: Float, y2: Float) = drawLine(Color.White, point(x,y), point(x2,y2), 1.6f*u, StrokeCap.Round)
        fun polygon(vararg xy: Float, fill: Boolean = false) {
            val p = Path().apply { moveTo(xy[0]*u, xy[1]*u); for (i in 2 until xy.size step 2) lineTo(xy[i]*u, xy[i+1]*u); close() }
            if (fill) drawPath(p, Color.White) else drawPath(p, Color.White, style = Stroke(1.6f*u))
        }
        when (key) {
            "plane" -> polygon(12f,1f,14f,9f,22f,14f,22f,16f,14f,13f,14f,20f,17f,22f,17f,23f,12f,21f,7f,23f,7f,22f,10f,20f,10f,13f,2f,16f,2f,14f,10f,9f,fill=true)
            "book" -> { polygon(2f,4f,10f,4f,12f,6f,14f,4f,22f,4f,22f,20f,14f,20f,12f,22f,10f,20f,2f,20f); line(12f,6f,12f,21f) }
            "home" -> { polygon(2f,11f,12f,2f,22f,11f,20f,11f,20f,22f,14f,22f,14f,15f,10f,15f,10f,22f,4f,22f,4f,11f) }
            "work" -> { polygon(2f,7f,22f,7f,22f,21f,2f,21f); polygon(8f,7f,8f,3f,16f,3f,16f,7f); line(2f,13f,22f,13f); line(12f,11f,12f,15f) }
            "music" -> { line(9f,4f,20f,2f); line(9f,4f,9f,18f); line(20f,2f,20f,16f); drawOval(Color.White,point(3f,16f),Size(6f*u,5f*u)); drawOval(Color.White,point(14f,14f),Size(6f*u,5f*u)) }
            "study" -> { polygon(1f,8f,12f,3f,23f,8f,12f,13f); line(5f,10f,5f,17f); line(5f,17f,12f,20f); line(12f,20f,19f,17f); line(19f,17f,19f,10f); line(23f,8f,23f,17f) }
            "cart" -> { line(1f,3f,4f,3f); polygon(4f,3f,7f,17f,20f,17f,23f,7f,5f,7f); drawCircle(Color.White,1.7f*u,point(8f,21f)); drawCircle(Color.White,1.7f*u,point(19f,21f)) }
            "health" -> polygon(9f,2f,15f,2f,15f,9f,22f,9f,22f,15f,15f,15f,15f,22f,9f,22f,9f,15f,2f,15f,2f,9f,9f,9f)
            "tools" -> { polygon(17f,2f,14f,6f,18f,10f,22f,7f,22f,12f,18f,14f,14f,12f,6f,22f,2f,18f,12f,10f,10f,6f,12f,2f); }
            "phone" -> { val p=Path().apply { moveTo(5f*u,2f*u); lineTo(9f*u,7f*u); lineTo(6.5f*u,10f*u); cubicTo(8f*u,13.5f*u,10.5f*u,16f*u,14f*u,17.5f*u); lineTo(17f*u,15f*u); lineTo(22f*u,19f*u); lineTo(20f*u,22f*u); cubicTo(10f*u,23f*u,1f*u,14f*u,2f*u,4f*u); close() }; drawPath(p,Color.White,style=Stroke(1.6f*u)) }
            "camera" -> { polygon(2f,7f,7f,7f,9f,4f,15f,4f,17f,7f,22f,7f,22f,21f,2f,21f); drawCircle(Color.White,4.5f*u,point(12f,14f),style=Stroke(1.7f*u)) }
            else -> { val values = (0 until 10).flatMap { i -> val a=-Math.PI/2+i*Math.PI/5; val r=if(i%2==0) 10f else 4.3f; listOf((12+cos(a)*r).toFloat(),(12+sin(a)*r).toFloat()) }; polygon(*values.toFloatArray()) }
        }
    }
}

@Composable fun AccessIconPicker(selected: String, onSelect: (String) -> Unit) {
    LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.fillMaxWidth().height(225.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(accessIcons, key = { it.key }) { icon ->
            OutlinedButton(onClick = { onSelect(icon.key) }, modifier = Modifier.height(70.dp), contentPadding = PaddingValues(4.dp),
                border = BorderStroke(if (selected == icon.key) 2.dp else 1.dp, Color.White), shape = RoundedCornerShape(8.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AccessSymbol(icon.key, Modifier.size(28.dp)); Text(icon.name, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

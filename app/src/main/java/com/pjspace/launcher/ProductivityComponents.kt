package com.pjspace.launcher

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@Composable fun MonthlyCalendar() {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var today by remember { mutableStateOf(LocalDate.now()) }
    var offset by remember { mutableIntStateOf(0) }
    LaunchedEffect(lifecycle) { lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        while (true) { today = LocalDate.now(); delay(60_000 - System.currentTimeMillis()%60_000 + 25) }
    } }
    val locale = Locale.getDefault()
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    val month = YearMonth.from(today).plusMonths(offset.toLong())
    val days = monthGrid(month, firstDay)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val rowHeight = ((maxHeight.value - 48f) / 7f).coerceAtLeast(8f).dp
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { offset-- }, contentPadding = PaddingValues(0.dp), modifier = Modifier.width(40.dp).semantics { contentDescription = "Mes anterior" }) { Text("‹", fontSize=24.sp) }
                Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale)), Modifier.weight(1f), maxLines=1, overflow=TextOverflow.Ellipsis)
                TextButton(onClick = { offset++ }, contentPadding = PaddingValues(0.dp), modifier = Modifier.width(40.dp).semantics { contentDescription = "Mes siguiente" }) { Text("›", fontSize=24.sp) }
            }
            Row(Modifier.fillMaxWidth().height(rowHeight)) {
                repeat(7) { index -> Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment=Alignment.Center) {
                    Text(firstDay.plus(index.toLong()).getDisplayName(TextStyle.NARROW,locale), fontSize=12.sp)
                } }
            }
            repeat(6) { week -> Row(Modifier.fillMaxWidth().height(rowHeight)) {
                repeat(7) { column ->
                    val date=days[week*7+column]
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment=Alignment.Center) {
                        if (date != null) Box(Modifier.size(minOf(rowHeight-2.dp,32.dp).coerceAtLeast(4.dp))
                            .background(if(date==today)Color.White else Color.Transparent,CircleShape)
                            .semantics { contentDescription = date.toString() + if(date==today) ", hoy" else "" }, contentAlignment=Alignment.Center) {
                            Text(date.dayOfMonth.toString(), color=if(date==today)Color.Black else Color.White, fontSize=13.sp)
                        }
                    }
                }
            } }
        }
    }
}

@Composable fun TodoPanel(items: List<TodoItem>, enabled: Boolean, onToggle: (String) -> Unit) {
    val progress=todoProgress(items)
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(48.dp), contentAlignment=Alignment.Center) {
                Canvas(Modifier.fillMaxSize().padding(3.dp)) {
                    drawCircle(Color.White.copy(alpha=.25f),style=Stroke(3.dp.toPx()))
                    drawArc(Color.White,-90f,360f*progress,false,style=Stroke(3.dp.toPx(),cap=StrokeCap.Round))
                }
                Text("${(progress*100).toInt()}%",fontSize=11.sp)
            }
            Column { Text("Pendientes",style=MaterialTheme.typography.titleMedium); Text("${items.count { it.done }} de ${items.size} resueltas",fontSize=12.sp) }
        }
        if(items.isEmpty()) Text("Agregá tareas en Modo edición.",Modifier.padding(top=12.dp),fontSize=14.sp)
        LazyColumn(Modifier.weight(1f)) {
            items(items,key={it.id}) { task ->
                Row(Modifier.fillMaxWidth().clickable(enabled=enabled) { onToggle(task.id) }.padding(vertical=8.dp),
                    verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    Canvas(Modifier.size(28.dp).semantics { contentDescription=if(task.done) "Resuelta" else "Pendiente" }) {
                        drawCircle(Color.White,radius=size.minDimension*.36f,style=if(task.done) androidx.compose.ui.graphics.drawscope.Fill else Stroke(1.5.dp.toPx()))
                        if(task.done) { drawLine(Color.Black,Offset(size.width*.28f,size.height*.5f),Offset(size.width*.43f,size.height*.65f),2.dp.toPx()); drawLine(Color.Black,Offset(size.width*.43f,size.height*.65f),Offset(size.width*.73f,size.height*.33f),2.dp.toPx()) }
                    }
                    Text(task.text,Modifier.weight(1f),fontSize=16.sp,textDecoration=if(task.done)TextDecoration.LineThrough else TextDecoration.None)
                }
            }
        }
    }
}

@Composable fun TodoEditor(initial: List<TodoItem>, onSave: (List<TodoItem>) -> Unit, onDismiss: () -> Unit) {
    var draft by remember { mutableStateOf(initial) }
    var input by remember { mutableStateOf("") }
    fun addPending() { if(input.isNotBlank()) { draft=draft+TodoItem(System.nanoTime().toString(),input.trim());input="" } }
    AlertDialog(onDismissRequest=onDismiss,title={Text("Editar pendientes")},text={
        Column(Modifier.heightIn(max=430.dp)) {
            OutlinedTextField(input,{input=it},label={Text("Nueva tarea")},modifier=Modifier.fillMaxWidth())
            TextButton(onClick={addPending()}){Text("+ Agregar")}
            LazyColumn(Modifier.weight(1f,fill=false)) {
                items(draft,key={it.id}) { task -> Row(verticalAlignment=Alignment.CenterVertically) {
                    OutlinedTextField(task.text,{value->draft=draft.map{if(it.id==task.id)it.copy(text=value) else it}},modifier=Modifier.weight(1f))
                    TextButton(onClick={draft=draft.filterNot{it.id==task.id}}){Text("×")}
                } }
            }
        }
    },confirmButton={TextButton(onClick={addPending();onSave(draft.filter{it.text.isNotBlank()})}){Text("Guardar")}},
        dismissButton={TextButton(onClick=onDismiss){Text("Cancelar")}})
}

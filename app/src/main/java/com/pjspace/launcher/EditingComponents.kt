package com.pjspace.launcher

import android.content.SharedPreferences
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import kotlin.math.roundToInt

fun readBlocks(raw:String?):List<TodoBlock>? = try {
    raw?.let { val a=JSONArray(it); (0 until a.length()).map { index -> val o=a.getJSONObject(index);val tasks=o.getJSONArray("items")
        TodoBlock(o.getString("id"),o.getString("name"),(0 until tasks.length()).map { val task=tasks.getJSONObject(it);TodoItem(task.getString("id"),task.getString("text"),task.optBoolean("done")) })
    } }
} catch(_:Exception){null}
fun writeBlocks(blocks:List<TodoBlock>):String = JSONArray().apply { blocks.forEach { block ->
    put(JSONObject().put("id",block.id).put("name",block.name).put("items",JSONArray().apply {block.items.forEach{put(JSONObject().put("id",it.id).put("text",it.text).put("done",it.done))}}))
} }.toString()
fun readMarks(raw:String?):Map<String,ImportantDay> = try { val o=JSONObject(raw?:"{}");o.keys().asSequence().associateWith {val d=o.getJSONObject(it);ImportantDay(d.optString("title"),d.optString("note"))} }catch(_:Exception){emptyMap()}
fun writeMarks(marks:Map<String,ImportantDay>)=JSONObject().apply {marks.forEach{(date,mark)->put(date,JSONObject().put("title",mark.title).put("note",mark.note))}}.toString()

@Composable fun ImportantDayDialog(date:LocalDate, initial:ImportantDay?, editing:Boolean, onSave:(ImportantDay?)->Unit, onDismiss:()->Unit) {
    var title by remember(date){mutableStateOf(initial?.title.orEmpty())};var note by remember(date){mutableStateOf(initial?.note.orEmpty())}
    AlertDialog(onDismissRequest=onDismiss,title={Text(date.toString())},text={Column(Modifier.verticalScroll(rememberScrollState())) {
        if(editing){
            OutlinedTextField(title,{title=it},label={Text("Título")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(note,{note=it},label={Text("Nota")},modifier=Modifier.fillMaxWidth())
            if(initial!=null)TextButton(onClick={onSave(null)}){Text("Quitar día importante")}
        }else{Text(title.ifBlank{"Día importante"},style=MaterialTheme.typography.titleMedium);Text(note)}
    }},confirmButton={TextButton(onClick={if(editing)onSave(ImportantDay(title.trim(),note))else onDismiss()}){Text(if(editing)"Guardar"else"Cerrar")}},
        dismissButton={if(editing)TextButton(onClick=onDismiss){Text("Cancelar")}})
}

@Composable fun BlockPanel(initial:List<TodoBlock>, editing:Boolean, onChange:(List<TodoBlock>)->Unit) {
    var selected by remember{mutableIntStateOf(0)}
    val index=selected.coerceIn(0,(initial.size-1).coerceAtLeast(0));val block=initial.getOrNull(index)
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            TextButton(onClick={selected=(index-1+initial.size)%initial.size.coerceAtLeast(1)},enabled=initial.size>1){Text("‹")}
            Text(block?.name?:"Sin bloques",Modifier.weight(1f),maxLines=1)
            TextButton(onClick={selected=(index+1)%initial.size.coerceAtLeast(1)},enabled=initial.size>1){Text("›")}
        }
        if(block!=null)TodoPanel(block.items,!editing){id->onChange(initial.map{if(it.id==block.id)it.copy(items=it.items.map{task->if(task.id==id)task.copy(done=!task.done)else task})else it})}
        else Text("Agregá un bloque en Modo edición.")
    }
}

@Composable fun BlocksEditor(initial:List<TodoBlock>, onSave:(List<TodoBlock>)->Unit, onDismiss:()->Unit) {
    var blocks by remember{mutableStateOf(initial)};var selected by remember{mutableIntStateOf(0)}
    var taskEditor by remember{mutableStateOf<String?>(null)};var confirm by remember{mutableStateOf<String?>(null)}
    val block=blocks.getOrNull(selected.coerceIn(0,(blocks.size-1).coerceAtLeast(0)))
    val taskBlock=blocks.find{it.id==taskEditor}
    if(taskBlock!=null)TodoEditor(taskBlock.items,onSave={items->blocks=blocks.map{if(it.id==taskBlock.id)it.copy(items=items)else it};taskEditor=null},onDismiss={taskEditor=null})
    else AlertDialog(onDismissRequest=onDismiss,title={Text("Bloques de pendientes")},text={Column(Modifier.verticalScroll(rememberScrollState())) {
        Row {TextButton(onClick={selected=(selected-1).coerceAtLeast(0)},enabled=selected>0){Text("‹")};Text("${if(block==null)0 else selected+1} / ${blocks.size}",Modifier.padding(12.dp));TextButton(onClick={selected++},enabled=selected<blocks.lastIndex){Text("›")}}
        if(block!=null){
            OutlinedTextField(block.name,{value->blocks=blocks.map{if(it.id==block.id)it.copy(name=value)else it}},label={Text("Nombre del bloque")})
            TextButton(onClick={taskEditor=block.id}){Text("Editar tareas (${block.items.size})")}
            TextButton(onClick={confirm=block.id}){Text("Borrar este bloque")}
        }
        TextButton(onClick={blocks=blocks+TodoBlock(System.nanoTime().toString(),"Nueva lista",emptyList());selected=blocks.lastIndex}){Text("+ Agregar bloque")}
        TextButton(onClick={confirm="all"},enabled=blocks.isNotEmpty()){Text("Borrar todos los bloques")}
    }},confirmButton={TextButton(onClick={onSave(blocks.map{it.copy(name=it.name.ifBlank{"Pendientes"})})}){Text("Guardar")}},dismissButton={TextButton(onClick=onDismiss){Text("Cancelar")}})
    confirm?.let { target -> AlertDialog(onDismissRequest={confirm=null},title={Text("¿Borrar ${if(target=="all")"todos los bloques"else"este bloque"}?")},text={Text("Se eliminarán sus tareas al guardar los cambios.")},
        confirmButton={TextButton(onClick={blocks=if(target=="all")emptyList()else blocks.filterNot{it.id==target};selected=0;confirm=null}){Text("Borrar")}},dismissButton={TextButton(onClick={confirm=null}){Text("Cancelar")}}) }
}

/** Handle moves only this panel; the content retains its own scrolling and click gestures. */
@Composable fun MovableEditBar(prefs:SharedPreferences, content:@Composable RowScope.()->Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density=LocalDensity.current;val w=with(density){maxWidth.toPx()};val h=with(density){maxHeight.toPx()}
        val panelWidth=minOf(maxWidth,360.dp);val panelHeight=96.dp
        val travelX=(w-with(density){panelWidth.toPx()}).coerceAtLeast(0f);val travelY=(h-with(density){panelHeight.toPx()}).coerceAtLeast(0f)
        var x by remember{mutableFloatStateOf(prefs.getFloat("editbar-x",.5f))};var y by remember{mutableFloatStateOf(prefs.getFloat("editbar-y",.8f))}
        Surface(Modifier.offset{IntOffset((x.coerceIn(0f,1f)*travelX).roundToInt(),(y.coerceIn(0f,1f)*travelY).roundToInt())}.width(panelWidth).height(panelHeight).border(1.dp,Color.White,MaterialTheme.shapes.large),shape=MaterialTheme.shapes.large,color=Color.Black) {
            Column {
                Box(Modifier.fillMaxWidth().height(40.dp).pointerInput(travelX,travelY){detectDragGestures(onDragEnd={prefs.edit().putFloat("editbar-x",x).putFloat("editbar-y",y).apply()}){change,delta->change.consume();x=(x+delta.x/travelX.coerceAtLeast(1f)).coerceIn(0f,1f);y=(y+delta.y/travelY.coerceAtLeast(1f)).coerceIn(0f,1f)}},contentAlignment=Alignment.Center){Text("━━━━  Mover barra")}
                Row(Modifier.horizontalScroll(rememberScrollState()),content=content)
            }
        }
    }
}

data class FixedIconPosition(val x:Float,val y:Float,val size:Float)
fun fixedDefault(key:String)=FixedIconPosition(if(key in listOf("gear","camera"))1f else 0f,if(key in listOf("phone","camera"))1f else 0f,64f)
fun fixedPosition(prefs:SharedPreferences,key:String):FixedIconPosition {val d=fixedDefault(key);return FixedIconPosition(prefs.getFloat("fixed-$key-x",d.x),prefs.getFloat("fixed-$key-y",d.y),prefs.getFloat("fixed-$key-size",d.size))}
@Composable fun FixedIconsEditor(prefs:SharedPreferences,onDismiss:()->Unit,onSaved:()->Unit) {
    val keys=listOf("brush" to "Pincel", "gear" to "Ajustes", "phone" to "Inferior izquierdo", "camera" to "Inferior derecho")
    var values by remember{mutableStateOf(keys.associate{it.first to fixedPosition(prefs,it.first)})}
    AlertDialog(onDismissRequest=onDismiss,title={Text("Iconos fijos")},text={Column(Modifier.verticalScroll(rememberScrollState())){keys.forEach{(key,name)->val v=values.getValue(key)
        Text(name,style=MaterialTheme.typography.titleMedium)
        Text("Tamaño: ${v.size.toInt()}");Slider(v.size,{values=values+(key to v.copy(size=it))},valueRange=48f..112f)
        Text("Posición horizontal");Slider(v.x,{values=values+(key to v.copy(x=it))})
        Text("Posición vertical");Slider(v.y,{values=values+(key to v.copy(y=it))})
        TextButton(onClick={values=values+(key to fixedDefault(key))}){Text("Restaurar $name")}
    }}},confirmButton={TextButton(onClick={val e=prefs.edit();values.forEach{(k,v)->e.putFloat("fixed-$k-x",v.x).putFloat("fixed-$k-y",v.y).putFloat("fixed-$k-size",v.size)};e.apply();onSaved()}){Text("Guardar")}},dismissButton={TextButton(onClick=onDismiss){Text("Cancelar")}})
}

@Composable fun FixedIcon(prefs:SharedPreferences,key:String,revision:Int,top:androidx.compose.ui.unit.Dp,bottom:androidx.compose.ui.unit.Dp,onClick:()->Unit,content:@Composable ()->Unit) {
    val v=remember(key,revision){fixedPosition(prefs,key)}
    BoxWithConstraints(Modifier.fillMaxSize().padding(start=8.dp,end=8.dp,top=top+8.dp,bottom=bottom+8.dp)) {
        val density=LocalDensity.current;val size=v.size.dp.coerceAtMost(minOf(maxWidth,maxHeight))
        IconButton(onClick=onClick,modifier=Modifier.offset{IntOffset(with(density){((maxWidth-size)*v.x.coerceIn(0f,1f)).toPx()}.roundToInt(),with(density){((maxHeight-size)*v.y.coerceIn(0f,1f)).toPx()}.roundToInt())}.size(size).background(Color.Black,androidx.compose.foundation.shape.CircleShape)) { Box(Modifier.size(size*.65f),contentAlignment=Alignment.Center){content()} }
    }
}

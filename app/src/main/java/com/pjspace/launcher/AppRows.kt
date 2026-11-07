package com.pjspace.launcher

import android.content.ComponentName
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable fun AppRows(apps:List<Pair<String,ComponentName>>,editing:Boolean,aliases:Map<String,String>,onOpen:(ComponentName)->Unit,
    onMove:(String,String)->Unit,onAction:(String,ComponentName)->Unit) {
    val list=rememberLazyListState();val currentApps by rememberUpdatedState(apps);val move by rememberUpdatedState(onMove)
    var dragging by remember{mutableStateOf<String?>(null)};var dragY by remember{mutableFloatStateOf(0f)}
    var menu by remember{mutableStateOf<ComponentName?>(null)}
    val edge=with(LocalDensity.current){64.dp.toPx()}
    fun reorder(){val id=dragging?:return;val hit=list.layoutInfo.visibleItemsInfo.firstOrNull{dragY>=it.offset && dragY<it.offset+it.size}?.key as? String ?:return;if(id!=hit)move(id,hit)}
    LaunchedEffect(dragging) { while(dragging!=null){val info=list.layoutInfo;val scroll=when {dragY<info.viewportStartOffset+edge -> -12f;dragY>info.viewportEndOffset-edge ->12f;else->0f};if(scroll!=0f){list.scroll {scrollBy(scroll)};reorder()};delay(16)} }
    LaunchedEffect(editing){if(!editing){dragging=null;menu=null}}
    LazyColumn(Modifier.fillMaxSize(),state=list) {
        items(apps,key={it.second.flattenToString()}) { app -> val id=app.second.flattenToString()
            Box(Modifier.fillMaxWidth().heightIn(min=56.dp).background(if(dragging==id)Color.White.copy(alpha=.18f)else Color.Transparent)
                .then(if(editing)Modifier.pointerInput(id,editing){detectDragGestures(onDragStart={offset->dragging=id;dragY=(list.layoutInfo.visibleItemsInfo.find{it.key==id}?.offset?:0)+offset.y},onDragEnd={dragging=null},onDragCancel={dragging=null}){change,delta->change.consume();dragY+=delta.y;reorder()}} else Modifier.combinedClickable(onClick={onOpen(app.second)},onLongClick={menu=app.second}))
                .padding(horizontal=16.dp,vertical=16.dp)) {
                Text(aliases[id]?.takeIf{it.isNotBlank()}?:app.first)
                DropdownMenu(expanded=menu==app.second,onDismissRequest={menu=null}) {
                    listOf("Información de app","Desinstalar","Ocultar","Renombrar").forEach{action->DropdownMenuItem(text={Text(action)},onClick={menu=null;onAction(action,app.second)})}
                }
            }
        }
        if(currentApps.isEmpty())item {Text("No se encontraron aplicaciones.",Modifier.padding(16.dp))}
    }
}

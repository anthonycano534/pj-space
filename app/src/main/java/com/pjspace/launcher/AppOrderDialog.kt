package com.pjspace.launcher

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable fun AppOrderDialog(apps: List<Pair<String,String>>, orderedIds: List<String>, customized: Boolean,
    onSave: (List<String>?) -> Unit, onDismiss: () -> Unit) {
    var draft by remember { mutableStateOf(orderedIds) }
    var custom by remember { mutableStateOf(customized) }
    val labels=apps.toMap()
    val state=rememberLazyListState()
    val scope=rememberCoroutineScope()
    fun move(id:String,delta:Int) {
        val index=draft.indexOf(id);val target=(index+delta).coerceIn(0,(draft.size-1).coerceAtLeast(0))
        draft=moveItem(draft,index,target);custom=true
    }
    AlertDialog(onDismissRequest=onDismiss,title={Text("Orden de aplicaciones")},text={
        Column(Modifier.height(440.dp)) {
            Text("Mantené pulsado ≡ y arrastrá. También podés usar las flechas.",style=MaterialTheme.typography.bodySmall)
            TextButton(onClick={draft=apps.map{it.first};custom=false}) { Text("Volver al orden alfabético") }
            LazyColumn(state=state,modifier=Modifier.weight(1f)) {
                items(draft,key={it}) { id ->
                    Row(Modifier.fillMaxWidth().height(64.dp),verticalAlignment=Alignment.CenterVertically) {
                        Text("≡",fontSize=26.sp,modifier=Modifier.size(40.dp).semantics{contentDescription="Arrastrar ${labels[id]}"}.pointerInput(id) {
                            var distance=0f
                            detectDragGesturesAfterLongPress(onDragStart={distance=0f},onDragEnd={distance=0f},onDragCancel={distance=0f}) { change,amount ->
                                change.consume();distance+=amount.y
                                val step=64.dp.toPx()
                                while(abs(distance)>=step) {
                                    val delta=if(distance>0)1 else -1
                                    move(id,delta);distance-=delta*step
                                    val index=draft.indexOf(id)
                                    if(index>=(state.layoutInfo.visibleItemsInfo.lastOrNull()?.index?:Int.MAX_VALUE)-1)scope.launch{state.scrollBy(step/2)}
                                    if(index<=state.firstVisibleItemIndex+1)scope.launch{state.scrollBy(-step/2)}
                                }
                            }
                        })
                        Text(labels[id]?:id,Modifier.weight(1f),maxLines=1,overflow=TextOverflow.Ellipsis)
                        TextButton(onClick={move(id,-1)},modifier=Modifier.width(36.dp).semantics{contentDescription="Subir ${labels[id]}"},contentPadding=PaddingValues(0.dp)){Text("↑")}
                        TextButton(onClick={move(id,1)},modifier=Modifier.width(36.dp).semantics{contentDescription="Bajar ${labels[id]}"},contentPadding=PaddingValues(0.dp)){Text("↓")}
                    }
                }
            }
        }
    },confirmButton={TextButton(onClick={onSave(if(custom)draft else null)}){Text("Guardar")}},dismissButton={TextButton(onClick=onDismiss){Text("Cancelar")}})
}

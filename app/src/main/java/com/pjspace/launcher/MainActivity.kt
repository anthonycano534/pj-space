package com.pjspace.launcher

import android.app.role.RoleManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("desktop-v1", MODE_PRIVATE) }
    private var page by mutableStateOf("Inicio")
    private var apps by mutableStateOf(emptyList<Pair<String, ComponentName>>())
    private val widgetHost by lazy { AppWidgetHost(this, 1024) }
    private val widgetManager by lazy { AppWidgetManager.getInstance(this) }
    private var addedWidget by mutableStateOf<Int?>(null)
    private var pendingWidget = -1
    private val bindWidget = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) configurePendingWidget() else cancelWidget()
    }
    private val pickWidget = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK) cancelWidget() else {
            val id = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingWidget) ?: pendingWidget
            if (id != pendingWidget) cancelWidget()
            else if (widgetManager.getAppWidgetInfo(id) != null) configurePendingWidget()
            else {
                @Suppress("DEPRECATION")
                val provider = result.data?.getParcelableExtra<ComponentName>(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER)
                if (provider == null) cancelWidget()
                else if (widgetManager.bindAppWidgetIdIfAllowed(id, provider)) configurePendingWidget()
                else try { bindWidget.launch(Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id).putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)) }
                catch (_: Exception) { cancelWidget() }
            }
        }
    }
    private fun addWidget() {
        pendingWidget = widgetHost.allocateAppWidgetId()
        try { pickWidget.launch(Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingWidget)) }
        catch (_: Exception) { cancelWidget(); Toast.makeText(this, "El sistema no ofrece selector de widgets", Toast.LENGTH_SHORT).show() }
    }
    private fun configurePendingWidget() {
        val info = widgetManager.getAppWidgetInfo(pendingWidget) ?: run { cancelWidget(); return }
        if (info.configure == null) finishWidget() else try {
            widgetHost.startAppWidgetConfigureActivityForResult(this, pendingWidget, 0, 800, null)
        } catch (_: Exception) { cancelWidget() }
    }
    @Deprecated("Used by AppWidgetHost configuration flow")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 800) { if (resultCode == RESULT_OK) finishWidget() else cancelWidget() }
    }
    private fun finishWidget() { if (pendingWidget >= 0) addedWidget = pendingWidget; pendingWidget = -1 }
    private fun cancelWidget() { if (pendingWidget >= 0) widgetHost.deleteAppWidgetId(pendingWidget); pendingWidget = -1 }
    override fun onStart() { super.onStart(); widgetHost.startListening() }
    override fun onStop() { widgetHost.stopListening(); super.onStop() }
    override fun onSaveInstanceState(outState: Bundle) { outState.putInt("pendingWidget", pendingWidget); super.onSaveInstanceState(outState) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingWidget = savedInstanceState?.getInt("pendingWidget", -1) ?: -1
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFB5DFC5), background = Color(0xFF101614))) {
                Surface(Modifier.fillMaxSize()) { Home() }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = packageManager.queryIntentActivities(intent, 0).map {
            it.loadLabel(packageManager).toString() to ComponentName(it.activityInfo.packageName, it.activityInfo.name)
        }.filter { it.second.packageName != packageName }.distinctBy { it.second }.sortedBy { it.first.lowercase() }
    }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); page = "Inicio" }
    private fun open(intent: Intent) {
        try { startActivity(intent) } catch (_: Exception) { Toast.makeText(this, "Esta acción no está disponible", Toast.LENGTH_SHORT).show() }
    }
    private fun chooseHome() {
        if (Build.VERSION.SDK_INT >= 29) {
            val manager = getSystemService(RoleManager::class.java)
            if (manager.isRoleAvailable(RoleManager.ROLE_HOME) && !manager.isRoleHeld(RoleManager.ROLE_HOME)) {
                open(manager.createRequestRoleIntent(RoleManager.ROLE_HOME)); return
            }
        }
        open(Intent(Settings.ACTION_HOME_SETTINGS))
    }
    private fun loadTiles(): List<Tile> = try {
        val array = JSONArray(prefs.getString("tiles", null) ?: "[]")
        if (!prefs.contains("tiles")) initialTiles else (0 until array.length()).map {
            val o = array.getJSONObject(it)
            Tile(o.getString("id"), o.getString("kind"), o.getDouble("x").toFloat(), o.getDouble("y").toFloat(), o.getDouble("w").toFloat(), o.getDouble("h").toFloat()).fit()
        }
    } catch (_: Exception) { initialTiles }
    private fun saveTiles(tiles: List<Tile>) {
        val array = JSONArray()
        tiles.forEach { array.put(JSONObject().put("id", it.id).put("kind", it.kind).put("x", it.x).put("y", it.y).put("w", it.w).put("h", it.h)) }
        prefs.edit().putString("tiles", array.toString()).apply()
    }
    @Composable private fun Home() {
        var editing by remember { mutableStateOf(false) }
        var tiles by remember { mutableStateOf(loadTiles()) }
        var query by remember { mutableStateOf("") }
        val notes = remember { mutableStateMapOf<String, String>() }
        var now by remember { mutableStateOf(LocalDateTime.now()) }
        var mappings by remember { mutableStateOf(listOf("Arriba", "Abajo", "Izquierda", "Derecha").associateWith { prefs.getString("gesture-$it", if (it == "Arriba") "Aplicaciones" else "Nada")!! }) }
        LaunchedEffect(addedWidget) {
            addedWidget?.let { id ->
                tiles = tiles + Tile("widget-$id", "Widget:$id", .05f, .05f, .9f, .4f)
                saveTiles(tiles); addedWidget = null
            }
        }
        LaunchedEffect(Unit) { while (true) { now = LocalDateTime.now(); delay(60_000 - System.currentTimeMillis() % 60_000 + 25) } }
        BackHandler(page != "Inicio" || editing) { page = "Inicio"; editing = false }
        fun action(value: String) { when (value) {
            "Aplicaciones" -> page = "Aplicaciones"
            "Ajustes" -> page = "Ajustes"
            "Cámara" -> open(Intent("android.media.action.STILL_IMAGE_CAMERA"))
            "Teléfono" -> open(Intent(Intent.ACTION_DIAL))
        } }
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
            Text("pj. / SPACE", style = MaterialTheme.typography.titleLarge)
            Text("Prender. Resolver. Apagar.", style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { page = "Inicio" }) { Text("Inicio") }
                TextButton(onClick = { page = "Aplicaciones" }) { Text("Apps") }
                TextButton(onClick = { page = "Ajustes" }) { Text("Ajustes") }
            }
            when (page) {
                "Aplicaciones" -> {
                    OutlinedTextField(query, { query = it }, label = { Text("Buscar aplicación") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    LazyColumn(Modifier.weight(1f)) {
                        items(apps.filter { it.first.contains(query, true) }, key = { it.second.flattenToString() }) { app ->
                            TextButton(onClick = { open(Intent.makeMainActivity(app.second)); page = "Inicio" }, modifier = Modifier.fillMaxWidth()) { Text(app.first) }
                        }
                        if (apps.isEmpty()) item { Text("No hay aplicaciones disponibles en este perfil.") }
                    }
                }
                "Ajustes" -> LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { Button(onClick = { chooseHome() }) { Text("Elegir como pantalla de inicio") } }
                    item { Text("Gestos sobre el fondo del escritorio") }
                    items(mappings.keys.toList()) { key ->
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { expanded = true }) { Text("$key: ${mappings[key]}") }
                            DropdownMenu(expanded, { expanded = false }) {
                                listOf("Nada", "Aplicaciones", "Ajustes", "Cámara", "Teléfono").forEach { value ->
                                    DropdownMenuItem(text = { Text(value) }, onClick = {
                                        mappings = mappings + (key to value)
                                        prefs.edit().putString("gesture-$key", value).apply(); expanded = false
                                    })
                                }
                            }
                        }
                    }
                    item { Text("Estrená celular sin comprar otro", style = MaterialTheme.typography.titleMedium) }
                    item { Text("Todo lo útil al alcance de la mano.\nDatos locales. Sin publicidad ni feed.") }
                }
                else -> {
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        TextButton(onClick = { editing = !editing }) { Text(if (editing) "Listo" else "Editar escritorio") }
                        if (editing) TextButton(onClick = {
                            tiles = tiles + Tile(System.nanoTime().toString(), "Nota", .05f, .05f, .8f, .35f); saveTiles(tiles)
                        }) { Text("+ Nota") }
                        if (editing) TextButton(onClick = { addWidget() }) { Text("+ Widget") }
                        if (editing && tiles.none { it.kind == "Reloj" }) TextButton(onClick = {
                            tiles = tiles + initialTiles.first(); saveTiles(tiles)
                        }) { Text("+ Reloj") }
                    }
                    if (editing) Text("Arrastrá la barra superior. Cambiá el tamaño desde ↘.", style = MaterialTheme.typography.bodySmall)
                    BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                        val width = maxWidth; val height = maxHeight
                        val density = LocalDensity.current
                        val wp = with(density) { width.toPx() }.coerceAtLeast(1f)
                        val hp = with(density) { height.toPx() }.coerceAtLeast(1f)
                        Box(Modifier.fillMaxSize().pointerInput(editing, mappings) {
                            var dx = 0f; var dy = 0f
                            detectDragGestures(onDragStart = { dx = 0f; dy = 0f }, onDragEnd = {
                                if (!editing && maxOf(abs(dx), abs(dy)) > 64.dp.toPx()) action(mappings[direction(dx, dy)] ?: "Nada")
                            }) { change, delta -> change.consume(); dx += delta.x; dy += delta.y }
                        })
                        tiles.forEach { tile ->
                            key(tile.id) {
                                fun update(dx: Float, dy: Float, resize: Boolean) {
                                    tiles = tiles.map { current -> if (current.id != tile.id) current else {
                                        if (resize) current.copy(w = current.w + dx / wp, h = current.h + dy / hp).fit()
                                        else current.copy(x = current.x + dx / wp, y = current.y + dy / hp).fit()
                                    } }
                                }
                                Surface(Modifier.offset { IntOffset((tile.x * wp).roundToInt(), (tile.y * hp).roundToInt()) }.size(width * tile.w, height * tile.h), shape = MaterialTheme.shapes.large, color = Color(0xFF202D27)) {
                                    Column(Modifier.padding(12.dp)) {
                                        if (editing) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Text("Mover ${if (tile.kind.startsWith("Widget:")) "widget" else tile.kind}", Modifier.weight(1f).heightIn(min = 48.dp).pointerInput(tile.id, wp, hp) {
                                                detectDragGestures(onDragEnd = { saveTiles(tiles) }, onDragCancel = { saveTiles(tiles) }) { change, delta -> change.consume(); update(delta.x, delta.y, false) }
                                            })
                                            TextButton(onClick = {
                                                tile.kind.substringAfter("Widget:", "").toIntOrNull()?.let { widgetHost.deleteAppWidgetId(it) }
                                                tiles = tiles.filterNot { it.id == tile.id }; saveTiles(tiles)
                                            }) { Text("×") }
                                        }
                                        Box(Modifier.weight(1f).fillMaxWidth()) {
                                            if (tile.kind.startsWith("Widget:")) {
                                                val widgetId = tile.kind.substringAfter(":").toInt()
                                                val info = widgetManager.getAppWidgetInfo(widgetId)
                                                if (info == null) Text("Widget no disponible. Podés quitarlo en Editar.")
                                                else AndroidView(factory = { widgetHost.createView(it, widgetId, info) }, modifier = Modifier.fillMaxSize(), update = { view ->
                                                    view.updateAppWidgetSize(null, (width.value * tile.w).toInt(), (height.value * tile.h - 80).toInt().coerceAtLeast(40), (width.value * tile.w).toInt(), (height.value * tile.h - 80).toInt().coerceAtLeast(40))
                                                })
                                            } else if (tile.kind == "Reloj") Column {
                                                Text(now.format(DateTimeFormatter.ofPattern("HH:mm")), style = MaterialTheme.typography.headlineLarge)
                                                Text(now.format(DateTimeFormatter.ofPattern("EEEE d MMM")))
                                            } else OutlinedTextField(notes[tile.id] ?: prefs.getString("note-${tile.id}", "").orEmpty(), { notes[tile.id] = it; prefs.edit().putString("note-${tile.id}", it).apply() }, placeholder = { Text("¿Qué necesitás resolver?") }, modifier = Modifier.fillMaxSize(), readOnly = editing)
                                        }
                                        if (editing) Text("↘", Modifier.align(Alignment.End).size(48.dp).pointerInput(tile.id, wp, hp) {
                                            detectDragGestures(onDragEnd = { saveTiles(tiles) }, onDragCancel = { saveTiles(tiles) }) { change, delta -> change.consume(); update(delta.x, delta.y, true) }
                                        }, style = MaterialTheme.typography.headlineMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

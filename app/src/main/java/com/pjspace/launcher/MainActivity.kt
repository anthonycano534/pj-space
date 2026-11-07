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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("desktop-v1", MODE_PRIVATE) }
    private var page by mutableStateOf("Inicio")
    private var homeRequest by mutableIntStateOf(0)
    private var apps by mutableStateOf(emptyList<Pair<String, ComponentName>>())
    private val widgetHost by lazy { AppWidgetHost(this, 1024) }
    private var widgetListening = false
    private val widgetManager by lazy { AppWidgetManager.getInstance(this) }
    private var addedWidget by mutableStateOf<Int?>(null)
    private var pendingWidget = -1
    private var pendingWidgetDesktop = CENTRAL_DESKTOP
    private var addedWidgetDesktop = CENTRAL_DESKTOP
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
    private fun addWidget(desktop: Int) {
        pendingWidgetDesktop = desktop
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
    private fun finishWidget() { if (pendingWidget >= 0) { addedWidgetDesktop = pendingWidgetDesktop; addedWidget = pendingWidget }; pendingWidget = -1 }
    private fun cancelWidget() { if (pendingWidget >= 0) widgetHost.deleteAppWidgetId(pendingWidget); pendingWidget = -1 }
    override fun onStart() {
        super.onStart()
        // Own clocks and notes never start the external host; only selected external widgets do.
        if (listOf(LEFT_DESKTOP, CENTRAL_DESKTOP).any { desktop -> loadTiles(desktop).any { it.kind.startsWith("Widget:") } }) {
            widgetHost.startListening(); widgetListening = true
        }
    }
    override fun onStop() { if (widgetListening) { widgetHost.stopListening(); widgetListening = false }; super.onStop() }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("pendingWidget", pendingWidget)
        outState.putInt("pendingWidgetDesktop", pendingWidgetDesktop)
        super.onSaveInstanceState(outState)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingWidget = savedInstanceState?.getInt("pendingWidget", -1) ?: -1
        pendingWidgetDesktop = savedInstanceState?.getInt("pendingWidgetDesktop", CENTRAL_DESKTOP) ?: CENTRAL_DESKTOP
        setContent {
            PJSpaceTheme {
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
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); page = "Inicio"; homeRequest++ }
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
    private fun loadTiles(desktop: Int): List<Tile> = try {
        val storageKey = desktopStorageKey(desktop)
        val array = JSONArray(prefs.getString(storageKey, null) ?: "[]")
        if (!prefs.contains(storageKey)) defaultTiles(desktop) else (0 until array.length()).map {
            val o = array.getJSONObject(it)
            Tile(o.getString("id"), o.getString("kind"), o.getDouble("x").toFloat(), o.getDouble("y").toFloat(), o.getDouble("w").toFloat(), o.getDouble("h").toFloat(),
                if (o.has("fontSizeSp")) o.getDouble("fontSizeSp").toFloat() else null).fit()
        }
    } catch (_: Exception) { defaultTiles(desktop) }
    private fun saveTiles(desktop: Int, tiles: List<Tile>) {
        val array = JSONArray()
        tiles.forEach {
            val item = JSONObject().put("id", it.id).put("kind", it.kind).put("x", it.x).put("y", it.y).put("w", it.w).put("h", it.h)
            it.fontSizeSp?.let { size -> item.put("fontSizeSp", size) }
            array.put(item)
        }
        prefs.edit().putString(desktopStorageKey(desktop), array.toString()).apply()
    }
    @Composable private fun Home() {
        var editing by remember { mutableStateOf(false) }
        val focus = LocalFocusManager.current
        val keyboard = LocalSoftwareKeyboardController.current
        LaunchedEffect(editing) { if (!editing) { focus.clearFocus(force = true); keyboard?.hide() } }
        LaunchedEffect(page) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (page == "Inicio") controller.hide(WindowInsetsCompat.Type.systemBars())
            else controller.show(WindowInsetsCompat.Type.systemBars())
        }
        val layouts = remember {
            mutableStateMapOf(LEFT_DESKTOP to loadTiles(LEFT_DESKTOP), CENTRAL_DESKTOP to loadTiles(CENTRAL_DESKTOP))
        }
        val pager = rememberPagerState(initialPage = CENTRAL_DESKTOP, pageCount = { 3 })
        val scope = rememberCoroutineScope()
        var lastEditableDesktop by remember { mutableIntStateOf(CENTRAL_DESKTOP) }
        LaunchedEffect(pager.settledPage) { if (pager.settledPage != APPS_DESKTOP) lastEditableDesktop = pager.settledPage }
        var selectedGesture by remember { mutableStateOf<String?>(null) }
        var selectedShortcut by remember { mutableStateOf<String?>(null) }
        var cornerApps by remember { mutableStateOf(listOf("Teléfono", "Cámara").associateWith { prefs.getString(cornerBindingKey(it), "").orEmpty() }) }
        var bindings by remember {
            mutableStateOf(listOf("Arriba", "Abajo").associateWith { prefs.getString("gesture-app-$it", "").orEmpty() })
        }
        fun updateLayout(desktop: Int, tiles: List<Tile>, persist: Boolean = true) {
            layouts[desktop] = tiles
            if (persist) saveTiles(desktop, tiles)
        }
        fun goTo(desktop: Int) {
            focus.clearFocus(force = true); keyboard?.hide()
            editing = false
            page = "Inicio"
            scope.launch { pager.animateScrollToPage(desktop) }
        }
        fun openCorner(name: String) {
            focus.clearFocus(force = true); keyboard?.hide()
            val component = ComponentName.unflattenFromString(cornerApps[name].orEmpty())
            open(if (component != null) Intent.makeMainActivity(component) else if (name == "Teléfono") Intent(Intent.ACTION_DIAL) else Intent("android.media.action.STILL_IMAGE_CAMERA"))
        }
        LaunchedEffect(homeRequest) {
            focus.clearFocus(force = true); keyboard?.hide()
            editing = false; pager.scrollToPage(CENTRAL_DESKTOP)
        }
        LaunchedEffect(addedWidget) {
            addedWidget?.let { id ->
                val desktop = addedWidgetDesktop
                updateLayout(desktop, layouts[desktop].orEmpty() + Tile("widget-$id", "Widget:$id", .05f, .05f, .9f, .4f))
                if (!widgetListening) { widgetHost.startListening(); widgetListening = true }
                addedWidget = null
            }
        }
        // Always consume Back. At the central desktop it must never finish the HOME activity.
        BackHandler(enabled = true) {
            focus.clearFocus(force = true); keyboard?.hide()
            when (launcherBackAction(selectedGesture != null || selectedShortcut != null, editing, page == "Ajustes", pager.currentPage)) {
                LauncherBackAction.CLOSE_DIALOG -> { selectedGesture = null; selectedShortcut = null }
                LauncherBackAction.FINISH_EDITING -> editing = false
                LauncherBackAction.OPEN_HOME -> goTo(CENTRAL_DESKTOP)
                LauncherBackAction.STAY_HOME -> Unit
            }
        }
        if (page == "Ajustes") {
            Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { goTo(CENTRAL_DESKTOP) }) { Text("Volver") }
                    Text("Ajustes", style = MaterialTheme.typography.titleLarge)
                }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { OutlinedButton(onClick = { chooseHome() }) { Text("Elegir como pantalla de inicio") } }
                    item { Text("Deslizar a izquierda o derecha cambia de escritorio.") }
                    item { Text("Arriba y abajo abren la aplicación que elijas. Usalos sobre el fondo libre.") }
                    items(listOf("Arriba", "Abajo")) { gesture ->
                        val binding = bindings[gesture].orEmpty()
                        val label = if (binding.isBlank()) "Sin asignar" else apps.find { it.second.flattenToString() == binding }?.first ?: "App no disponible · volver a elegir"
                        OutlinedButton(onClick = { selectedGesture = gesture }, modifier = Modifier.fillMaxWidth()) { Text("$gesture: $label") }
                    }
                    item { Text("Accesos de las esquinas inferiores") }
                    items(listOf("Teléfono", "Cámara")) { name ->
                        val binding = cornerApps[name].orEmpty()
                        val label = if (binding.isBlank()) "Acción predeterminada" else apps.find { it.second.flattenToString() == binding }?.first ?: "App no disponible · volver a elegir"
                        OutlinedButton(onClick = { selectedShortcut = name }, modifier = Modifier.fillMaxWidth()) { Text("$name: $label") }
                    }
                    item { Text("Estrená celular sin comprar otro", style = MaterialTheme.typography.titleMedium) }
                    item { Text("Todo lo útil al alcance de la mano.") }
                }
            }
        } else {
            Box(Modifier.fillMaxSize().displayCutoutPadding()) {
                HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), userScrollEnabled = !editing, key = { it }) { index ->
                    if (index == APPS_DESKTOP) {
                        Box(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 80.dp, bottom = 80.dp)) {
                            AppList(onOpen = { app -> open(Intent.makeMainActivity(app)); goTo(CENTRAL_DESKTOP) }, isActive = pager.settledPage == APPS_DESKTOP && !pager.isScrollInProgress)
                        }
                    } else {
                        DesktopCanvas(
                            tiles = layouts[index].orEmpty(), editing = editing,
                            onTilesChange = { tiles, persist -> updateLayout(index, tiles, persist) },
                            onVerticalGesture = { gesture ->
                                val component = ComponentName.unflattenFromString(bindings[gesture].orEmpty())
                                if (component != null) open(Intent.makeMainActivity(component))
                            }
                        )
                    }
                }
                IconButton(
                    onClick = {
                        focus.clearFocus(force = true); keyboard?.hide()
                        val target = editingTargetPage(pager.settledPage, lastEditableDesktop)
                        editing = !editing
                        scope.launch { pager.scrollToPage(target) }
                    },
                    modifier = Modifier.align(Alignment.TopStart).zIndex(20f).padding(8.dp).size(64.dp)
                        .background(Color.Black, CircleShape).semantics { contentDescription = if (editing) "Terminar edición" else "Modo edición" }
                ) { LauncherIcon(settings = false) }
                IconButton(
                    onClick = { focus.clearFocus(force = true); keyboard?.hide(); editing = false; page = "Ajustes" },
                    modifier = Modifier.align(Alignment.TopEnd).zIndex(20f).padding(8.dp).size(64.dp)
                        .background(Color.Black, CircleShape).semantics { contentDescription = "Ajustes" }
                ) { LauncherIcon(settings = true) }
                IconButton(onClick = { openCorner("Teléfono") },
                    modifier = Modifier.align(Alignment.BottomStart).zIndex(20f).padding(8.dp).size(64.dp).background(Color.Black, CircleShape)
                        .semantics { contentDescription = "Teléfono" }) { PhoneIcon() }
                IconButton(onClick = { openCorner("Cámara") },
                    modifier = Modifier.align(Alignment.BottomEnd).zIndex(20f).padding(8.dp).size(64.dp).background(Color.Black, CircleShape)
                        .semantics { contentDescription = "Cámara" }) { CameraIcon() }
                if (editing) {
                    val desktop = pager.currentPage
                    Surface(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 8.dp, end = 8.dp, bottom = 80.dp)
                        .border(1.dp, Color.White, MaterialTheme.shapes.large), shape = MaterialTheme.shapes.large, color = Color.Black) {
                        Row(Modifier.horizontalScroll(rememberScrollState())) {
                            TextButton(onClick = { editing = false }) { Text("Listo") }
                            TextButton(onClick = { updateLayout(desktop, layouts[desktop].orEmpty() + Tile("note-$desktop-${System.nanoTime()}", "Nota", .05f, .1f, .8f, .35f)) }) { Text("+ Nota") }
                            if (layouts[desktop].orEmpty().none { it.kind == "Reloj" }) TextButton(onClick = { updateLayout(desktop, layouts[desktop].orEmpty() + initialTiles.first().copy(id = "clock-$desktop")) }) { Text("+ Reloj") }
                            TextButton(onClick = { addWidget(desktop) }) { Text("+ Widget externo") }
                        }
                    }
                }
            }
        }
        selectedGesture?.let { gesture ->
            AppPicker(gesture, onDismiss = { selectedGesture = null }, onSelect = { component ->
                val value = component?.flattenToString().orEmpty()
                bindings = bindings + (gesture to value)
                prefs.edit().putString("gesture-app-$gesture", value).apply()
                selectedGesture = null
            })
        }
        selectedShortcut?.let { name ->
            AppPicker(name, onDismiss = { selectedShortcut = null }, resetLabel = "Usar acción predeterminada", onSelect = { component ->
                val value = component?.flattenToString().orEmpty()
                cornerApps = cornerApps + (name to value)
                prefs.edit().putString(cornerBindingKey(name), value).apply()
                selectedShortcut = null
            })
        }
    }

    @Composable private fun AppList(onOpen: (ComponentName) -> Unit, isActive: Boolean = true) {
        var query by remember { mutableStateOf("") }
        var searching by remember { mutableStateOf(false) }
        val focus = LocalFocusManager.current
        val keyboard = LocalSoftwareKeyboardController.current
        val requester = remember { FocusRequester() }
        fun closeSearch() { focus.clearFocus(force = true); keyboard?.hide(); searching = false; query = "" }
        LaunchedEffect(isActive) { if (!isActive) closeSearch() }
        LaunchedEffect(searching) { if (searching && isActive) { requester.requestFocus(); keyboard?.show() } }
        BackHandler(searching && isActive) { closeSearch() }
        val filtered = apps.filter { it.first.contains(query, true) || it.second.packageName.contains(query, true) }
        Column(Modifier.fillMaxSize()) {
            if (searching) OutlinedTextField(query, { query = it }, label = { Text("Buscar aplicación") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(requester).onFocusChanged { if (it.isFocused && searching) keyboard?.show() },
                leadingIcon = { SearchIcon() },
                trailingIcon = { IconButton(onClick = { closeSearch() }, modifier = Modifier.semantics { contentDescription = "Cerrar búsqueda" }) { Text("×") } })
            else OutlinedButton(onClick = { searching = true }, modifier = Modifier.fillMaxWidth().height(56.dp).semantics { contentDescription = "Buscar aplicaciones" }, shape = RoundedCornerShape(50)) { SearchIcon() }
            LazyColumn(Modifier.weight(1f)) {
                items(filtered, key = { it.second.flattenToString() }) { app ->
                    TextButton(onClick = { closeSearch(); onOpen(app.second) }, modifier = Modifier.fillMaxWidth()) { Text(app.first) }
                }
                if (filtered.isEmpty()) item { Text("No se encontraron aplicaciones.") }
            }
        }
    }

    @Composable private fun AppPicker(gesture: String, onDismiss: () -> Unit, onSelect: (ComponentName?) -> Unit, resetLabel: String = "Sin asignar") {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Aplicación para $gesture") },
            text = {
                Column(Modifier.height(400.dp)) {
                    TextButton(onClick = { onSelect(null) }) { Text(resetLabel) }
                    AppList(onOpen = { onSelect(it) })
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
        )
    }

    @Composable private fun ClockEditor(initialSize: Float, initialLabel: String, initialApp: String,
        onSave: (Float, String, String) -> Unit, onDismiss: () -> Unit) {
        var size by remember { mutableFloatStateOf(initialSize) }
        var label by remember { mutableStateOf(initialLabel) }
        var app by remember { mutableStateOf(initialApp) }
        var choosingApp by remember { mutableStateOf(false) }
        val focus = LocalFocusManager.current
        val keyboard = LocalSoftwareKeyboardController.current
        fun releaseKeyboard() { focus.clearFocus(force = true); keyboard?.hide() }
        if (choosingApp) {
            AppPicker("la barra del reloj", onDismiss = { choosingApp = false }, onSelect = { component -> app = component?.flattenToString().orEmpty(); choosingApp = false })
        } else AlertDialog(
            onDismissRequest = { releaseKeyboard(); onDismiss() },
            title = { Text("Editar reloj y barra") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("Tamaño de hora: ${size.toInt()}")
                    Slider(size, { size = it }, valueRange = 24f..240f)
                    OutlinedTextField(label, { label = it }, label = { Text("Texto de la barra") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedButton(onClick = { releaseKeyboard(); choosingApp = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Abrir: " + (apps.find { it.second.flattenToString() == app }?.first ?: if (app.isBlank()) "Elegir aplicación" else "App no disponible"))
                    }
                    Box(Modifier.fillMaxWidth().height(210.dp)) { DigitalClock(size, label) }
                    Text("El pájaro es fijo. Agrandá la tarjeta para dar más espacio a la hora.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { releaseKeyboard(); onSave(size, label, app) }) { Text("Guardar") } },
            dismissButton = { TextButton(onClick = { releaseKeyboard(); onDismiss() }) { Text("Cancelar") } }
        )
    }

    @Composable private fun DesktopCanvas(
        tiles: List<Tile>,
        editing: Boolean,
        onTilesChange: (List<Tile>, Boolean) -> Unit,
        onVerticalGesture: (String) -> Unit
    ) {
        val latestTiles by rememberUpdatedState(tiles)
        val latestChange by rememberUpdatedState(onTilesChange)
        val latestGesture by rememberUpdatedState(onVerticalGesture)
        val notes = remember { mutableStateMapOf<String, String>() }
        val clockLabels = remember { mutableStateMapOf<String, String>() }
        val clockApps = remember { mutableStateMapOf<String, String>() }
        var editingNote by remember { mutableStateOf<String?>(null) }
        var editingClock by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(editing) { if (!editing) { editingNote = null; editingClock = null } }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val width = maxWidth; val height = maxHeight
            val density = LocalDensity.current
            val wp = with(density) { width.toPx() }.coerceAtLeast(1f)
            val hp = with(density) { height.toPx() }.coerceAtLeast(1f)
            Box(Modifier.fillMaxSize().pointerInput(editing) {
                if (!editing) {
                    var distance = 0f
                    detectVerticalDragGestures(
                        onDragStart = { distance = 0f },
                        onDragCancel = { distance = 0f },
                        onDragEnd = {
                            verticalGestureDirection(distance, 64.dp.toPx())?.let { latestGesture(it) }
                            distance = 0f
                        }
                    ) { change, delta -> change.consume(); distance += delta }
                }
            })
            tiles.forEach { tile ->
                key(tile.id) {
                    fun update(dx: Float, dy: Float, resize: Boolean) {
                        latestChange(latestTiles.map { current ->
                            if (current.id != tile.id) current
                            else if (resize) current.copy(w = current.w + dx / wp, h = current.h + dy / hp).fit()
                            else current.copy(x = current.x + dx / wp, y = current.y + dy / hp).fit()
                        }, false)
                    }
                    val frame = if (editing) Modifier.border(1.dp, Color.White, MaterialTheme.shapes.large) else Modifier
                    Surface(Modifier.offset { IntOffset((tile.x * wp).roundToInt(), (tile.y * hp).roundToInt()) }.size(width * tile.w, height * tile.h).then(frame), shape = MaterialTheme.shapes.large, color = Color.Black) {
                        Column(Modifier.padding(if (editing) 8.dp else 4.dp)) {
                            if (editing) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Mover ${if (tile.kind.startsWith("Widget:")) "widget" else tile.kind}", Modifier.weight(1f).heightIn(min = 48.dp).pointerInput(tile.id, wp, hp) {
                                    detectDragGestures(onDragEnd = { latestChange(latestTiles, true) }, onDragCancel = { latestChange(latestTiles, true) }) { change, delta -> change.consume(); update(delta.x, delta.y, false) }
                                })
                                if (tile.kind == "Nota") TextButton(onClick = { editingNote = tile.id }) { Text("Editar") }
                                if (tile.kind == "Reloj") TextButton(onClick = { editingClock = tile.id }) { Text("Editar") }
                                TextButton(onClick = {
                                    tile.kind.substringAfter("Widget:", "").toIntOrNull()?.let { widgetHost.deleteAppWidgetId(it) }
                                    latestChange(latestTiles.filterNot { it.id == tile.id }, true)
                                }) { Text("×") }
                            }
                            Box(Modifier.weight(1f).fillMaxWidth()) {
                                if (tile.kind.startsWith("Widget:")) {
                                    val widgetId = tile.kind.substringAfter(":").toInt()
                                    val info = widgetManager.getAppWidgetInfo(widgetId)
                                    if (info == null) Text("Widget no disponible. Podés quitarlo en Modo edición.")
                                    else AndroidView(factory = { widgetHost.createView(it, widgetId, info) }, modifier = Modifier.fillMaxSize(), update = { view ->
                                        view.updateAppWidgetSize(null, (width.value * tile.w).toInt(), (height.value * tile.h - 80).toInt().coerceAtLeast(40), (width.value * tile.w).toInt(), (height.value * tile.h - 80).toInt().coerceAtLeast(40))
                                    })
                                } else if (tile.kind == "Reloj") DigitalClock(
                                    fontSizeSp = componentFontSize(tile.kind, tile.fontSizeSp),
                                    actionText = clockLabels[tile.id] ?: prefs.getString("clock-label-${tile.id}", "Busca...").orEmpty(),
                                    onAction = {
                                        if (editing) editingClock = tile.id
                                        else {
                                            val binding = clockApps[tile.id] ?: prefs.getString("clock-app-${tile.id}", "").orEmpty()
                                            val component = ComponentName.unflattenFromString(binding)
                                            if (component != null) open(Intent.makeMainActivity(component))
                                            else Toast.makeText(this@MainActivity, "Elegí la aplicación de la barra en Modo edición → Editar reloj", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                                else NotePreview(notes[tile.id] ?: prefs.getString("note-${tile.id}", "").orEmpty(), componentFontSize(tile.kind, tile.fontSizeSp))
                            }
                            if (editing) Text("↘", Modifier.align(Alignment.End).size(48.dp).pointerInput(tile.id, wp, hp) {
                                detectDragGestures(onDragEnd = { latestChange(latestTiles, true) }, onDragCancel = { latestChange(latestTiles, true) }) { change, delta -> change.consume(); update(delta.x, delta.y, true) }
                            }, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }
        }
        if (editing) editingNote?.let { id ->
            if (tiles.any { it.id == id && it.kind == "Nota" }) NoteEditor(
                initialText = notes[id] ?: prefs.getString("note-$id", "").orEmpty(),
                initialFontSize = componentFontSize("Nota", tiles.find { it.id == id }?.fontSizeSp),
                onSave = { value, size ->
                    notes[id] = value; prefs.edit().putString("note-$id", value).apply()
                    latestChange(latestTiles.map { if (it.id == id) it.copy(fontSizeSp = size) else it }, true)
                    editingNote = null
                },
                onDismiss = { editingNote = null }
            )
        }
        if (editing) editingClock?.let { id ->
            if (tiles.any { it.id == id && it.kind == "Reloj" }) ClockEditor(
                initialSize = componentFontSize("Reloj", tiles.find { it.id == id }?.fontSizeSp),
                initialLabel = clockLabels[id] ?: prefs.getString("clock-label-$id", "Busca...").orEmpty(),
                initialApp = clockApps[id] ?: prefs.getString("clock-app-$id", "").orEmpty(),
                onSave = { size, label, app ->
                    clockLabels[id] = label; clockApps[id] = app
                    prefs.edit().putString("clock-label-$id", label).putString("clock-app-$id", app).apply()
                    latestChange(latestTiles.map { if (it.id == id) it.copy(fontSizeSp = size) else it }, true)
                    editingClock = null
                },
                onDismiss = { editingClock = null }
            )
        }
    }
}

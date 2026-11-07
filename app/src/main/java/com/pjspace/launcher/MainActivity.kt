package com.pjspace.launcher

import android.app.role.RoleManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.AlarmClock
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
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
    private var aliases by mutableStateOf<Map<String,String>>(emptyMap())
    private var hidden by mutableStateOf<Set<String>>(emptySet())
    private var appOrder by mutableStateOf<List<String>?>(null)
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
        if (listOf(EXTRA_DESKTOP, LEFT_DESKTOP, CENTRAL_DESKTOP).any { desktop -> loadTiles(desktop).any { it.kind.startsWith("Widget:") } }) {
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
        enableEdgeToEdge(statusBarStyle=SystemBarStyle.dark(android.graphics.Color.BLACK),navigationBarStyle=SystemBarStyle.dark(android.graphics.Color.BLACK))
        if(Build.VERSION.SDK_INT>=29)window.isNavigationBarContrastEnforced=false
        appOrder=try { if(prefs.contains("app-order")) { val a=JSONArray(prefs.getString("app-order","[]"));(0 until a.length()).map{a.getString(it)} } else null } catch(_:Exception){null}
        aliases=try { val o=JSONObject(prefs.getString("app-aliases","{}")?:"{}");o.keys().asSequence().associateWith{o.getString(it)} }catch(_:Exception){emptyMap()}
        hidden=prefs.getStringSet("hidden-apps",emptySet()).orEmpty().toSet()
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
        configureSystemBars()
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = packageManager.queryIntentActivities(intent, 0).map {
            it.loadLabel(packageManager).toString() to ComponentName(it.activityInfo.packageName, it.activityInfo.name)
        }.filter { it.second.packageName != packageName }.distinctBy { it.second }.sortedBy { it.first.lowercase() }
        appOrder?.let { val normalized=orderedAppIds(apps.map{it.second.flattenToString()},it);if(normalized!=it)saveAppOrder(normalized) }
    }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); page = "Inicio"; homeRequest++ }
    private fun open(intent: Intent) {
        try { startActivity(intent) } catch (_: Exception) { Toast.makeText(this, "Esta acción no está disponible", Toast.LENGTH_SHORT).show() }
    }
    private fun configureSystemBars() {
        val controller=WindowCompat.getInsetsController(window,window.decorView)
        controller.isAppearanceLightStatusBars=false;controller.isAppearanceLightNavigationBars=false
        controller.systemBarsBehavior=WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if(page=="Inicio")controller.hide(WindowInsetsCompat.Type.systemBars())else controller.show(WindowInsetsCompat.Type.systemBars())
    }
    override fun onWindowFocusChanged(hasFocus:Boolean){super.onWindowFocusChanged(hasFocus);if(hasFocus)configureSystemBars()}
    private fun openAlarms() {
        try { startActivity(Intent(AlarmClock.ACTION_SET_ALARM)) }
        catch(_:Exception){try{startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS))}catch(_:Exception){Toast.makeText(this,"No se encontró una app de alarmas",Toast.LENGTH_SHORT).show()}}
    }
    private fun saveAppOrder(order:List<String>?) {
        appOrder=order?.let{orderedAppIds(apps.map{app->app.second.flattenToString()},it)}
        val editor=prefs.edit();if(appOrder==null)editor.remove("app-order")else editor.putString("app-order",JSONArray(appOrder).toString());editor.apply()
    }
    private fun orderedApps():List<Pair<String,ComponentName>> {
        val byId=apps.associateBy{it.second.flattenToString()}
        return orderedAppIds(apps.map{it.second.flattenToString()},appOrder).mapNotNull{byId[it]}
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
    private fun loadTiles(desktop: Int): List<Tile> {
        val key=desktopStorageKey(desktop)
        val tiles=try {
            if(!prefs.contains(key))defaultTiles(desktop) else {
                val a=JSONArray(prefs.getString(key,"[]"))
                (0 until a.length()).map { index -> val o=a.getJSONObject(index)
                    Tile(o.getString("id"),o.getString("kind"),o.getDouble("x").toFloat(),o.getDouble("y").toFloat(),o.getDouble("w").toFloat(),o.getDouble("h").toFloat(),
                        if(o.has("fontSizeSp"))o.getDouble("fontSizeSp").toFloat()else null,
                        if(o.has("iconKey"))o.getString("iconKey")else null,
                        if(o.has("appComponent"))o.getString("appComponent")else null,
                        if(o.has("label"))o.getString("label")else null,o.optString("textAlignment","left"),o.optString("fontFamily","default")).fit()
                }
            }
        }catch(_:Exception){defaultTiles(desktop)}
        if(prefs.getBoolean("split-bars-$desktop",false))return tiles
        val labels=tiles.associate{it.id to prefs.getString("clock-label-${it.id}","Busca...").orEmpty()}
        val bindings=tiles.associate{it.id to prefs.getString("clock-app-${it.id}","").orEmpty()}
        return splitLegacyClockBars(tiles,labels,bindings).also{saveTiles(desktop,it)}
    }
    private fun saveTiles(desktop:Int,tiles:List<Tile>) {
        val a=JSONArray()
        tiles.forEach { tile ->
            val o=JSONObject().put("id",tile.id).put("kind",tile.kind).put("x",tile.x).put("y",tile.y).put("w",tile.w).put("h",tile.h)
            tile.fontSizeSp?.let{o.put("fontSizeSp",it)};tile.iconKey?.let{o.put("iconKey",it)}
            o.put("textAlignment",tile.textAlignment).put("fontFamily",tile.fontFamily)
            tile.appComponent?.let{o.put("appComponent",it)};tile.label?.let{o.put("label",it)};a.put(o)
        }
        prefs.edit().putString(desktopStorageKey(desktop),a.toString()).putBoolean("split-bars-$desktop",true).apply()
    }
    private fun loadTodos(id:String):List<TodoItem> = try {
        val a=JSONArray(prefs.getString("todos-$id","[]"));(0 until a.length()).map{val o=a.getJSONObject(it);TodoItem(o.getString("id"),o.getString("text"),o.optBoolean("done",false))}
    }catch(_:Exception){emptyList()}
    private fun saveTodos(id:String,items:List<TodoItem>) {
        val a=JSONArray();items.forEach{a.put(JSONObject().put("id",it.id).put("text",it.text).put("done",it.done))}
        prefs.edit().putString("todos-$id",a.toString()).apply()
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable private fun Home() {
        var editing by remember { mutableStateOf(false) }
        val focus = LocalFocusManager.current
        val keyboard = LocalSoftwareKeyboardController.current
        LaunchedEffect(editing) { if (!editing) { focus.clearFocus(force = true); keyboard?.hide() } }
        LaunchedEffect(page) { configureSystemBars() }
        val density=LocalDensity.current
        val bottomInset=with(density){if(WindowInsets.areNavigationBarsVisible)WindowInsets.navigationBarsIgnoringVisibility.getBottom(this).toDp()else 0.dp}
        val topInset=with(density){if(WindowInsets.areStatusBarsVisible)WindowInsets.statusBarsIgnoringVisibility.getTop(this).toDp()else 0.dp}
        var extra by remember { mutableStateOf(prefs.getBoolean("extra-desktop",false)) }
        val pages=desktopPages(extra)
        val layouts=remember {mutableStateMapOf(EXTRA_DESKTOP to loadTiles(EXTRA_DESKTOP),LEFT_DESKTOP to loadTiles(LEFT_DESKTOP),CENTRAL_DESKTOP to loadTiles(CENTRAL_DESKTOP))}
        val pager=rememberPagerState(initialPage=pages.indexOf(CENTRAL_DESKTOP),pageCount={pages.size})
        val desktop=pages.getOrElse(pager.currentPage){CENTRAL_DESKTOP}
        val scope=rememberCoroutineScope()
        var clipboard by remember {mutableStateOf<Tile?>(null)}
        var grid by remember {mutableStateOf(prefs.getBoolean("edit-grid",false))}
        var fixedEditor by remember {mutableStateOf(false)};var fixedRevision by remember {mutableIntStateOf(0)}
        var draftOrder by remember {mutableStateOf<List<String>?>(null)}
        LaunchedEffect(editing){if(editing)draftOrder=orderedApps().map{it.second.flattenToString()}else draftOrder=null}
        fun finishEditing(){draftOrder?.let{saveAppOrder(it)};editing=false}
        var selectedGesture by remember { mutableStateOf<String?>(null) }
        var selectedShortcut by remember { mutableStateOf<String?>(null) }
        var orderingApps by remember { mutableStateOf(false) }
        var addingComponent by remember { mutableStateOf(false) }
        LaunchedEffect(editing){if(!editing){orderingApps=false;addingComponent=false}}
        var cornerApps by remember { mutableStateOf(listOf("Teléfono", "Cámara").associateWith { prefs.getString(cornerBindingKey(it), "").orEmpty() }) }
        var cornerIcons by remember { mutableStateOf(listOf("Teléfono","Cámara").associateWith { prefs.getString(if(it=="Teléfono")"corner-phone-icon" else "corner-camera-icon",if(it=="Teléfono")"phone" else "camera").orEmpty() }) }
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
            scope.launch { pager.animateScrollToPage(pages.indexOf(desktop).coerceAtLeast(0)) }
        }
        fun openCorner(name: String) {
            focus.clearFocus(force = true); keyboard?.hide()
            val component = ComponentName.unflattenFromString(cornerApps[name].orEmpty())
            open(if (component != null) Intent.makeMainActivity(component) else if (name == "Teléfono") Intent(Intent.ACTION_DIAL) else Intent("android.media.action.STILL_IMAGE_CAMERA"))
        }
        LaunchedEffect(homeRequest) {
            focus.clearFocus(force = true); keyboard?.hide()
            editing = false; pager.scrollToPage(pages.indexOf(CENTRAL_DESKTOP))
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
            when (launcherBackAction(selectedGesture != null || selectedShortcut != null, editing, page == "Ajustes", desktop)) {
                LauncherBackAction.CLOSE_DIALOG -> { selectedGesture = null; selectedShortcut = null }
                LauncherBackAction.FINISH_EDITING -> finishEditing()
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
                    item { OutlinedButton(onClick={fixedEditor=true}){Text("Iconos fijos: tamaño y posición")} }
                    item { if(!extra)OutlinedButton(onClick={extra=true;prefs.edit().putBoolean("extra-desktop",true).apply();scope.launch{pager.scrollToPage(desktopPages(true).indexOf(APPS_DESKTOP))}}){Text("Añadir escritorio a la izquierda")} }
                    item { Text("Aplicaciones ocultas") }
                    items(apps.filter{it.second.flattenToString() in hidden}) { app -> TextButton(onClick={hidden=hidden-app.second.flattenToString();prefs.edit().putStringSet("hidden-apps",hidden).apply()}){Text("Mostrar: "+(aliases[app.second.flattenToString()]?:app.first))} }
                    item { Text("Accesos de las esquinas inferiores") }
                    items(listOf("Teléfono", "Cámara")) { name ->
                        val binding = cornerApps[name].orEmpty()
                        val label = if (binding.isBlank()) "Acción predeterminada" else apps.find { it.second.flattenToString() == binding }?.first ?: "App no disponible · volver a elegir"
                        val iconName=accessIcons.find{it.key==cornerIcons[name]}?.name?:name
                        OutlinedButton(onClick = { selectedShortcut = name }, modifier = Modifier.fillMaxWidth()) { Text("${if(name=="Teléfono")"Inferior izquierdo" else "Inferior derecho"}: $iconName · $label") }
                    }
                    item { Text("Estrená celular sin comprar otro", style = MaterialTheme.typography.titleMedium) }
                    item { Text("Todo lo útil al alcance de la mano.") }
                }
            }
        } else {
            Box(Modifier.fillMaxSize().displayCutoutPadding()) {
                HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), userScrollEnabled = true, key = { pages[it] }) { pageIndex ->
                    val index=pages[pageIndex]
                    if (index == APPS_DESKTOP) {
                        Box(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 80.dp+topInset, bottom = 8.dp+bottomInset)) {
                            AppList(onOpen = { app -> open(Intent.makeMainActivity(app)); goTo(CENTRAL_DESKTOP) }, isActive = pages[pager.settledPage] == APPS_DESKTOP && !pager.isScrollInProgress, editing=editing,
                                order=draftOrder, onMove={from,to-> val list=draftOrder?:orderedApps().map{it.second.flattenToString()};draftOrder=moveItem(list,list.indexOf(from),list.indexOf(to))})
                        }
                    } else {
                        DesktopCanvas(
                            tiles = layouts[index].orEmpty(), editing = editing, grid=grid, onCopy={clipboard=it},
                            onTilesChange = { tiles, persist -> updateLayout(index, tiles, persist) },
                            onVerticalGesture = { gesture ->
                                val component = ComponentName.unflattenFromString(bindings[gesture].orEmpty())
                                if (component != null) open(Intent.makeMainActivity(component))
                            }
                        )
                    }
                }
                if(desktop==APPS_DESKTOP) {
                    Text("Aplicaciones",Modifier.align(Alignment.TopCenter).padding(top=30.dp+topInset),style=MaterialTheme.typography.titleLarge)
                    FixedIcon(prefs,"brush",fixedRevision,topInset,bottomInset,onClick={focus.clearFocus(force=true);keyboard?.hide();if(editing)finishEditing()else editing=true}) {LauncherIcon(settings=false,modifier=Modifier.fillMaxSize())}
                    FixedIcon(prefs,"gear",fixedRevision,topInset,bottomInset,onClick={focus.clearFocus(force=true);keyboard?.hide();if(editing)finishEditing();page="Ajustes"}) {LauncherIcon(settings=true,modifier=Modifier.fillMaxSize())}
                }
                if(desktop==CENTRAL_DESKTOP) {
                    FixedIcon(prefs,"phone",fixedRevision,topInset,bottomInset,onClick={openCorner("Teléfono")}){AccessSymbol(cornerIcons["Teléfono"]?:"phone",Modifier.fillMaxSize())}
                    FixedIcon(prefs,"camera",fixedRevision,topInset,bottomInset,onClick={openCorner("Cámara")}){AccessSymbol(cornerIcons["Cámara"]?:"camera",Modifier.fillMaxSize())}
                }
                if (editing) {
                    MovableEditBar(prefs) {
                            TextButton(onClick = { finishEditing() }) { Text("Listo") }
                            if(desktop!=APPS_DESKTOP) {
                            TextButton(onClick={grid=!grid;prefs.edit().putBoolean("edit-grid",grid).apply()}){Text(if(grid)"Cuadrícula ✓"else"Cuadrícula")}
                            TextButton(enabled=clipboard!=null,onClick={clipboard?.let{updateLayout(desktop,layouts[desktop].orEmpty()+it.duplicate("copy-${System.nanoTime()}"))}}){Text("Pegar")}
                            Box {
                                TextButton(onClick={addingComponent=true}){Text("Añadir componente")}
                                DropdownMenu(addingComponent,{addingComponent=false}){
                                    listOf("Nota","Reloj","Barra","Acceso","Calendario","Pendientes").forEach { kind ->
                                        DropdownMenuItem(text={Text(kind)},onClick={
                                            val h=when(kind){"Barra"->.075f;"Acceso"->.14f;"Calendario"->.50f;"Pendientes"->.45f;else->.30f}
                                            updateLayout(desktop,layouts[desktop].orEmpty()+Tile("${kind.lowercase()}-$desktop-${System.nanoTime()}",kind,.05f,.12f,if(kind=="Acceso").24f else .90f,h,iconKey=if(kind=="Acceso")"star"else null,label=if(kind=="Barra")"Busca..."else null).fit())
                                            addingComponent=false
                                        })
                                    }
                                }
                            }

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
            AppTileEditor(Tile("corner-$name","Acceso",0f,0f,.2f,.14f,iconKey=cornerIcons[name],appComponent=cornerApps[name],label=name),
                cornerDefault=if(name=="Teléfono")"phone"else"camera",onDismiss={selectedShortcut=null},onSave={tile->
                val value=tile.appComponent.orEmpty();val icon=tile.iconKey?:if(name=="Teléfono")"phone"else"camera"
                cornerApps=cornerApps+(name to value);cornerIcons=cornerIcons+(name to icon)
                prefs.edit().putString(cornerBindingKey(name),value).putString(if(name=="Teléfono")"corner-phone-icon"else"corner-camera-icon",icon).apply()
                selectedShortcut = null
            })
        }
        if(fixedEditor)FixedIconsEditor(prefs,onDismiss={fixedEditor=false},onSaved={fixedRevision++;fixedEditor=false})

    }

    @Composable private fun AppList(onOpen: (ComponentName) -> Unit, isActive: Boolean = true, editing:Boolean=false, order:List<String>?=null, onMove:(String,String)->Unit={_,_->}) {
        var query by remember { mutableStateOf("") }
        var searching by remember { mutableStateOf(false) }
        val focus = LocalFocusManager.current
        val keyboard = LocalSoftwareKeyboardController.current
        val requester = remember { FocusRequester() }
        fun closeSearch() { focus.clearFocus(force = true); keyboard?.hide(); searching = false; query = "" }
        LaunchedEffect(isActive) { if (!isActive) closeSearch() }
        LaunchedEffect(searching) { if (searching && isActive) { requester.requestFocus(); keyboard?.show() } }
        BackHandler(searching && isActive) { closeSearch() }
        var renaming by remember{mutableStateOf<ComponentName?>(null)};var renameText by remember{mutableStateOf("")}
        val byId=apps.associateBy{it.second.flattenToString()}
        val filtered = orderedAppIds(apps.map{it.second.flattenToString()},order?:appOrder).mapNotNull{byId[it]}.filter { appMatches(it.first,aliases[it.second.flattenToString()].orEmpty(),it.second.packageName,it.second.flattenToString() in hidden,query) }
        LaunchedEffect(editing){if(editing)closeSearch()}
        Column(Modifier.fillMaxSize()) {
            if (searching) OutlinedTextField(query, { query = it }, label = { Text("Buscar aplicación") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(requester).onFocusChanged { if (it.isFocused && searching) keyboard?.show() },
                leadingIcon = { SearchIcon() },
                trailingIcon = { IconButton(onClick = { closeSearch() }, modifier = Modifier.semantics { contentDescription = "Cerrar búsqueda" }) { Text("×") } })
            else OutlinedButton(enabled=!editing,onClick = { searching = true }, modifier = Modifier.fillMaxWidth().height(56.dp).semantics { contentDescription = "Buscar aplicaciones" }, shape = RoundedCornerShape(50)) { SearchIcon() }
            Box(Modifier.weight(1f)) {AppRows(filtered,editing,aliases,onOpen={closeSearch();onOpen(it)},onMove=onMove,onAction={action,app->
                when(action){
                    "Información de app"->open(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:${app.packageName}")))
                    "Desinstalar"->open(Intent(Intent.ACTION_DELETE,Uri.parse("package:${app.packageName}")))
                    "Ocultar"->{hidden=hidden+app.flattenToString();prefs.edit().putStringSet("hidden-apps",hidden).apply()}
                    "Renombrar"->{closeSearch();renaming=app;renameText=aliases[app.flattenToString()]?:apps.find{it.second==app}?.first.orEmpty()}
                }
            })}
        }
        renaming?.let { app -> AlertDialog(onDismissRequest={renaming=null},title={Text("Nombre en PJ Space")},text={OutlinedTextField(renameText,{renameText=it},label={Text("Nombre")},singleLine=true)},confirmButton={TextButton(onClick={val id=app.flattenToString();aliases=if(renameText.isBlank())aliases-id else aliases+(id to renameText.trim());prefs.edit().putString("app-aliases",JSONObject(aliases).toString()).apply();focus.clearFocus(force=true);keyboard?.hide();renaming=null}){Text("Guardar")}},dismissButton={TextButton(onClick={renaming=null}){Text("Cancelar")}}) }
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

    @Composable private fun ClockEditor(initialSize:Float,onSave:(Float)->Unit,onDismiss:()->Unit) {
        var size by remember{mutableFloatStateOf(initialSize)}
        AlertDialog(onDismissRequest=onDismiss,title={Text("Editar reloj")},text={Column {
            Text("Tamaño de hora: ${size.toInt()}");Slider(size,{size=it},valueRange=24f..240f)
            Box(Modifier.fillMaxWidth().height(180.dp)){DigitalClock(size)}
            Text("En Inicio, tocar la hora o fecha abre alarmas.",style=MaterialTheme.typography.bodySmall)
        }},confirmButton={TextButton(onClick={onSave(size)}){Text("Guardar")}},dismissButton={TextButton(onClick=onDismiss){Text("Cancelar")}})
    }

    @Composable private fun AppTileEditor(initial:Tile,cornerDefault:String?=null,onSave:(Tile)->Unit,onDismiss:()->Unit,onDelete:(()->Unit)?=null,onCopy:(()->Unit)?=null) {
        var label by remember{mutableStateOf(initial.label.orEmpty())}
        var app by remember{mutableStateOf(initial.appComponent.orEmpty())}
        var icon by remember{mutableStateOf(initial.iconKey?:"star")}
        var choosing by remember{mutableStateOf(false)}
        val focus=LocalFocusManager.current;val keyboard=LocalSoftwareKeyboardController.current
        fun release(){focus.clearFocus(force=true);keyboard?.hide()}
        if(choosing)AppPicker(if(initial.kind=="Barra")"la barra"else"el acceso",onDismiss={choosing=false},
            resetLabel=if(cornerDefault==null)"Sin asignar"else"Usar acción predeterminada",onSelect={app=it?.flattenToString().orEmpty();choosing=false})
        else AlertDialog(onDismissRequest={release();onDismiss()},title={Text(if(cornerDefault!=null)"Personalizar acceso fijo"else"Editar ${initial.kind.lowercase()}")},text={
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if(initial.kind=="Acceso")AccessIconPicker(icon){icon=it}
                if(cornerDefault==null)OutlinedTextField(label,{label=it},label={Text("Texto")},singleLine=true,modifier=Modifier.fillMaxWidth())
                OutlinedButton(onClick={release();choosing=true},modifier=Modifier.fillMaxWidth()) {
                    Text("Abrir: "+(apps.find{it.second.flattenToString()==app}?.first?:if(app.isBlank())if(cornerDefault!=null)"Acción predeterminada"else"Elegir aplicación"else"App no disponible"))
                }
                if(initial.kind=="Barra")Box(Modifier.fillMaxWidth().height(52.dp)){ActionBar(label){}}
                if(cornerDefault!=null)TextButton(onClick={icon=cornerDefault;app=""}){Text("Restaurar icono y acción predeterminados")}
                if(onCopy!=null)TextButton(onClick={release();onCopy()}){Text("Copiar acceso o barra")}
                if(onDelete!=null)TextButton(onClick={release();onDelete()}){Text("Eliminar componente")}
            }
        },confirmButton={TextButton(onClick={release();onSave(initial.copy(label=label,appComponent=app,iconKey=if(initial.kind=="Acceso")icon else initial.iconKey))}){Text("Guardar")}},
            dismissButton={TextButton(onClick={release();onDismiss()}){Text("Cancelar")}})
    }

    @Composable private fun DesktopCanvas(tiles:List<Tile>,editing:Boolean,grid:Boolean,onCopy:(Tile)->Unit,onTilesChange:(List<Tile>,Boolean)->Unit,onVerticalGesture:(String)->Unit) {
        val latestTiles by rememberUpdatedState(tiles);val latestChange by rememberUpdatedState(onTilesChange);val latestGesture by rememberUpdatedState(onVerticalGesture)
        val notes=remember{mutableStateMapOf<String,String>()};val blocks=remember{mutableStateMapOf<String,List<TodoBlock>>()}
        val marks=remember{mutableStateMapOf<String,Map<String,ImportantDay>>()};var selectedDay by remember{mutableStateOf<Pair<String,java.time.LocalDate>?>(null)}
        fun getBlocks(id:String):List<TodoBlock> = blocks[id]?:readBlocks(prefs.getString("blocks-$id",null))?:migratedBlocks(loadTodos(id)).also{prefs.edit().putString("blocks-$id",writeBlocks(it)).apply()}
        fun setBlocks(id:String,value:List<TodoBlock>){blocks[id]=value;prefs.edit().putString("blocks-$id",writeBlocks(value)).apply()}
        fun getMarks(id:String)=marks[id]?:readMarks(prefs.getString("calendar-$id",null))
        var editorId by remember{mutableStateOf<String?>(null)}
        val focus=LocalFocusManager.current;val keyboard=LocalSoftwareKeyboardController.current
        fun closeEditor(){focus.clearFocus(force=true);keyboard?.hide();editorId=null;selectedDay=null}
        fun replace(tile:Tile){latestChange(latestTiles.map{if(it.id==tile.id)tile else it},true)}
        fun remove(id:String){latestTiles.find{it.id==id}?.kind?.substringAfter("Widget:","")?.toIntOrNull()?.let{widgetHost.deleteAppWidgetId(it)};latestChange(latestTiles.filterNot{it.id==id},true);closeEditor()}
        fun launchTile(tile:Tile){
            val component=ComponentName.unflattenFromString(tile.appComponent.orEmpty())
            if(component!=null)open(Intent.makeMainActivity(component))else Toast.makeText(this,"Elegí una app en Modo edición → Editar",Toast.LENGTH_SHORT).show()
        }
        LaunchedEffect(editing){if(!editing)closeEditor()}
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val width=maxWidth;val height=maxHeight;val density=LocalDensity.current
            val wp=with(density){width.toPx()}.coerceAtLeast(1f);val hp=with(density){height.toPx()}.coerceAtLeast(1f)
            Box(Modifier.fillMaxSize().pointerInput(editing){if(!editing){
                var distance=0f
                detectVerticalDragGestures(onDragStart={distance=0f},onDragCancel={distance=0f},onDragEnd={verticalGestureDirection(distance,GESTURE_DISTANCE_DP.dp.toPx())?.let{latestGesture(it)};distance=0f}){change,amount->change.consume();distance+=amount}
            }})
            if(editing&&grid)Canvas(Modifier.fillMaxSize()){for(i in 1 until 20){drawLine(Color.White.copy(alpha=.14f),androidx.compose.ui.geometry.Offset(size.width*i/20,0f),androidx.compose.ui.geometry.Offset(size.width*i/20,size.height));drawLine(Color.White.copy(alpha=.14f),androidx.compose.ui.geometry.Offset(0f,size.height*i/20),androidx.compose.ui.geometry.Offset(size.width,size.height*i/20))}}
            tiles.forEach { tile -> key(tile.id) {
                fun update(dx:Float,dy:Float,resize:Boolean){latestChange(latestTiles.map{current->if(current.id!=tile.id)current else if(resize)current.copy(w=current.w+dx/wp,h=current.h+dy/hp).fit()else current.copy(x=current.x+dx/wp,y=current.y+dy/hp).fit()},false)}
                val decorated=editing||tile.kind in listOf("Nota","Calendario","Pendientes")
                val shape=MaterialTheme.shapes.large
                val frame=if(decorated)Modifier.border(1.dp,Color.White,shape)else Modifier
                Surface(Modifier.offset{IntOffset((tile.x*wp).roundToInt(),(tile.y*hp).roundToInt())}.size(width*tile.w,height*tile.h).then(frame),shape=shape,color=Color.Black) {
                    Box(Modifier.fillMaxSize()) {
                        Box(Modifier.fillMaxSize().padding(if(tile.kind in listOf("Nota","Calendario","Pendientes"))12.dp else 4.dp)) {
                            when {
                                tile.kind.startsWith("Widget:") -> {
                                    val id=tile.kind.substringAfter(":").toInt();val info=widgetManager.getAppWidgetInfo(id)
                                    if(info==null)Text("Widget no disponible")else AndroidView(factory={widgetHost.createView(it,id,info)},modifier=Modifier.fillMaxSize(),update={view->view.updateAppWidgetSize(null,(width.value*tile.w).toInt(),(height.value*tile.h).toInt(),(width.value*tile.w).toInt(),(height.value*tile.h).toInt())})
                                }
                                tile.kind=="Reloj" -> DigitalClock(componentFontSize(tile.kind,tile.fontSizeSp)){if(editing)editorId=tile.id else openAlarms()}
                                tile.kind=="Barra" -> ActionBar(tile.label?:"Busca..."){if(editing)editorId=tile.id else launchTile(tile)}
                                tile.kind=="Acceso" -> BoxWithConstraints(Modifier.fillMaxSize().clickable{if(editing)editorId=tile.id else launchTile(tile)}) {
                                    val side=minOf(maxWidth,(maxHeight-24.dp).coerceAtLeast(16.dp),72.dp)
                                    Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
                                        AccessSymbol(tile.iconKey?:"star",Modifier.size(side))
                                        val label=tile.label?.takeIf{it.isNotBlank()}?:apps.find{it.second.flattenToString()==tile.appComponent}?.first.orEmpty()
                                        if(label.isNotBlank())Text(label,fontSize=12.sp,maxLines=2,overflow=TextOverflow.Ellipsis)
                                    }
                                }
                                tile.kind=="Calendario" -> MonthlyCalendar(getMarks(tile.id),editing){selectedDay=tile.id to it}
                                tile.kind=="Pendientes" -> BlockPanel(getBlocks(tile.id),editing){setBlocks(tile.id,it)}
                                else -> NotePreview(notes[tile.id]?:prefs.getString("note-${tile.id}","").orEmpty(),componentFontSize(tile.kind,tile.fontSizeSp),tile.textAlignment,tile.fontFamily)
                            }
                        }
                        if(editing) {
                            Row(Modifier.align(Alignment.TopStart).fillMaxWidth().background(Color.Black.copy(alpha=.9f)),verticalAlignment=Alignment.CenterVertically) {
                                Text("↕",fontSize=20.sp,modifier=Modifier.size(32.dp).pointerInput(tile.id,wp,hp,grid){detectDragGestures(onDragEnd={latestChange(if(grid)latestTiles.map{if(it.id==tile.id)it.snapped()else it}else latestTiles,true)},onDragCancel={latestChange(if(grid)latestTiles.map{if(it.id==tile.id)it.snapped()else it}else latestTiles,true)}){change,delta->change.consume();update(delta.x,delta.y,false)}})
                                if(tile.kind in listOf("Nota","Reloj","Barra","Acceso","Pendientes"))TextButton(onClick={editorId=tile.id},contentPadding=PaddingValues(0.dp),modifier=Modifier.height(32.dp).width(40.dp)){Text("Editar",fontSize=11.sp)}
                                if(tile.kind !in listOf("Barra","Acceso"))TextButton(onClick={remove(tile.id)},contentPadding=PaddingValues(0.dp),modifier=Modifier.size(32.dp)){Text("×")}
                            }
                            Text("↘",fontSize=24.sp,modifier=Modifier.align(Alignment.BottomEnd).size(32.dp).background(Color.Black).pointerInput(tile.id,wp,hp,grid){detectDragGestures(onDragEnd={latestChange(if(grid)latestTiles.map{if(it.id==tile.id)it.snapped()else it}else latestTiles,true)},onDragCancel={latestChange(if(grid)latestTiles.map{if(it.id==tile.id)it.snapped()else it}else latestTiles,true)}){change,delta->change.consume();update(delta.x,delta.y,true)}})
                        }
                    }
                }
            } }
        }
        if(editing)editorId?.let { id -> latestTiles.find{it.id==id}?.let { tile -> when(tile.kind) {
            "Nota" -> NoteEditor(notes[id]?:prefs.getString("note-$id","").orEmpty(),componentFontSize("Nota",tile.fontSizeSp),tile.textAlignment,tile.fontFamily,onSave={text,size,alignment,family->notes[id]=text;prefs.edit().putString("note-$id",text).apply();replace(tile.copy(fontSizeSp=size,textAlignment=alignment,fontFamily=family));closeEditor()},onDismiss={closeEditor()})
            "Reloj" -> ClockEditor(componentFontSize("Reloj",tile.fontSizeSp),onSave={replace(tile.copy(fontSizeSp=it));closeEditor()},onDismiss={closeEditor()})
            "Barra","Acceso" -> AppTileEditor(tile,onSave={replace(it);closeEditor()},onDismiss={closeEditor()},onDelete={remove(id)},onCopy={onCopy(tile);closeEditor()})
            "Pendientes" -> BlocksEditor(getBlocks(id),onSave={setBlocks(id,it);closeEditor()},onDismiss={closeEditor()})
        } } }
        selectedDay?.let{(id,date)->ImportantDayDialog(date,getMarks(id)[date.toString()],editing,onSave={value->val updated=if(value==null)getMarks(id)-date.toString()else getMarks(id)+(date.toString() to value);marks[id]=updated;prefs.edit().putString("calendar-$id",writeMarks(updated)).apply();focus.clearFocus(force=true);keyboard?.hide();selectedDay=null},onDismiss={focus.clearFocus(force=true);keyboard?.hide();selectedDay=null})}

    }
}



package com.pjspace.launcher

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable fun PJSpaceTheme(content: @Composable () -> Unit) {
    val black = Color.Black
    val white = Color.White
    MaterialTheme(colorScheme = darkColorScheme(
        primary = white, onPrimary = black, primaryContainer = black, onPrimaryContainer = white,
        secondary = white, onSecondary = black, secondaryContainer = black, onSecondaryContainer = white,
        tertiary = white, onTertiary = black, tertiaryContainer = black, onTertiaryContainer = white,
        background = black, onBackground = white, surface = black, onSurface = white,
        surfaceVariant = black, onSurfaceVariant = white, surfaceTint = black,
        surfaceDim = black, surfaceBright = black, surfaceContainer = black,
        surfaceContainerLowest = black, surfaceContainerLow = black,
        surfaceContainerHigh = black, surfaceContainerHighest = black,
        outline = white, outlineVariant = white, error = white, onError = black,
        errorContainer = black, onErrorContainer = white
    )) {
        CompositionLocalProvider(LocalTextSelectionColors provides TextSelectionColors(white, white.copy(alpha = .25f))) { content() }
    }
}

/** Clock and action bar are own components; the bird never depends on the assigned app. */
@Composable fun DigitalClock(fontSizeSp: Float = 96f, onClockClick: (() -> Unit)? = null) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) { now = LocalDateTime.now(); delay(60_000 - System.currentTimeMillis() % 60_000 + 25) }
        }
    }
    val locale = Locale.getDefault()
    val time = now.format(DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a", locale))
    val dateParts = listOf(now.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, locale).uppercase(locale),
        now.format(DateTimeFormatter.ofPattern("MMMM", locale)), now.dayOfMonth.toString())
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val bandHeight = (maxHeight.value * .18f).coerceIn(24f, 48f).dp
        val gap = (maxHeight.value * .025f).coerceIn(3f, 8f).dp
        Column(Modifier.fillMaxSize().then(if (onClockClick != null) Modifier.clickable(onClick = onClockClick) else Modifier), verticalArrangement = Arrangement.spacedBy(gap)) {
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().border(3.dp, Color.White).padding(6.dp), contentAlignment = Alignment.Center) {
                val requested = componentFontSize("Reloj", fontSizeSp)
                val style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Light,
                    fontSize = requested.sp, lineHeight = (requested * 1.1f).sp)
                val measured = measurer.measure(time, style, softWrap = false, maxLines = 1)
                val fitted = fittedClockFont(requested, with(density) { maxWidth.toPx() }, with(density) { maxHeight.toPx() }, measured.size.width.toFloat(), measured.size.height.toFloat())
                Text(time, color = Color.White, style = style.copy(fontSize = fitted.sp, lineHeight = (fitted * 1.1f).sp), maxLines = 1, softWrap = false)
            }
            Row(Modifier.fillMaxWidth().height(bandHeight).background(Color.White), verticalAlignment = Alignment.CenterVertically) {
                ClockDatePart(dateParts[0], Modifier.weight(1.35f))
                Box(Modifier.width(1.dp).fillMaxHeight(.65f).background(Color.Black.copy(alpha = .45f)))
                ClockDatePart(dateParts[1], Modifier.weight(1.25f))
                Box(Modifier.width(1.dp).fillMaxHeight(.65f).background(Color.Black.copy(alpha = .45f)))
                ClockDatePart(dateParts[2], Modifier.weight(.45f))
            }
        }
    }
}

@Composable fun ActionBar(label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(50), color = Color.White, contentColor = Color.Black) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(painterResource(R.drawable.ic_bird), contentDescription = null, modifier = Modifier.size(30.dp))
            Text(label.ifBlank { "Busca..." }, Modifier.weight(1f), color = Color.Black, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            SearchIcon(Color.Black)
        }
    }
}

@Composable private fun ClockDatePart(text: String, modifier: Modifier) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier.fillMaxHeight().padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
        val requested = 22f
        val style = TextStyle(fontSize = requested.sp, lineHeight = 26.sp)
        val measured = measurer.measure(text, style, softWrap = false, maxLines = 1)
        val fitted = fittedClockFont(requested, with(density) { maxWidth.toPx() }, with(density) { maxHeight.toPx() }, measured.size.width.toFloat(), measured.size.height.toFloat())
        Text(text, color = Color.Black, fontSize = fitted.sp, lineHeight = (fitted * 1.18f).sp, maxLines = 1, softWrap = false, textAlign = TextAlign.Center)
    }
}

/** Home contains only text: no text input, focus target, IME or editable semantics. */
@Composable fun NotePreview(text: String, fontSizeSp: Float = 18f, alignment: String = "left", family: String = "default") {
    Text(
        if (text.isBlank()) "Nota vacía" else text,
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        color = Color.White,
        textAlign = noteAlign(alignment), fontFamily = noteFont(family),
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSizeSp.sp, lineHeight = (fontSizeSp * 1.45f).sp)
    )
}

@Composable fun NoteEditor(initialText: String, initialFontSize: Float, initialAlignment: String = "left", initialFamily: String = "default", onSave: (String, Float, String, String) -> Unit, onDismiss: () -> Unit) {
    var draft by remember { mutableStateOf(initialText) }
    var alignment by remember { mutableStateOf(initialAlignment) }; var family by remember { mutableStateOf(initialFamily) }
    var fontSize by remember { mutableFloatStateOf(componentFontSize("Nota", initialFontSize)) }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    fun close() { focus.clearFocus(force = true); keyboard?.hide(); onDismiss() }
    AlertDialog(
        onDismissRequest = { close() },
        title = { Text("Editar nota") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row { listOf("left" to "Izq.", "center" to "Centro", "right" to "Der.").forEach { (key, label) -> TextButton(onClick={alignment=key}) { Text(if(alignment==key) "• $label" else label) } } }
                Row(Modifier.horizontalScroll(rememberScrollState())) { listOf("default" to "Sistema", "sans" to "Sans", "serif" to "Serif", "mono" to "Mono").forEach { (key,label) -> TextButton(onClick={family=key}) { Text(if(family==key) "• $label" else label) } } }
                Text("Tamaño del texto: ${fontSize.toInt()}")
                Slider(value = fontSize, onValueChange = { fontSize = it }, valueRange = 12f..64f)
                OutlinedTextField(
                value = draft, onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 320.dp),
                label = { Text("Nota") }, placeholder = { Text("¿Qué necesitás resolver?") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign=noteAlign(alignment),fontFamily=noteFont(family),fontSize = fontSize.sp, lineHeight = (fontSize * 1.45f).sp)
                )
            }
        },
        confirmButton = { TextButton(onClick = { focus.clearFocus(force = true); keyboard?.hide(); onSave(draft, fontSize, alignment, family) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = { close() }) { Text("Cancelar") } }
    )
}


fun noteAlign(value:String) = when(value){ "center"->TextAlign.Center; "right"->TextAlign.Right; else->TextAlign.Left }
fun noteFont(value:String) = when(value){ "sans"->FontFamily.SansSerif; "serif"->FontFamily.Serif; "mono"->FontFamily.Monospace; else->FontFamily.Default }

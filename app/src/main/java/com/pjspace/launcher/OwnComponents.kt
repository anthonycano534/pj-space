package com.pjspace.launcher

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

/** Native Compose component: no AppWidgetHost, background service or per-second timer. */
@Composable fun DigitalClock(fontSizeSp: Float = 96f) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                now = LocalDateTime.now()
                delay(60_000 - System.currentTimeMillis() % 60_000 + 25)
            }
        }
    }
    val twentyFourHour = DateFormat.is24HourFormat(context)
    val time = now.format(DateTimeFormatter.ofPattern(if (twentyFourHour) "HH:mm" else "h:mm a", Locale.getDefault()))
    val day = now.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault()))
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val requested = componentFontSize("Reloj", fontSizeSp)
        val dateSize = (requested * .2f).coerceIn(12f, 20f)
        val dateStyle = TextStyle(fontSize = dateSize.sp, lineHeight = (dateSize * 1.25f).sp)
        val timeStyle = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Light, fontSize = requested.sp, lineHeight = (requested * 1.1f).sp)
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val dateHeight = measurer.measure(day, dateStyle, maxLines = 2, constraints = Constraints(maxWidth = widthPx.toInt().coerceAtLeast(1))).size.height
        val timeMeasure = measurer.measure(time, timeStyle, softWrap = false, maxLines = 1)
        val size = fittedClockFont(requested, widthPx, heightPx - dateHeight - with(density) { 8.dp.toPx() }, timeMeasure.size.width.toFloat(), timeMeasure.size.height.toFloat())
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(time, color = Color.White, style = timeStyle.copy(fontSize = size.sp, lineHeight = (size * 1.1f).sp), maxLines = 1, softWrap = false)
            Spacer(Modifier.height(8.dp))
            Text(day, color = Color.White, style = dateStyle, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Home contains only text: no text input, focus target, IME or editable semantics. */
@Composable fun NotePreview(text: String, fontSizeSp: Float = 18f) {
    Text(
        if (text.isBlank()) "Nota vacía" else text,
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        color = Color.White,
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSizeSp.sp, lineHeight = (fontSizeSp * 1.45f).sp)
    )
}

@Composable fun NoteEditor(initialText: String, initialFontSize: Float, onSave: (String, Float) -> Unit, onDismiss: () -> Unit) {
    var draft by remember { mutableStateOf(initialText) }
    var fontSize by remember { mutableFloatStateOf(componentFontSize("Nota", initialFontSize)) }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    fun close() { focus.clearFocus(force = true); keyboard?.hide(); onDismiss() }
    AlertDialog(
        onDismissRequest = { close() },
        title = { Text("Editar nota") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Tamaño del texto: ${fontSize.toInt()}")
                Slider(value = fontSize, onValueChange = { fontSize = it }, valueRange = 12f..64f)
                OutlinedTextField(
                value = draft, onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 320.dp),
                label = { Text("Nota") }, placeholder = { Text("¿Qué necesitás resolver?") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, lineHeight = (fontSize * 1.45f).sp)
                )
            }
        },
        confirmButton = { TextButton(onClick = { focus.clearFocus(force = true); keyboard?.hide(); onSave(draft, fontSize) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = { close() }) { Text("Cancelar") } }
    )
}

@Composable fun ClockSizeEditor(initialSize: Float, onSave: (Float) -> Unit, onDismiss: () -> Unit) {
    var size by remember { mutableFloatStateOf(componentFontSize("Reloj", initialSize)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tamaño del reloj") },
        text = {
            Column {
                Text("Tamaño de la hora: ${size.toInt()}")
                Slider(size, { size = it }, valueRange = 24f..240f)
                Box(Modifier.fillMaxWidth().height(170.dp)) { DigitalClock(size) }
                Text("Agrandá también la tarjeta para dar más espacio a los números.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(size) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

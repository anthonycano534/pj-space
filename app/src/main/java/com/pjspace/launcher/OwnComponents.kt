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
@Composable fun DigitalClock() {
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
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val size = minOf(maxWidth.value / if (twentyFourHour) 3.2f else 4.8f, maxHeight.value * .45f).coerceIn(16f, 64f)
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(time, color = Color.White, fontSize = size.sp, fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light, maxLines = 1, overflow = TextOverflow.Clip)
            Text(day, color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Home contains only text: no text input, focus target, IME or editable semantics. */
@Composable fun NotePreview(text: String) {
    Text(
        if (text.isBlank()) "Nota vacía" else text,
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        color = Color.White,
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 26.sp)
    )
}

@Composable fun NoteEditor(initialText: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var draft by remember { mutableStateOf(initialText) }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    fun close() { focus.clearFocus(force = true); keyboard?.hide(); onDismiss() }
    AlertDialog(
        onDismissRequest = { close() },
        title = { Text("Editar nota") },
        text = {
            OutlinedTextField(
                value = draft, onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp, max = 320.dp),
                label = { Text("Nota") }, placeholder = { Text("¿Qué necesitás resolver?") },
                textStyle = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = { TextButton(onClick = { focus.clearFocus(force = true); keyboard?.hide(); onSave(draft) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = { close() }) { Text("Cancelar") } }
    )
}

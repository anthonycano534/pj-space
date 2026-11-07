package com.pjspace.launcher

data class Tile(val id: String, val kind: String, val x: Float, val y: Float, val w: Float, val h: Float)

// Coordinates are fractions of the usable desktop; preserve layout across screen sizes.
fun Tile.fit(): Tile {
    val width = w.coerceIn(.25f, 1f)
    val height = h.coerceIn(.18f, 1f)
    return copy(w = width, h = height, x = x.coerceIn(0f, 1f - width), y = y.coerceIn(0f, 1f - height))
}
val initialTiles = listOf(Tile("clock", "Reloj", 0f, 0f, 1f, .25f), Tile("note", "Nota", 0f, .29f, 1f, .38f))

const val LEFT_DESKTOP = 0
const val CENTRAL_DESKTOP = 1
const val APPS_DESKTOP = 2

// Keep the original key for the central desktop so upgrades preserve existing layouts.
fun desktopStorageKey(desktop: Int): String = when (desktop) {
    LEFT_DESKTOP -> "tiles-left"
    CENTRAL_DESKTOP -> "tiles"
    else -> error("The apps page does not store tiles")
}
fun defaultTiles(desktop: Int): List<Tile> = when (desktop) {
    LEFT_DESKTOP -> emptyList()
    CENTRAL_DESKTOP -> initialTiles
    else -> error("The apps page does not store tiles")
}

// Horizontal movement is exclusively handled by HorizontalPager, never an app binding.
fun verticalGestureDirection(distance: Float, threshold: Float): String? = when {
    distance <= -threshold -> "Arriba"
    distance >= threshold -> "Abajo"
    else -> null
}

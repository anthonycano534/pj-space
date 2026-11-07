package com.pjspace.launcher

import kotlin.math.abs

data class Tile(val id: String, val kind: String, val x: Float, val y: Float, val w: Float, val h: Float)

// Coordinates are fractions of the usable desktop; preserve layout across screen sizes.
fun Tile.fit(): Tile {
    val width = w.coerceIn(.25f, 1f)
    val height = h.coerceIn(.18f, 1f)
    return copy(w = width, h = height, x = x.coerceIn(0f, 1f - width), y = y.coerceIn(0f, 1f - height))
}
fun direction(x: Float, y: Float): String =
    if (abs(x) > abs(y)) { if (x > 0) "Derecha" else "Izquierda" }
    else { if (y > 0) "Abajo" else "Arriba" }

val initialTiles = listOf(Tile("clock", "Reloj", 0f, 0f, 1f, .25f), Tile("note", "Nota", 0f, .29f, 1f, .38f))

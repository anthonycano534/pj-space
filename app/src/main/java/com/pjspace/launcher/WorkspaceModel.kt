package com.pjspace.launcher

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

const val GESTURE_DISTANCE_DP = 24f
data class TodoItem(val id: String, val text: String, val done: Boolean = false)
data class AccessIcon(val key: String, val name: String)
val accessIcons = listOf(AccessIcon("plane", "Avión"), AccessIcon("book", "Libro"), AccessIcon("home", "Casa"),
    AccessIcon("work", "Trabajo"), AccessIcon("music", "Música"), AccessIcon("study", "Estudio"), AccessIcon("cart", "Compras"),
    AccessIcon("health", "Salud"), AccessIcon("tools", "Herramientas"), AccessIcon("star", "Favoritos"), AccessIcon("phone", "Teléfono"), AccessIcon("camera", "Cámara"))

fun todoProgress(items: List<TodoItem>): Float = if (items.isEmpty()) 0f else items.count { it.done }.toFloat() / items.size

fun monthGrid(month: YearMonth, firstDay: DayOfWeek): List<LocalDate?> {
    val offset = (month.atDay(1).dayOfWeek.value - firstDay.value + 7) % 7
    return List(42) { index -> (index - offset + 1).takeIf { it in 1..month.lengthOfMonth() }?.let { month.atDay(it) } }
}

fun orderedAppIds(alphabeticalIds: List<String>, savedOrder: List<String>?): List<String> {
    val current = alphabeticalIds.distinct()
    if (savedOrder == null) return current
    val kept = savedOrder.distinct().filter { it in current }
    return kept + current.filterNot { it in kept }
}

fun <T> moveItem(items: List<T>, from: Int, to: Int): List<T> {
    if (from !in items.indices || to !in items.indices || from == to) return items
    return items.toMutableList().apply { add(to, removeAt(from)) }
}

/** One-time migration. Stable bar IDs make this idempotent; unrelated cards are never moved. */
fun splitLegacyClockBars(tiles: List<Tile>, labels: Map<String, String>, apps: Map<String, String>): List<Tile> {
    val existingIds = tiles.map { it.id }.toSet()
    return tiles.flatMap { tile ->
        if (tile.kind != "Reloj" || "bar-${tile.id}" in existingIds) listOf(tile)
        else {
            val clock = tile.copy(h = tile.h * .74f).fit()
            val bar = Tile("bar-${tile.id}", "Barra", tile.x, clock.y + clock.h + .008f, tile.w, maxOf(.055f, tile.h * .23f),
                appComponent = apps[tile.id].orEmpty(), label = labels[tile.id] ?: "Busca...").fit()
            listOf(clock, bar)
        }
    }
}

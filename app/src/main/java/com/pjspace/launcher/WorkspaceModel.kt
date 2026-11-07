package com.pjspace.launcher

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

const val GESTURE_DISTANCE_DP = 24f
data class TodoItem(val id: String, val text: String, val done: Boolean = false)
data class TodoBlock(val id: String, val name: String, val items: List<TodoItem>)
data class ImportantDay(val title: String, val note: String)
fun migratedBlocks(items: List<TodoItem>) = listOf(TodoBlock("legacy", "Pendientes", items))
fun appMatches(original: String, alias: String, packageName: String, hidden: Boolean, query: String): Boolean =
    if (query.isBlank()) !hidden else listOf(original, alias, packageName).any { it.contains(query.trim(), true) }
fun Tile.snapped(): Tile {
    val fitted=fit()
    return fitted.copy(x=kotlin.math.round(fitted.x*20)/20, y=kotlin.math.round(fitted.y*20)/20,
        w=(kotlin.math.round(fitted.w*20)/20).coerceAtLeast(if(kind=="Acceso").15f else .25f),
        h=(kotlin.math.round(fitted.h*20)/20).coerceAtLeast(when(kind){"Barra"->.10f;"Acceso"->.10f;else->.20f})).fit()
}
fun Tile.duplicate(id: String) = copy(id=id, x=(x+.05f), y=(y+.05f)).fit()
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

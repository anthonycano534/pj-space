package com.pjspace.launcher
import org.junit.Assert.*
import org.junit.Test

class DesktopTest {
    @Test fun resizingAndMovingNeverEscapeDesktop() {
        for (x in listOf(-2f, 0f, .9f, 4f)) for (w in listOf(-1f, .3f, 2f)) {
            val tile = Tile("t", "Nota", x, x, w, w).fit()
            assertTrue(tile.x >= 0 && tile.y >= 0)
            assertTrue(tile.x + tile.w <= 1.00001f && tile.y + tile.h <= 1.00001f)
            assertTrue(tile.w >= .25f && tile.h >= .18f)
        }
    }
    @Test fun gesturesChooseDominantAxis() {
        assertEquals("Arriba", direction(10f, -80f))
        assertEquals("Abajo", direction(-5f, 80f))
        assertEquals("Izquierda", direction(-80f, 10f))
        assertEquals("Derecha", direction(80f, -10f))
    }
}

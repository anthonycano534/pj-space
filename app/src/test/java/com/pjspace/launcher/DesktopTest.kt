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
    @Test fun verticalGesturesRequireDistanceAndChooseDirection() {
        assertNull(verticalGestureDirection(0f, 64f))
        assertNull(verticalGestureDirection(63f, 64f))
        assertNull(verticalGestureDirection(-63f, 64f))
        assertEquals("Arriba", verticalGestureDirection(-64f, 64f))
        assertEquals("Abajo", verticalGestureDirection(64f, 64f))
    }
    @Test fun desktopStoragePreservesOldCentralKeyAndSeparatesLeft() {
        assertEquals("tiles", desktopStorageKey(CENTRAL_DESKTOP))
        assertNotEquals(desktopStorageKey(CENTRAL_DESKTOP), desktopStorageKey(LEFT_DESKTOP))
        assertTrue(defaultTiles(LEFT_DESKTOP).isEmpty())
        assertEquals(initialTiles, defaultTiles(CENTRAL_DESKTOP))
    }
    @Test(expected = IllegalStateException::class) fun appsPageCannotStoreWidgets() {
        desktopStorageKey(APPS_DESKTOP)
    }
}

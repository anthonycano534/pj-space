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
    @Test fun backAtCentralDesktopStaysWithoutClosingLauncher() {
        assertEquals(LauncherBackAction.STAY_HOME, launcherBackAction(false, false, false, CENTRAL_DESKTOP))
        assertEquals(LauncherBackAction.OPEN_HOME, launcherBackAction(false, false, false, LEFT_DESKTOP))
        assertEquals(LauncherBackAction.OPEN_HOME, launcherBackAction(false, false, false, APPS_DESKTOP))
        assertEquals(LauncherBackAction.OPEN_HOME, launcherBackAction(false, false, true, CENTRAL_DESKTOP))
    }
    @Test fun backClosesDialogBeforeEditingOrNavigation() {
        assertEquals(LauncherBackAction.CLOSE_DIALOG, launcherBackAction(true, true, true, LEFT_DESKTOP))
        assertEquals(LauncherBackAction.FINISH_EDITING, launcherBackAction(false, true, false, LEFT_DESKTOP))
    }
    @Test fun largeClockIsNotCappedAtOldFontLimit() {
        assertEquals(160f, fittedClockFont(160f, 900f, 500f, 600f, 180f), .001f)
    }
    @Test fun clockFitsWidthAndHeightWithoutExceedingRequestedSize() {
        assertEquals(80f, fittedClockFont(160f, 300f, 500f, 600f, 180f), .001f)
        assertEquals(40f, fittedClockFont(160f, 900f, 45f, 600f, 180f), .001f)
    }
    @Test fun fontSettingSurvivesGeometryChangesAndOldTilesHaveDefaults() {
        val tile = Tile("note", "Nota", 0f, 0f, .5f, .4f, 32f)
        assertEquals(32f, tile.copy(x = .9f, w = .8f).fit().fontSizeSp!!, .001f)
        assertEquals(18f, componentFontSize("Nota", null), .001f)
        assertEquals(96f, componentFontSize("Reloj", null), .001f)
        assertEquals(96f, componentFontSize("Reloj", Float.NaN), .001f)
    }
    @Test fun brushNeverSelectsApplicationsAsAnEditableDesktop() {
        assertEquals(LEFT_DESKTOP, editingTargetPage(LEFT_DESKTOP, CENTRAL_DESKTOP))
        assertEquals(CENTRAL_DESKTOP, editingTargetPage(CENTRAL_DESKTOP, LEFT_DESKTOP))
        assertEquals(LEFT_DESKTOP, editingTargetPage(APPS_DESKTOP, LEFT_DESKTOP))
        assertEquals(CENTRAL_DESKTOP, editingTargetPage(APPS_DESKTOP, CENTRAL_DESKTOP))
        assertEquals(CENTRAL_DESKTOP, editingTargetPage(-1, APPS_DESKTOP))
    }
    @Test fun phoneAndCameraHaveIndependentSavedActions() {
        assertEquals("corner-phone-app", cornerBindingKey("Teléfono"))
        assertEquals("corner-camera-app", cornerBindingKey("Cámara"))
        assertNotEquals(cornerBindingKey("Teléfono"), cornerBindingKey("Cámara"))
    }
}

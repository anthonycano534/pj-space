package com.pjspace.launcher

import org.junit.Assert.*
import org.junit.Test

class ExperienceTest {
    @Test fun extraDesktopPreservesStableIdsAndStorageKeys() {
        assertEquals(listOf(3,0,1,2),desktopPages(true))
        assertEquals(listOf(0,1,2),desktopPages(false))
        assertEquals("tiles",desktopStorageKey(CENTRAL_DESKTOP))
        assertEquals("tiles-left",desktopStorageKey(LEFT_DESKTOP))
        assertEquals("tiles-extra",desktopStorageKey(EXTRA_DESKTOP))
        assertTrue(defaultTiles(EXTRA_DESKTOP).isEmpty())
    }
    @Test fun hiddenAppsRequireSearchAndMatchOriginalOrAlias() {
        assertFalse(appMatches("Chrome","Navegador","com.android.chrome",true,""))
        assertFalse(appMatches("Chrome","Navegador","com.android.chrome",true,"   "))
        assertTrue(appMatches("Chrome","Navegador","com.android.chrome",true," chrome "))
        assertTrue(appMatches("Chrome","Navegador","com.android.chrome",true,"NAVEGADOR"))
        assertFalse(appMatches("Chrome","Navegador","com.android.chrome",true,"Correo"))
        assertTrue(appMatches("Chrome","Navegador","com.android.chrome",false,""))
    }
    @Test fun migrationKeepsEveryLegacyTaskAndCompletionState() {
        val old=listOf(TodoItem("a","A",true),TodoItem("b","B"))
        val blocks=migratedBlocks(old)
        assertEquals(old,blocks.single().items)
        assertEquals(.5f,todoProgress(blocks.single().items),.001f)
        assertEquals(0f,todoProgress(TodoBlock("other","Otra lista",emptyList()).items),.001f)
    }
    @Test fun pastedAccessKeepsActionAppearanceAndIndependentIdentity() {
        val source=Tile("a","Acceso",.9f,.9f,.2f,.15f,24f,"plane","app/.Main","Viajar")
        val duplicate=source.duplicate("b")
        assertNotEquals(source.id,duplicate.id)
        assertEquals(source.appComponent,duplicate.appComponent)
        assertEquals(source.iconKey,duplicate.iconKey)
        assertEquals(source.label,duplicate.label)
        assertEquals(source.w,duplicate.w,.001f)
        assertTrue(duplicate.x+duplicate.w<=1.0001f)
    }
    @Test fun snappingRemainsAlignedAndInsideScreenForEveryComponent() {
        for(kind in listOf("Reloj","Barra","Acceso","Nota","Pendientes","Calendario","Widget:4"))for(i in 0..100){
            val tile=Tile("x",kind,i/100f,i/100f,.273f,.221f).snapped()
            assertTrue(tile.x>=0 && tile.y>=0 && tile.x+tile.w<=1.0001f && tile.y+tile.h<=1.0001f)
            listOf(tile.x,tile.y,tile.w,tile.h).forEach { assertEquals(kotlin.math.round(it*20),it*20,.001f) }
        }
    }
    @Test fun fittingRetainsPerNoteTypography() {
        val tile=Tile("note","Nota",0f,0f,.5f,.5f,32f,textAlignment="center",fontFamily="serif").fit()
        assertEquals("center",tile.textAlignment);assertEquals("serif",tile.fontFamily);assertEquals(32f,tile.fontSizeSp!!,.001f)
    }
}

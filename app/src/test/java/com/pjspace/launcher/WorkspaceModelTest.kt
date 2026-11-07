package com.pjspace.launcher

import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.YearMonth

class WorkspaceModelTest {
    @Test fun splitPreservesExistingNotesAndBarActionAndDoesNotDuplicateOnRepeat() {
        val clock=Tile("old-clock","Reloj",.1f,.1f,.8f,.4f,120f)
        val note=Tile("old-note","Nota",.1f,.6f,.8f,.3f,28f)
        val migrated=splitLegacyClockBars(listOf(clock,note),mapOf("old-clock" to "Mi navegador"),mapOf("old-clock" to "browser/.Main"))
        assertEquals(3,migrated.size)
        assertEquals(note,migrated.last())
        val bar=migrated.single{it.kind=="Barra"}
        assertEquals("Mi navegador",bar.label);assertEquals("browser/.Main",bar.appComponent)
        assertEquals(120f,migrated.first().fontSizeSp!!,.001f)
        assertEquals(migrated,splitLegacyClockBars(migrated,emptyMap(),emptyMap()))
    }
    @Test fun barAndAccessCanBeCompactWithoutEscapingScreen() {
        listOf("Barra","Acceso").forEach { kind->
            val tile=Tile("x",kind,.99f,.99f,.2f,.07f,iconKey="book",appComponent="reader/.Main",label="Leer").fit()
            assertTrue(tile.x+tile.w<=1.00001f);assertTrue(tile.y+tile.h<=1.00001f)
            assertEquals("reader/.Main",tile.appComponent);assertEquals("book",tile.iconKey)
        }
    }
    @Test fun customAppOrderRemovesMissingAndDuplicatesAndAppendsNewApps() {
        assertEquals(listOf("c","a","b","d"),orderedAppIds(listOf("a","b","c","d"),listOf("c","missing","a","a")))
        assertEquals(listOf("a","b","c"),orderedAppIds(listOf("a","b","c"),null))
        assertEquals(listOf("c","a","b","d","e"),orderedAppIds(listOf("a","b","c","d","e"),listOf("c","a","b","d")))
    }
    @Test fun reorderingCanMoveAnAppToFirstOrLastWithoutLosingEntries() {
        assertEquals(listOf("b","c","a"),moveItem(listOf("a","b","c"),0,2))
        assertEquals(listOf("c","a","b"),moveItem(listOf("a","b","c"),2,0))
        assertEquals(listOf("a"),moveItem(listOf("a"),-1,2))
    }
    @Test fun monthGridCoversEveryDayIncludingLeapYearsAndEveryWeekStart() {
        for(year in 2023..2026)for(month in 1..12)for(first in DayOfWeek.values()) {
            val value=YearMonth.of(year,month);val grid=monthGrid(value,first);val dates=grid.filterNotNull()
            assertEquals(42,grid.size);assertEquals(value.lengthOfMonth(),dates.size)
            assertEquals(value.atDay(1),dates.first());assertEquals(value.atEndOfMonth(),dates.last())
            assertEquals(dates.size,dates.distinct().size)
            assertEquals((value.atDay(1).dayOfWeek.value-first.value+7)%7,grid.indexOf(value.atDay(1)))
        }
    }
    @Test fun todoRingHandlesEmptyPartialAndCompleteLists() {
        assertEquals(0f,todoProgress(emptyList()),.001f)
        assertEquals(.5f,todoProgress(listOf(TodoItem("a","A",true),TodoItem("b","B"))),.001f)
        assertEquals(1f,todoProgress(listOf(TodoItem("a","A",true))),.001f)
    }
    @Test fun shortVerticalSwipeIsEnoughButATapIsNot() {
        assertEquals("Arriba",verticalGestureDirection(-25f,GESTURE_DISTANCE_DP))
        assertEquals("Abajo",verticalGestureDirection(25f,GESTURE_DISTANCE_DP))
        assertNull(verticalGestureDirection(5f,GESTURE_DISTANCE_DP))
    }
}

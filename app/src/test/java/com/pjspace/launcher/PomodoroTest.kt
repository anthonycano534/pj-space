package com.pjspace.launcher

import org.junit.Assert.*
import org.junit.Test

class PomodoroTest {
    @Test fun defaultsAndTimeRounding() {
        val s=PomodoroState();assertEquals(25,s.focusMinutes);assertEquals(5,s.restMinutes)
        assertEquals("25:00",pomodoroTime(s.remaining(999)))
        assertEquals("00:01",pomodoroTime(1));assertEquals("00:00",pomodoroTime(-1))
    }
    @Test fun pauseResumeUsesDeadlineWithoutLosingTimeToUiTicks() {
        val s=PomodoroState().start(1_000,20_000,1,"a")
        val paused=s.pause(61_000)
        assertEquals(24*60_000L,paused.remainingMs);assertEquals(PomodoroStatus.PAUSED,paused.status)
        val resumed=paused.start(500_000,900_000,1,"b")
        assertEquals(24*60_000L,resumed.remaining(500_000));assertEquals(23*60_000L,resumed.remaining(560_000))
        assertEquals(paused.remainingMs,paused.remaining(2_000_000))
    }
    @Test fun completionIsNeitherEarlyNorDuplicatedAndIgnoresStaleAlarm() {
        val s=PomodoroState().configure(1,1).start(1_000,50_000,1,"current")
        assertEquals(s,s.finish(61_000,"old"));assertEquals(s,s.finish(60_999,"current"))
        val ended=s.finish(61_000,"current")
        assertEquals(PomodoroStatus.FINISHED,ended.status);assertEquals(1,ended.cycles)
        assertEquals(ended,ended.finish(99_000,"current"))
    }
    @Test fun focusThenRestRequireExplicitStartAndOnlyFocusIncrementsCycles() {
        val focus=PomodoroState().configure(1,2).start(0,1_000,1,"a").finish(60_000,"a")
        assertEquals(PomodoroPhase.FOCUS,focus.phase);assertEquals(0L,focus.remainingMs)
        val rest=focus.start(80_000,100_000,1,"b")
        assertEquals(PomodoroPhase.REST,rest.phase);assertEquals(120_000L,rest.remaining(80_000))
        val ended=rest.finish(200_000,"b");assertEquals(1,ended.cycles)
        assertEquals(PomodoroPhase.FOCUS,ended.start(300_000,400_000,1,"c").phase)
    }
    @Test fun resetAndReconfigureInvalidateScheduledAlarmTokens() {
        val s=PomodoroState().start(0,0,1,"old")
        val reset=s.reset();assertEquals(PomodoroStatus.READY,reset.status);assertEquals("",reset.token)
        assertEquals(reset,reset.finish(99999999,"old"))
        val updated=s.configure(10,3);assertEquals(600_000L,updated.remainingMs);assertEquals("",updated.token)
        assertEquals(1,s.configure(-1,0).focusMinutes);assertEquals(180,s.configure(999,999).restMinutes)
    }
    @Test fun rebootRecoversRemainingWallTimeAndNormalResumeKeepsMonotonicClock() {
        val s=PomodoroState().configure(1,1).start(5_000,100_000,1,"a")
        assertEquals(s,s.recoverBoot(50_000,500_000,1))
        val reboot=s.recoverBoot(2_000,130_000,2)
        assertEquals(30_000L,reboot.remaining(2_000));assertEquals("a",reboot.token)
        val overdue=s.recoverBoot(1_000,200_000,2)
        assertEquals(0L,overdue.remaining(1_000));assertEquals(PomodoroStatus.FINISHED,overdue.finish(1_000,"a").status)
    }
    @Test fun selectorOnlyOffersExistingDesktopsAndEditsOnlyChosenTarget() {
        assertEquals(listOf(CENTRAL_DESKTOP,LEFT_DESKTOP,APPS_DESKTOP),editingOptions(false).map{it.first})
        assertEquals(listOf(CENTRAL_DESKTOP,LEFT_DESKTOP,EXTRA_DESKTOP,APPS_DESKTOP),editingOptions(true).map{it.first})
        assertTrue(editsDesktop(true,LEFT_DESKTOP,LEFT_DESKTOP));assertFalse(editsDesktop(true,LEFT_DESKTOP,APPS_DESKTOP));assertFalse(editsDesktop(false,LEFT_DESKTOP,LEFT_DESKTOP))
        assertEquals(listOf(LEFT_DESKTOP,CENTRAL_DESKTOP,APPS_DESKTOP),desktopPages(false))
    }
}

package com.pjspace.launcher

enum class PomodoroPhase { FOCUS, REST }
enum class PomodoroStatus { READY, RUNNING, PAUSED, FINISHED }

/** Deadlines, not UI ticks, own time. Wall time is only needed to recover after reboot. */
data class PomodoroState(
    val focusMinutes:Int=25, val restMinutes:Int=5,
    val phase:PomodoroPhase=PomodoroPhase.FOCUS, val status:PomodoroStatus=PomodoroStatus.READY,
    val remainingMs:Long=25*60_000L, val endElapsed:Long=0, val endWall:Long=0,
    val bootCount:Int=-1, val token:String="", val cycles:Int=0
) {
    fun duration(phase:PomodoroPhase=this.phase) = (if(phase==PomodoroPhase.FOCUS)focusMinutes else restMinutes).coerceIn(1,180)*60_000L
    fun remaining(now:Long) = if(status==PomodoroStatus.RUNNING)(endElapsed-now).coerceAtLeast(0) else remainingMs.coerceAtLeast(0)
    fun start(now:Long,wall:Long,boot:Int,newToken:String):PomodoroState {
        if(status==PomodoroStatus.RUNNING)return this
        val next=if(status==PomodoroStatus.FINISHED) { if(phase==PomodoroPhase.FOCUS)PomodoroPhase.REST else PomodoroPhase.FOCUS }else phase
        val duration=if(status==PomodoroStatus.FINISHED)duration(next)else remainingMs.coerceAtLeast(1)
        return copy(phase=next,status=PomodoroStatus.RUNNING,remainingMs=duration,endElapsed=now+duration,endWall=wall+duration,bootCount=boot,token=newToken)
    }
    fun pause(now:Long)=if(status==PomodoroStatus.RUNNING)copy(status=PomodoroStatus.PAUSED,remainingMs=remaining(now),endElapsed=0,endWall=0,token="")else this
    fun reset()=copy(status=PomodoroStatus.READY,remainingMs=duration(),endElapsed=0,endWall=0,token="")
    fun configure(focus:Int,rest:Int)=copy(focusMinutes=focus.coerceIn(1,180),restMinutes=rest.coerceIn(1,180)).reset()
    fun finish(now:Long,expectedToken:String):PomodoroState =
        if(status!=PomodoroStatus.RUNNING || token!=expectedToken || remaining(now)>0)this
        else copy(status=PomodoroStatus.FINISHED,remainingMs=0,endElapsed=0,endWall=0,token="",cycles=cycles+if(phase==PomodoroPhase.FOCUS)1 else 0)
    fun recoverBoot(now:Long,wall:Long,boot:Int):PomodoroState =
        if(status!=PomodoroStatus.RUNNING || bootCount==boot)this
        else copy(endElapsed=now+(endWall-wall).coerceAtLeast(0),bootCount=boot)
}

fun pomodoroTime(ms:Long):String {val seconds=(ms.coerceAtLeast(0)+999)/1000;return "%02d:%02d".format(java.util.Locale.ROOT,seconds/60,seconds%60)}
fun editingOptions(extra:Boolean)=listOf(CENTRAL_DESKTOP to "Escritorio principal",LEFT_DESKTOP to "Escritorio secundario 1")+
    (if(extra)listOf(EXTRA_DESKTOP to "Escritorio secundario 2")else emptyList())+listOf(APPS_DESKTOP to "Lista de aplicaciones")
fun editsDesktop(editing:Boolean,target:Int,current:Int)=editing && target==current

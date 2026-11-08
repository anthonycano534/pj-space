package com.pjspace.launcher

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import java.util.UUID

class PomodoroAlarms(context:Context) {
    private val context=context.applicationContext
    val prefs=this.context.getSharedPreferences("desktop-v1",Context.MODE_PRIVATE)
    private val alarms=this.context.getSystemService(AlarmManager::class.java)
    private val notifications=this.context.getSystemService(NotificationManager::class.java)
    companion object { const val CHANNEL="pomodoro-alerts-v1";const val ACTION="com.pjspace.launcher.POMODORO_END";private val lock=Any() }
    private fun boot()=try{Settings.Global.getInt(context.contentResolver,Settings.Global.BOOT_COUNT,-1)}catch(_:Exception){-1}
    fun read(id:String):PomodoroState = try {
        val o=JSONObject(prefs.getString("pomodoro-$id","{}")?:"{}")
        PomodoroState(o.optInt("focus",25).coerceIn(1,180),o.optInt("rest",5).coerceIn(1,180),
            PomodoroPhase.valueOf(o.optString("phase","FOCUS")),PomodoroStatus.valueOf(o.optString("status","READY")),
            o.optLong("remaining",25*60_000L),o.optLong("elapsed"),o.optLong("wall"),o.optInt("boot",-1),o.optString("token"),o.optInt("cycles"))
    }catch(_:Exception){PomodoroState()}
    private fun write(id:String,s:PomodoroState)=prefs.edit().putString("pomodoro-$id",JSONObject()
        .put("focus",s.focusMinutes).put("rest",s.restMinutes).put("phase",s.phase.name).put("status",s.status.name)
        .put("remaining",s.remainingMs).put("elapsed",s.endElapsed).put("wall",s.endWall).put("boot",s.bootCount).put("token",s.token).put("cycles",s.cycles).toString()).commit()
    fun ensureChannel() {
        val channel=NotificationChannel(CHANNEL,"Pomodoro: concentración y descanso",NotificationManager.IMPORTANCE_HIGH).apply {
            description="Alerta al terminar un contador Pomodoro";enableVibration(true);vibrationPattern=longArrayOf(0,350,200,350)
            setSound(Settings.System.DEFAULT_ALARM_ALERT_URI,AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
        }
        notifications.createNotificationChannel(channel)
    }
    fun notificationAccess():Boolean = (Build.VERSION.SDK_INT<33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED) && notifications.areNotificationsEnabled() && notifications.getNotificationChannel(CHANNEL)?.importance!=NotificationManager.IMPORTANCE_NONE
    fun exactAccess()=Build.VERSION.SDK_INT<31 || alarms.canScheduleExactAlarms()
    fun ready()=notificationAccess() && exactAccess()
    private fun intent(id:String)=Intent(context,PomodoroReceiver::class.java).setAction(ACTION).setData(Uri.parse("pjspace://pomodoro/${Uri.encode(id)}")).putExtra("id",id)
    private fun pending(id:String,token:String)=PendingIntent.getBroadcast(context,0,intent(id).putExtra("token",token),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    private fun cancel(id:String) {
        val pending=PendingIntent.getBroadcast(context,0,intent(id),PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if(pending!=null){alarms.cancel(pending);pending.cancel()}
        notifications.cancel(id,182)
    }
    private fun schedule(id:String,s:PomodoroState) {
        val show=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        // Alarm-clock delivery also covers short breaks during Doze, without a permanent service.
        alarms.setAlarmClock(AlarmManager.AlarmClockInfo(System.currentTimeMillis()+s.remaining(SystemClock.elapsedRealtime()),show),pending(id,s.token))
    }
    fun start(id:String):Boolean = synchronized(lock) {
        ensureChannel();if(!ready())return@synchronized false
        val old=read(id);if(old.status==PomodoroStatus.RUNNING)return@synchronized true
        val next=old.start(SystemClock.elapsedRealtime(),System.currentTimeMillis(),boot(),UUID.randomUUID().toString())
        cancel(id);if(!write(id,next))return@synchronized false
        try{schedule(id,next);true}catch(_:Exception){cancel(id);write(id,old);false}
    }
    fun pause(id:String)=synchronized(lock){val s=read(id);if(s.status==PomodoroStatus.RUNNING && s.remaining(SystemClock.elapsedRealtime())==0L)complete(id,s.token)else{cancel(id);write(id,s.pause(SystemClock.elapsedRealtime()))};Unit}
    fun reset(id:String)=synchronized(lock){cancel(id);write(id,read(id).reset());Unit}
    fun configure(id:String,focus:Int,rest:Int)=synchronized(lock){cancel(id);write(id,read(id).configure(focus,rest));Unit}
    fun delete(id:String)=synchronized(lock){cancel(id);prefs.edit().remove("pomodoro-$id").commit();Unit}
    fun complete(id:String,token:String)=synchronized(lock) {
        val old=read(id);val now=SystemClock.elapsedRealtime();val finished=old.finish(now,token)
        if(finished==old){
            // If the wall clock changed before its broadcast arrived, re-arm against elapsed time.
            if(old.status==PomodoroStatus.RUNNING && old.token==token && old.remaining(now)>0 && ready())try{schedule(id,old)}catch(_:Exception){}
            return@synchronized
        }
        if(!write(id,finished))return@synchronized
        ensureChannel()
        if(notificationAccess()) {
            val title=if(old.phase==PomodoroPhase.FOCUS)"Concentración terminada"else"Descanso terminado"
            val text=if(old.phase==PomodoroPhase.FOCUS)"Buen trabajo. Ya podés iniciar tu descanso."else"Listo para otro Pomodoro."
            val open=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val alert=NotificationCompat.Builder(context,CHANNEL).setSmallIcon(R.drawable.ic_pomodoro_notification).setContentTitle(title).setContentText(text)
                .setCategory(NotificationCompat.CATEGORY_ALARM).setPriority(NotificationCompat.PRIORITY_HIGH).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(open).setAutoCancel(true).build()
            try{notifications.notify(id,182,alert)}catch(_:SecurityException){}
        }
    }
    fun restoreAll(reboot:Boolean=false)=synchronized(lock) {
        val ids=prefs.all.keys.filter{it.startsWith("pomodoro-")}.map{it.removePrefix("pomodoro-")}
        if(ids.isEmpty())return@synchronized
        ensureChannel()
        ids.forEach{id->val old=read(id)
            if(old.status==PomodoroStatus.RUNNING){
                val now=SystemClock.elapsedRealtime();val wall=System.currentTimeMillis()
                val restored=(if(reboot)old.copy(bootCount=Int.MIN_VALUE)else old).recoverBoot(now,wall,boot())
                val s=restored.copy(endWall=wall+restored.remaining(now))
                if(s!=old)write(id,s)
                if(!ready()){cancel(id);write(id,s.pause(SystemClock.elapsedRealtime()))}
                else if(s.remaining(SystemClock.elapsedRealtime())==0L)complete(id,s.token)
                else try{schedule(id,s)}catch(_:Exception){cancel(id);write(id,s.pause(SystemClock.elapsedRealtime()))}
            }
        }
    }
}

class PomodoroReceiver:BroadcastReceiver() {
    override fun onReceive(context:Context,intent:Intent) {
        val alarms=PomodoroAlarms(context)
        when(intent.action){
            PomodoroAlarms.ACTION->{val id=intent.getStringExtra("id")?:return;val token=intent.getStringExtra("token")?:return;alarms.complete(id,token)}
            Intent.ACTION_BOOT_COMPLETED->alarms.restoreAll(reboot=true)
            Intent.ACTION_MY_PACKAGE_REPLACED,Intent.ACTION_TIME_CHANGED,AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED->alarms.restoreAll()
        }
    }
}

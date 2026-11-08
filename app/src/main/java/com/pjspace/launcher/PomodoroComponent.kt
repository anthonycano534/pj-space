package com.pjspace.launcher

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay

@Composable fun PomodoroComponent(id:String,editing:Boolean,onEdit:()->Unit) {
    val context=LocalContext.current
    val alarms=remember(context){PomodoroAlarms(context).also{it.ensureChannel()}}
    var state by remember(id){mutableStateOf(alarms.read(id))}
    var now by remember{mutableLongStateOf(SystemClock.elapsedRealtime())}
    var access by remember{mutableStateOf(false)};var permissionRevision by remember{mutableIntStateOf(0)}
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){permissionRevision++}
    DisposableEffect(id,alarms){val listener=SharedPreferences.OnSharedPreferenceChangeListener{_,key->if(key=="pomodoro-$id")state=alarms.read(id)};alarms.prefs.registerOnSharedPreferenceChangeListener(listener);onDispose{alarms.prefs.unregisterOnSharedPreferenceChangeListener(listener)}}
    LaunchedEffect(lifecycle,id,state.status,state.token){lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
        state=alarms.read(id);now=SystemClock.elapsedRealtime();permissionRevision++
        if(state.status==PomodoroStatus.RUNNING)while(true){now=SystemClock.elapsedRealtime();delay(1000-now%1000)}
    }}
    fun start(){if(!alarms.ready()){access=true;return};if(!alarms.start(id))Toast.makeText(context,"No se pudo programar la alerta. Revisá los permisos e intentá otra vez.",Toast.LENGTH_LONG).show()}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
        TextButton(onClick=onEdit,modifier=Modifier.fillMaxWidth()){Text("Pomodoro",style=MaterialTheme.typography.titleLarge)}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){
            Column(horizontalAlignment=Alignment.CenterHorizontally){Text("Concentración",fontSize=12.sp);Text(pomodoroTime(if(state.phase==PomodoroPhase.FOCUS)state.remaining(now)else state.duration(PomodoroPhase.FOCUS)),fontSize=if(state.phase==PomodoroPhase.FOCUS)32.sp else 22.sp)}
            Column(horizontalAlignment=Alignment.CenterHorizontally){Text("Descanso",fontSize=12.sp);Text(pomodoroTime(if(state.phase==PomodoroPhase.REST)state.remaining(now)else state.duration(PomodoroPhase.REST)),fontSize=if(state.phase==PomodoroPhase.REST)32.sp else 22.sp)}
        }
        Text(when(state.status){PomodoroStatus.RUNNING->"En marcha";PomodoroStatus.PAUSED->"En pausa";PomodoroStatus.FINISHED->"Intervalo terminado";else->"Listo para empezar"},Modifier.padding(top=6.dp),fontSize=12.sp)
        Row {
            TextButton(enabled=!editing,onClick={if(state.status==PomodoroStatus.RUNNING)alarms.pause(id)else start()}){Text(when(state.status){PomodoroStatus.RUNNING->"Pausar";PomodoroStatus.PAUSED->"Continuar";PomodoroStatus.FINISHED->if(state.phase==PomodoroPhase.FOCUS)"Iniciar descanso"else"Iniciar concentración";else->"Iniciar"})}
            TextButton(enabled=!editing,onClick={alarms.reset(id)}){Text("Reiniciar")}
        }
        Text("${state.cycles} pomodoros completados",fontSize=12.sp)
    }
    if(access) {
        // Revision makes grants/denials visible immediately, including a return from Settings.
        key(permissionRevision){AlertDialog(onDismissRequest={access=false},title={Text("Activar alertas de Pomodoro")},text={Column {
            Text("Para avisarte con otra app abierta o la pantalla apagada, habilitá las notificaciones y las alarmas. Después volvé y tocá Iniciar.")
            TextButton(onClick={
                if(Build.VERSION.SDK_INT>=33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED && !alarms.prefs.getBoolean("notification-requested",false)){
                    alarms.prefs.edit().putBoolean("notification-requested",true).apply();permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }else{
                    val intent=if(alarms.notificationAccess())Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_CHANNEL_ID,PomodoroAlarms.CHANNEL)else Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE,context.packageName);try{context.startActivity(intent)}catch(_:Exception){Toast.makeText(context,"Abrí los ajustes de notificaciones de PJ Space",Toast.LENGTH_LONG).show()}
                }
            }){Text(if(alarms.notificationAccess())"Notificaciones activadas · revisar sonido"else"Activar notificaciones")}
            if(Build.VERSION.SDK_INT>=31)TextButton(onClick={try{context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:${context.packageName}")))}catch(_:Exception){Toast.makeText(context,"Abrí Alarmas y recordatorios en los ajustes de Android",Toast.LENGTH_LONG).show()}}){Text(if(alarms.exactAccess())"Alarmas habilitadas"else"Habilitar alarmas")}
            Text("El sonido y la vibración respetan el volumen y los ajustes de Android.",fontSize=12.sp)
        }},confirmButton={TextButton(onClick={access=false}){Text("Volver al contador")}})}
    }
}

@Composable fun PomodoroEditor(id:String,onDismiss:()->Unit) {
    val context=LocalContext.current;val alarms=remember(context){PomodoroAlarms(context)};val initial=remember(id){alarms.read(id)}
    var focusMinutes by remember(id){mutableStateOf(initial.focusMinutes.toString())};var restMinutes by remember(id){mutableStateOf(initial.restMinutes.toString())}
    val focus=LocalFocusManager.current;val keyboard=LocalSoftwareKeyboardController.current
    val valid=(focusMinutes.toIntOrNull()?:0) in 1..180 && (restMinutes.toIntOrNull()?:0) in 1..180
    fun close(){focus.clearFocus(force=true);keyboard?.hide();onDismiss()}
    AlertDialog(onDismissRequest={close()},title={Text("Editar Pomodoro")},text={Column(Modifier.verticalScroll(rememberScrollState())) {
        OutlinedTextField(focusMinutes,{focusMinutes=it.filter(Char::isDigit).take(3)},label={Text("Concentración · minutos")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
        OutlinedTextField(restMinutes,{restMinutes=it.filter(Char::isDigit).take(3)},label={Text("Descanso · minutos")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
        Text("De 1 a 180 minutos. Guardar reinicia el intervalo actual y cancela su alerta pendiente.")
    }},confirmButton={TextButton(enabled=valid,onClick={alarms.configure(id,focusMinutes.toInt(),restMinutes.toInt());close()}){Text("Guardar")}},dismissButton={TextButton(onClick={close()}){Text("Cancelar")}})
}


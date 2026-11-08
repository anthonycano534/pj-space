# PJ Space 0.1.8 · Pendientes y Pomodoro

Prender. Resolver. Apagar.

## Cambios

- Tocar el título superior **Pendientes** abre su editor desde Inicio. Se pueden agregar, renombrar o borrar bloques y administrar tareas sin activar Modo edición. Guardar confirma; Cancelar descarta el borrador. Marcar tareas y navegar entre bloques sigue disponible en Inicio.
- El pincel muestra **¿Qué querés editar?**: principal, secundario 1, secundario 2 si existe y lista de aplicaciones. Solo la pantalla elegida entra en edición. Listo guarda el orden de apps cuando corresponde. Si navegás a otra página, la barra ofrece volver al área elegida.
- Ajustes permite **Eliminar escritorio secundario 2** con confirmación. Se borran únicamente sus componentes y datos, se liberan sus widgets externos y se cancelan sus Pomodoros. El principal y el secundario 1 se conservan. Puede añadirse de nuevo vacío.
- Nuevo componente **Pomodoro**, disponible en Añadir componente: concentración de 25 minutos y descanso de 5, configurables entre 1 y 180 minutos tocando su título desde Inicio. Iniciar, pausar, continuar y reiniciar; contador de pomodoros completados.
- Al llegar a cero aparece una notificación con sonido de alarma y vibración. El siguiente intervalo se inicia manualmente: Iniciar descanso o Iniciar concentración. No se encadenan alertas sin intervención.

## Alertas de Pomodoro

El primer inicio guía para permitir notificaciones y, en Android 12 o posterior cuando corresponda, alarmas y recordatorios. Después de habilitarlos, volver al contador y tocar Iniciar. El temporizador no comienza si faltan estos permisos.

Se programa una alarma del sistema por intervalo, con `setAlarmClock` y receptor explícito. No depende de mantener Compose visible ni de un servicio permanente. Android puede mostrarla entre las próximas alarmas. Pausar, reiniciar, cambiar duraciones o eliminar el componente cancela la programación anterior. Los tokens descartan entregas antiguas y evitan avisos duplicados.

Se conserva el tiempo mediante una fecha límite monotónica; cambiar la hora no modifica el intervalo en curso. Se recupera la programación tras actualizar la app o reiniciar el teléfono, usando la fecha guardada en este último caso. Un intervalo vencido mientras el teléfono estaba apagado avisa al recuperar la programación después del encendido/desbloqueo. Si se revocan permisos, al volver a la app los intervalos activos quedan pausados.

El sonido y la vibración respetan la configuración del canal, volumen y No molestar de Android. Forzar la detención de la app desde Android impide sus alarmas hasta volver a abrirla. Falta validar la entrega real en los dispositivos del usuario, incluyendo sus restricciones de batería.

Referencias: [alarmas de Android](https://developer.android.com/develop/background-work/services/alarms) y [permiso de notificaciones](https://developer.android.com/develop/ui/compose/notifications/notification-permission).

## Prueba rápida

1. Actualizar sin desinstalar. Tocar Pendientes desde Inicio, agregar un bloque y quitar una tarea; guardar y comprobar que persiste.
2. Tocar el pincel: verificar el selector y editar por separado cada pantalla. La lista de apps no debe entrar en edición hasta elegirla.
3. Añadir el secundario 2, colocar un componente y eliminarlo desde Ajustes. Cancelar primero para comprobar que se conserva; confirmar después y verificar los otros escritorios.
4. Añadir Pomodoro al principal. Tocar su título y configurar concentración y descanso de 1 minuto para probar. Permitir avisos y alarmas e iniciar.
5. Abrir otra app o apagar la pantalla durante un minuto. Comprobar aviso único, sonido y vibración. Volver e iniciar descanso; repetir.
6. Pausar a mitad de intervalo, esperar y continuar: el tiempo pausado no debe descontarse. Reiniciar antes del vencimiento: no debe sonar la alarma anterior.
7. Eliminar un Pomodoro en marcha, o su escritorio: no debe quedar su alarma programada. Comprobar también actualización, reinicio del dispositivo y permisos denegados.

La compilación y las pruebas automáticas del modelo no sustituyen estas pruebas de interfaz y alarmas reales.

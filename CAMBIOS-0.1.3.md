# PJ Space 0.1.3 · negro, blanco y modo edición

## Experiencia

- Fondo negro y textos/controles blancos en la interfaz de PJ Space, incluido el inicio de la actividad. No se ocultan las barras del sistema ni se recupera el modo de pantalla completa de 0.2.
- Se priorizan los componentes propios: **Nota** y **Reloj** aparecen antes que **Widget externo** en Modo edición. Los externos siguen disponibles y se conservan los ya agregados; su estilo lo define cada proveedor.
- El reloj digital es un componente interno de Compose: muestra hora, día y fecha, respeta formato 12/24 horas y el idioma del teléfono. Se actualiza al minuto y pausa su tarea cuando la actividad deja de estar visible. No requiere un proveedor de widgets ni un servicio.
- Las notas del inicio son texto de lectura, con letra más grande, menor relleno y desplazamiento para textos largos. No son campos de entrada ni abren el teclado al tocarlas.
- **Editar escritorio** pasa a llamarse **Modo edición**. En ese modo, el botón **Editar** de la barra de cada nota abre un editor más amplio con **Guardar** y **Cancelar**.
- Al terminar la edición o navegar se limpia el foco y se oculta el teclado. Cancelar el editor no modifica la nota.
- Las notas, tamaños, posiciones, escritorios y aplicaciones asignadas a gestos mantienen sus claves de almacenamiento.

## Prueba corta en teléfono

1. Instalar sobre la versión anterior sin desinstalar. Comprobar fondo negro y letras blancas en Inicio, Apps y Ajustes.
2. Tocar y desplazar una nota en Inicio: no debe aparecer el teclado ni cambiar su contenido.
3. Entrar en Modo edición → Editar de una nota. Modificar y Guardar. Volver al inicio: se ve el texto guardado y no queda abierto el teclado.
4. Repetir y Cancelar: debe conservar el texto anterior. Cerrar/reabrir y comprobar persistencia.
5. Agregar un reloj propio en el escritorio izquierdo. Verificar hora y fecha, mover y cambiar su tamaño. Comprobar actualización al cambiar el minuto y al volver de otra app.
6. En Modo edición, desplazarse por la barra hasta Widget externo y comprobar que se mantiene el selector del sistema.
7. Repetir navegación entre escritorios y gestos arriba/abajo para detectar regresiones.

No se ha medido todavía consumo de memoria, batería ni fluidez en un dispositivo. El objetivo de esta versión es usar componentes propios para lo básico, manteniendo la opción de externos.

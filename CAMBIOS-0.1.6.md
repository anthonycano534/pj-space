# PJ Space 0.1.6 · componentes independientes y escritorio productivo

## Cambios confirmados

- **Reloj y barra separados:** cada uno es una tarjeta con posición/tamaño independientes. La actualización separa la barra antigua una sola vez, conserva su app/texto y no mueve las notas. Si la tarjeta estaba cerca de un borde, la nueva barra se mantiene dentro de pantalla y puede necesitar acomodarse para evitar superposición.
- **Alarmas:** tocar la hora o fecha en Inicio abre la interfaz de configurar alarma del dispositivo. En Modo edición abre el editor del reloj. La barra conserva su pájaro y app elegida, y no abre alarmas.
- **Barras de Android:** se solicita ocultar estado y navegación en Inicio, con revelado temporal desde el borde y estilo oscuro. Los accesos fijos y controles se apartan de las barras cuando Android informa que están visibles. Se vuelve a aplicar el modo al recuperar foco. El sistema y fabricante conservan el control final de sus barras.
- **Accesos movibles:** Añadir componente → Acceso. Iconos: avión, libro, casa, trabajo, música, estudio, compras, salud, herramientas, favoritos, teléfono y cámara. Se eligen icono/app/texto en su editor y se guardan por tarjeta.
- **Accesos inferiores fijos:** también pueden cambiar tanto el dibujo como la app en Ajustes. Conservan sus esquinas y permiten restaurar valores predeterminados.
- **Gestos cortos:** umbral vertical reducido de 64 a 24 dp después del bloqueo direccional de Android. Se mantienen sobre el fondo libre, fuera de scrolls de notas, apps y componentes, y desactivados al editar.
- **Notas:** tarjetas negras con borde blanco fino, esquinas redondeadas y espaciado interior. En Inicio siguen siendo texto, sin entrada de teclado; tamaño de letra y edición conservados.
- **Calendario propio:** vista mensual en blanco y negro con semana según idioma del teléfono, mes/año, navegación entre meses y día actual resaltado. No lee agenda ni eventos y no pide permiso de calendario.
- **Pendientes propio:** tareas con círculos para marcar/desmarcar desde Inicio y anillo de progreso con conteo. Agregar, editar y eliminar tareas queda en Modo edición. Datos locales por componente.
- **Orden de apps:** Modo edición → Orden de apps. Arrastrar desde ≡ con pulsación larga o usar flechas. Guardar aplica, Cancelar conserva lo anterior y Volver al orden alfabético restaura el predeterminado. La búsqueda respeta el orden personalizado; las apps nuevas se agregan al final.

## Uso

Pincel → **Añadir componente** ofrece Nota, Reloj, Barra, Acceso, Calendario y Pendientes. Widget externo sigue disponible como opción aparte. Las tarjetas se mueven desde ↕ y se redimensionan desde ↘; estos controles están superpuestos para no cambiar su geometría al salir de edición. Barra y Acceso se pueden eliminar desde su editor.

Las notas, tamaños, gestos y asignaciones anteriores se conservan. La migración usa identificadores estables y un marcador guardado junto con el escritorio para no recrear una barra que se eliminó después.

## Prueba corta por bloques

1. Actualizar sin desinstalar: comprobar notas y asignación/texto del pájaro. Mover barra y reloj por separado y reiniciar la app; no debe aparecer otra barra duplicada.
2. Tocar reloj en Inicio y comprobar alarmas; tocar barra y comprobar la app elegida. En edición, ambos deben abrir sus editores.
3. Añadir Acceso, elegir libro y una app; mover/redimensionar. Cambiar también icono/app de un acceso inferior y comprobarlos tras reabrir.
4. Mostrar temporalmente barras superior e inferior. Comprobar estilo, accesibilidad de las esquinas y que al ocultarse vuelvan a su lugar.
5. Probar deslizamientos arriba/abajo cortos sobre fondo. Desplazar notas/lista de apps: no debe abrirse una app por ese desplazamiento.
6. Añadir Calendario y Pendientes. Navegar meses, comprobar hoy, agregar tres tareas y marcar una/dos/todas; verificar anillo y persistencia.
7. Orden de apps: mover una al principio y otra al final, guardar, buscar y reabrir. Comprobar Cancelar y restaurar alfabético.

Compilación y pruebas de lógica no sustituyen estas pruebas interactivas en el teléfono/tablet. No se ha medido todavía consumo ni fluidez.

Referencias de plataforma: [alarmas de Android](https://developer.android.com/reference/android/provider/AlarmClock) y [barras del sistema en modo inmersivo](https://developer.android.com/develop/ui/views/layout/immersive).

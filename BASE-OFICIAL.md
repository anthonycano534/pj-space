# Base oficial: primera versión

Por decisión del usuario, PJ Space vuelve al comportamiento y la interfaz de la primera versión (0.1). El código de MainActivity se restauró desde el archivo original PJSpace-0.1-source.zip, sin agregar funciones ni corregir otros comportamientos.

La publicación de restauración es **0.1.1**, con `versionCode = 3`. Este número interno permite actualizar desde la 0.2 instalada sin desinstalar ni borrar datos. Se conserva el paquete, la firma y el almacenamiento local.

Se revierte la pantalla completa de 0.2, la ocultación de barras, el menú flotante y la pulsación larga para editar. Vuelven el encabezado y los controles de la primera versión.

La versión 0.2 queda en el historial y en su publicación para referencia. La rama main y la descarga más reciente pasan a la primera versión restaurada.

## Cómo continuaremos

Primera evolución autorizada: 0.1.2 incorpora páginas laterales (izquierda vacía, derecha Apps) y corrige Arriba/Abajo para abrir aplicaciones concretas elegidas por el usuario. Los cambios de 0.2 siguen revertidos. Ver CAMBIOS-0.1.2.md.

Segunda evolución autorizada: 0.1.3 incorpora negro/blanco, reloj y notas propios, y edición de notas únicamente en Modo edición. Por aclaración expresa del usuario, se priorizan los componentes propios y se mantiene la posibilidad de agregar widgets externos. Ver CAMBIOS-0.1.3.md.

Tercera evolución, revisada y confirmada antes de programar: 0.1.4 corrige Atrás, usa pantalla completa con pincel arriba a la izquierda y ajustes arriba a la derecha, y permite tamaños de letra por reloj/nota. No se restableció la publicación descartada 0.2. Ver CAMBIOS-0.1.4.md.

Cuarta evolución confirmada: 0.1.5 cambia el reloj a la segunda referencia, agrega su barra de pájaro fijo configurable, limita teclado de búsqueda al toque de lupa y corrige/agranda controles. Durante el trabajo el usuario autorizó acciones configurables también para teléfono/cámara fijos en las esquinas inferiores. Ver CAMBIOS-0.1.5.md.

Quinta evolución confirmada: 0.1.6 separa reloj y barra conservando sus ajustes, abre alarmas desde el reloj, ajusta barras del sistema, añade accesos con icono/app, gestos cortos, tarjetas de notas, calendario, pendientes y orden de apps. El usuario revisó la lista y autorizó programar la versión. Ver CAMBIOS-0.1.6.md.

1. Partir de esta base oficial.
2. Hacer un único cambio por vez.
3. Compilar y realizar las comprobaciones correspondientes.
4. Probar ese cambio en el dispositivo del usuario antes de darlo por validado y acumular otro cambio.

Las notas, posiciones, tamaños y gestos guardados siguen usando el mismo formato. El escritorio se adapta al espacio de la interfaz original, por lo que las coordenadas proporcionales pueden verse en otra ubicación física.

Sexta evolución confirmada: 0.1.7 incorpora los cambios de edición y organización revisados por el usuario, incluida búsqueda de apps ocultas y el logo. Ver CAMBIOS-0.1.7.md. Los escritorios previos conservan sus claves; las tareas anteriores se migran a un primer bloque sin borrar el registro original.

Séptima evolución confirmada: 0.1.8 añade edición de pendientes desde Inicio, selector del pincel, eliminación del escritorio adicional y Pomodoro con alarmas del sistema. Ver CAMBIOS-0.1.8.md. Se conservan el paquete, firma y almacenamiento anterior.

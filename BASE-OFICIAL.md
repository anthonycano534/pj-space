# Base oficial: primera versión

Por decisión del usuario, PJ Space vuelve al comportamiento y la interfaz de la primera versión (0.1). El código de MainActivity se restauró desde el archivo original PJSpace-0.1-source.zip, sin agregar funciones ni corregir otros comportamientos.

La publicación de restauración es **0.1.1**, con `versionCode = 3`. Este número interno permite actualizar desde la 0.2 instalada sin desinstalar ni borrar datos. Se conserva el paquete, la firma y el almacenamiento local.

Se revierte la pantalla completa de 0.2, la ocultación de barras, el menú flotante y la pulsación larga para editar. Vuelven el encabezado y los controles de la primera versión.

La versión 0.2 queda en el historial y en su publicación para referencia. La rama main y la descarga más reciente pasan a la primera versión restaurada.

## Cómo continuaremos

Primera evolución autorizada: 0.1.2 incorpora páginas laterales (izquierda vacía, derecha Apps) y corrige Arriba/Abajo para abrir aplicaciones concretas elegidas por el usuario. Los cambios de 0.2 siguen revertidos. Ver CAMBIOS-0.1.2.md.

1. Partir de esta base oficial.
2. Hacer un único cambio por vez.
3. Compilar y realizar las comprobaciones correspondientes.
4. Probar ese cambio en el dispositivo del usuario antes de darlo por validado y acumular otro cambio.

Las notas, posiciones, tamaños y gestos guardados siguen usando el mismo formato. El escritorio se adapta al espacio de la interfaz original, por lo que las coordenadas proporcionales pueden verse en otra ubicación física.

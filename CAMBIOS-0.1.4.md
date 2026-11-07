# PJ Space 0.1.4 · inicio completo y tipografía personalizable

Cambios confirmados por el usuario antes de programar:

- Atrás se intercepta siempre: en el principal permanece sin finalizar la actividad; desde ajustes o laterales vuelve al central; cierra edición y diálogos antes de navegar.
- Inicio a pantalla completa, sin título, lema, nombres de escritorio ni barra de navegación propia.
- Dos iconos flotantes: pincel arriba a la izquierda y engranaje arriba a la derecha. Tienen zona táctil de 48 dp y descripción accesible; no reservan espacio del escritorio.
- El pincel activa/termina Modo edición. Los controles para agregar tarjetas aparecen superpuestos abajo únicamente al editar. La geometría del escritorio es igual en Inicio y Modo edición.
- El reloj permite elegir tamaño de hora entre 24 y 240; muestra vista previa y guarda la elección por tarjeta. El tamaño inicial es mayor que antes. El tamaño efectivo se ajusta a las dimensiones reales de la tarjeta y al espacio reservado para fecha, sin el antiguo tope 64. Para mostrar números mayores también hay que darles suficiente espacio redimensionando la tarjeta.
- Las notas permiten elegir tamaño entre 12 y 64 dentro de su editor. Guardar conserva texto y tamaño por nota; Cancelar descarta ambos cambios.
- Las notas siguen siendo texto de lectura en Inicio. Los componentes propios tienen prioridad y Widget externo sigue disponible.
- Los gestos y tres escritorios de las versiones anteriores se conservan. Las barras del sistema se muestran temporalmente al deslizar desde el borde.

Las posiciones, notas y gestos se conservan. Los archivos antiguos sin fontSizeSp siguen siendo válidos y reciben tamaños por defecto. La superficie es ahora más grande y las posiciones proporcionales se adaptan a ella.

## Prueba breve

1. Instalar sin desinstalar. Ver solo pincel arriba a la izquierda y engranaje arriba a la derecha sobre el escritorio.
2. Presionar Atrás repetidamente en el central: no debe cerrar el inicio, mostrar negro ni recargarlo. Probar desde laterales, ajustes y diálogos.
3. Pincel → Tamaño del reloj → aumentar y Guardar. Agrandar/mover la tarjeta; salir de edición y comprobar que no cambia de posición.
4. Pincel → Editar en cada nota → elegir tamaños distintos → Guardar. Comprobarlos en Inicio y tras cerrar/reabrir. Probar Cancelar.
5. Tocar notas en Inicio: no debe abrir teclado. Repetir gestos y alta de un widget externo.

Esta publicación necesita validación interactiva en el dispositivo. Las pruebas unitarias no demuestran fluidez ni comportamiento completo del sistema Android.

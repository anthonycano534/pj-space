# PJ Space 0.1.5 · reloj de referencia y accesos configurables

Implementación de los cambios confirmados y la ampliación solicitada durante el trabajo:

- Reloj basado en la segunda imagen: hora grande dentro de un marco blanco; franja blanca inferior con día de semana, mes y día separados por líneas. Idioma, formato horario y tamaño personalizable conservados.
- Barra redondeada blanca debajo del reloj con el pájaro negro fijo de la referencia, texto personalizable y lupa. El icono no cambia con la app asignada.
- En Modo edición → Editar del reloj se configuran tamaño, texto de barra y app concreta a abrir. Guardar conserva todo por reloj; Cancelar no aplica el borrador. Tocar la barra en Inicio abre la app elegida, sin campo de entrada ni teclado de PJ Space. Sin app elegida, muestra una indicación para asignarla.
- La lista de apps empieza con un botón alargado de lupa y teclado cerrado. Solo al tocar ese botón aparece el campo y se solicita foco/teclado. Atrás o × cierra búsqueda, borra el filtro y oculta el teclado. Al salir de esa página se reinicia la búsqueda.
- Pincel y engranaje de mayor tamaño: iconos visibles de 34 dp y controles de 64 dp. Pincel entra en edición de forma inmediata y fija una página editable; nunca selecciona la página de Apps para editar.
- Cuatro controles fijos, por encima de las tarjetas: pincel arriba izquierda, ajustes arriba derecha, teléfono abajo izquierda y cámara abajo derecha. No participan en el motor de movimiento/redimensionado. La barra de edición aparece más arriba para no tapar los accesos inferiores.
- Teléfono y cámara tienen acciones configurables en Ajustes. Por defecto abren el marcador sin llamar y la cámara del sistema. Se puede elegir cualquier app disponible para abrir en cada uno, o restaurar su acción predeterminada; los dibujos permanecen como teléfono y cámara.
- Se conservan notas, geometría, tamaños, widgets externos, gestos y comportamiento de Atrás.

## Prueba breve en teléfono o tablet

1. Instalar como actualización sin desinstalar. Ver reloj enmarcado, banda de fecha y barra del pájaro.
2. Tocar el pincel en ambos escritorios y desde Apps: debe entrar a un escritorio editable y nunca deslizar hacia Apps. Comprobar los iconos más grandes y las cuatro posiciones fijas.
3. Modo edición → Editar reloj → elegir app y cambiar texto de barra → Guardar. En Inicio, tocar la barra: debe abrir esa app manteniendo el pájaro fijo.
4. Entrar en Apps: teclado cerrado. Tocar lupa: teclado abierto y búsqueda funcional. Atrás/× debe cerrar la búsqueda. Salir y volver: debe entrar cerrada.
5. Probar teléfono y cámara predeterminados. Luego Ajustes → accesos inferiores → elegir dos apps distintas. Comprobar cada acceso y restaurar valores predeterminados.
6. Cerrar y volver a abrir PJ Space. Comprobar acciones y texto guardados y las notas/tamaños anteriores.

Si el dispositivo no tiene cámara o marcador compatibles, la acción predeterminada informa que no está disponible; se puede asignar otra app. La prueba de interacción, teclado y fluidez requiere dispositivo real.

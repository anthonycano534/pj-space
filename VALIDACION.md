# Validación · 7 de noviembre de 2026

## Reloj de referencia y acciones fijas · 0.1.5

- Compilación `assembleDebug testDebugUnitTest`: correcta. Once pruebas unitarias aprobadas, sin fallos ni errores.
- Nuevas pruebas cubren que el pincel solo elige escritorio izquierdo o central, incluso al tocarlo en Apps, y que las acciones de teléfono/cámara tienen claves de almacenamiento independientes.
- El diseño nuevo se implementa en componentes propios y un vector local del pájaro; no requiere recursos de red ni proveedores de widgets para el reloj/barra. El pájaro no depende de la app seleccionada.
- El botón de búsqueda no contiene entrada editable. El campo con FocusRequester solo se compone tras tocar la lupa; al cerrar/salir se quita el campo, se borra el filtro y se solicita ocultar teclado. La interacción real con IME requiere prueba de dispositivo.
- El editor del reloj conserva borradores mientras se elige app; tamaño/texto/app solo se guardan al pulsar Guardar. Las asignaciones inferiores se guardan por separado y pueden volver a sus acciones predeterminadas.
- Se conservan firma, geometría, notas, tamaños, gestos y widgets externos. Los iconos fijos no se incluyen en las listas de tarjetas editables.
- Pendiente de dispositivo: parecido visual con la referencia, tacto del pincel, foco/teclado, apertura de apps por barra/esquinas, uso en tablet y fluidez. No se realizó validación visual en Android ni medición de recursos.

## Inicio completo, Atrás y tipografía · 0.1.4

- Compilación final `assembleDebug testDebugUnitTest`: correcta. Nueve pruebas aprobadas, cero fallos y cero errores.
- Nuevas pruebas cubren prioridad de diálogos/edición al volver, permanencia en el principal, navegación desde laterales/ajustes, conservación del tamaño al cambiar geometría y ajuste de reloj a ancho/alto sin el antiguo tope 64.
- El BackHandler está siempre habilitado. El caso principal no finaliza la actividad ni llama a navegación/recarga; los diálogos conservan su cierre propio.
- Pincel y engranaje son iconos nativos dibujados en código, flotantes y accesibles. El escritorio conserva exactamente las mismas dimensiones al entrar o salir de edición.
- fontSizeSp es opcional en el almacenamiento de tarjetas. Se mantienen las claves de notas, geometría y gestos de versiones anteriores.
- Hora configurada entre 24 y 240; el tamaño efectivo se ajusta a ancho y alto medidos dejando espacio separado para la fecha. Notas entre 12 y 64, independientes por tarjeta y guardadas junto con la nota al pulsar Guardar.
- Pendiente de dispositivo: comportamiento real de Atrás y barras del sistema, fluidez, posiciones con pantalla completa, reloj en tamaños extremos y accesibilidad de los controles. Las pruebas unitarias no sustituyen esta comprobación.

## Negro/blanco y Modo edición · 0.1.3

- Compilación final `assembleDebug testDebugUnitTest`: correcta. Cuatro pruebas existentes aprobadas. No se añadieron pruebas que simulen la implementación de la interfaz.
- Revisión de entrada de texto: NotePreview usa Text; el campo editable de notas solo se crea en NoteEditor, abierto desde Modo edición. Guardar persiste y Cancelar descarta el borrador. Al salir de edición se limpia foco y se solicita ocultar el teclado.
- PJSpaceTheme usa negro y blanco; el tema de actividad y barras también usa fondo negro con texto claro. Los widgets externos conservan el estilo de su proveedor.
- El reloj digital propio actualiza al minuto dentro de repeatOnLifecycle(STARTED). El host de widgets externos se inicia únicamente si hay widgets externos, o después de agregar uno.
- Se mantiene la opción Widget externo tras las opciones propias, según aclaración expresa del usuario. Se mantienen las tarjetas y notas existentes y los gestos de 0.1.2.
- Pendiente de dispositivo: apariencia, tocar notas sin teclado, editor con Guardar/Cancelar, teclado al terminar edición, reloj en ambos escritorios, selector externo y regresiones de gestos. No se midieron memoria, batería ni fluidez.

## Escritorios y apps por gesto · 0.1.2

- Compilación `assembleDebug testDebugUnitTest`: correcta. Cuatro pruebas aprobadas, sin fallos ni errores.
- Se comprueban límites de tarjetas, umbral y dirección de los gestos verticales, aislamiento de las claves de ambos escritorios y exclusión de tarjetas en la página de Apps.
- El escritorio central conserva la clave de almacenamiento original; el izquierdo tiene su propia clave y empieza sin tarjetas.
- Android gestiona el arrastre horizontal mediante HorizontalPager. El detector vertical está limitado al fondo libre de los escritorios y se desactiva durante edición. La lista de apps conserva su desplazamiento vertical normal.
- Los dos gestos guardan el componente real de la app seleccionada. Se retiraron las acciones prefijadas y las asignaciones de izquierda/derecha.
- Se mantiene la firma anterior y versionCode aumenta a 4 para instalar como actualización.
- Pendiente de dispositivo: fluidez de los desplazamientos, arbitraje de gestos diagonales, apertura de apps elegidas, widgets y persistencia tras cierre. Ver prueba corta de CAMBIOS-0.1.2.md. La compilación y las pruebas unitarias no validan estas interacciones.

## Restauración oficial 0.1.1

- MainActivity.kt y Desktop.kt coinciden exactamente (SHA-256) con los archivos de PJSpace-0.1-source.zip.
- Único cambio de compilación respecto de 0.1: versión 0.1.1, versionCode 3, para actualizar sobre 0.2.
- Compilación de restauración `assembleDebug testDebugUnitTest`: correcta. Dos pruebas aprobadas, cero errores y cero fallos.
- La primera versión fue reportada como funcional y más fluida por el usuario. La instalación de esta restauración sobre 0.2 requiere su comprobación en el teléfono.
- Se mantiene el paquete y la firma de depuración para permitir actualización conservando los datos.

## Comprobaciones iniciales del proyecto

- Compilación final: `assembleDebug testDebugUnitTest`, correcta.
- Pruebas unitarias: 2, sin fallos ni errores. Cubren límites del escritorio al mover/redimensionar y dirección dominante de los cuatro gestos.
- APK de desarrollo firmado con clave de depuración; no es una entrega de producción.
- Java 17 portátil, Gradle 8.13, AGP 8.13.2, Kotlin/compilador Compose 2.2.21; Compose BOM 2025.10.01.
- SDK instalado verificado: API 37; mínimo de la app: API 26; target: API 36.
- Hay advertencias por APIs de compatibilidad del host de widgets y la configuración de JVM. No impiden compilar.
- No se ejecutó la app: ADB no encontró dispositivos y el emulador existente no arrancó por falta del controlador de aceleración. No se instaló ese controlador ni se cambió la virtualización.
- Pendiente: prueba visual e interactiva en Android, selección HOME, widgets externos y sus tamaños/configuración, persistencia tras cierre y rotación, accesibilidad y consumo de recursos.

Los archivos se crearon en una carpeta nueva. Se preservaron los proyectos existentes. El emulador se intentó iniciar en modo de solo lectura y sin guardar instantáneas.

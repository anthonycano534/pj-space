# Validación · 7 de noviembre de 2026

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

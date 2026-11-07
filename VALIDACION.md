# Validación · 7 de noviembre de 2026

- Compilación final: `assembleDebug testDebugUnitTest`, correcta.
- Pruebas unitarias: 2, sin fallos ni errores. Cubren límites del escritorio al mover/redimensionar y dirección dominante de los cuatro gestos.
- APK de desarrollo firmado con clave de depuración; no es una entrega de producción.
- Java 17 portátil, Gradle 8.13, AGP 8.13.2, Kotlin/compilador Compose 2.2.21; Compose BOM 2025.10.01.
- SDK instalado verificado: API 37; mínimo de la app: API 26; target: API 36.
- Hay advertencias por APIs de compatibilidad del host de widgets y la configuración de JVM. No impiden compilar.
- No se ejecutó la app: ADB no encontró dispositivos y el emulador existente no arrancó por falta del controlador de aceleración. No se instaló ese controlador ni se cambió la virtualización.
- Pendiente: prueba visual e interactiva en Android, selección HOME, widgets externos y sus tamaños/configuración, persistencia tras cierre y rotación, accesibilidad y consumo de recursos.

Los archivos se crearon en una carpeta nueva. Se preservaron los proyectos existentes. El emulador se intentó iniciar en modo de solo lectura y sin guardar instantáneas.

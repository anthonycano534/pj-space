# PJ Space · primera versión oficial (restauración 0.1.1)

Estrená celular sin comprar otro. Todo lo útil al alcance de la mano.

**Prender. Resolver. Apagar.** Un launcher local, sin anuncios, analítica ni feed.

[Descargar el APK más reciente](https://github.com/anthonycano534/pj-space/releases/latest/download/PJSpace.apk) · [Versiones y cambios](https://github.com/anthonycano534/pj-space/releases)

Prototipo en desarrollo. Instalá las actualizaciones sobre la versión anterior para conservar el escritorio.

La base oficial vuelve a la primera versión (0.1), que el usuario encontró más fluida. La publicación 0.1.1 restaura su código y usa un número interno superior para poder instalarse sobre 0.2 sin desinstalar. A partir de ahora se hará y probará un cambio por vez. Ver [BASE-OFICIAL.md](BASE-OFICIAL.md). La 0.2 se conserva únicamente como historial.

## Abrir

Abrí esta carpeta como proyecto en Android Studio. Usá JDK 17 para Gradle y dejá sincronizar las dependencias. El SDK local está indicado en `local.properties` (archivo específico de esta PC, excluido de Git). Kotlin + Compose, paquete provisional `com.pjspace.launcher`, Android mínimo 8.0 / API 26.

En esta PC, la copia portátil de Java está en `../../work/jdk17/jdk-17.0.21+9` respecto de esta carpeta. Seleccionala en Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK. Java 25 de Android Studio no es compatible con esta versión de Gradle. `Build-PJSpace.ps1` selecciona automáticamente esa copia portátil; en otra PC admite `-JdkPath`.

## Usar

- Ajustes → Elegir como pantalla de inicio: Android solicita seleccionar el launcher. Para volver al anterior, usá Ajustes de Android → Aplicaciones predeterminadas → Inicio.
- Apps: búsqueda y apertura de aplicaciones del perfil actual.
- Editar escritorio: arrastrá la barra superior de una tarjeta; arrastrá ↘ para cambiar su tamaño. Listo termina la edición. Las tarjetas pueden superponerse; la última se muestra encima.
- + Nota agrega una nota independiente; + Widget abre el selector del sistema. Cada proveedor puede solicitar configuración. × elimina la tarjeta y libera su widget.
- Cuatro gestos sobre el fondo libre: configurables en Ajustes. Por defecto, arriba abre Apps. Los gestos no se activan mientras editás.
- Posiciones, tamaños, notas y gestos se guardan localmente. Las coordenadas proporcionales se adaptan al espacio disponible.

## Alcance y próximos pasos

Es un prototipo, no una versión lista para publicar. Falta validar en teléfono la selección HOME, los proveedores de widgets, rotación, teclado, accesibilidad y los gestos. El motor permite superposición; todavía no hay orden de capas editable, plantillas, accesos fijados, restauración de copias ni panel de información. Los widgets aún no respetan restricciones específicas de tamaño del proveedor. El reloj usa la hora local; las notas son texto simple. Los perfiles de trabajo y accesos especiales no están integrados.

Se eligieron versiones fijas y conservadoras de las dependencias para hacer reproducible esta primera base. No se modificó otro proyecto ni la instalación de Android Studio.

## Validación manual

1. Ejecutar la app y elegir PJ Space como inicio. Presionar Home desde otra app.
2. Buscar y abrir una aplicación; regresar a Inicio.
3. Mover y redimensionar tarjetas hasta los bordes. Crear dos notas con textos diferentes.
4. Cerrar el proceso y reabrir: comprobar notas y geometría.
5. Cambiar los cuatro gestos y probarlos sobre el fondo.
6. Agregar, configurar, redimensionar y eliminar un widget de otro proveedor; cancelar también su selector.
7. Rotar el dispositivo y repetir con teclado abierto y tamaño de fuente grande.

Las pruebas unitarias cubren límites del escritorio y selección del eje de los gestos; no reemplazan estas pruebas de dispositivo.

Referencia: [configuración oficial de Compose](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler).

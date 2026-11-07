# PJ Space · base oficial · versión 0.1.4

Estrená celular sin comprar otro. Todo lo útil al alcance de la mano.

**Prender. Resolver. Apagar.** Un launcher local, sin anuncios, analítica ni feed.

[Descargar el APK más reciente](https://github.com/anthonycano534/pj-space/releases/latest/download/PJSpace.apk) · [Versiones y cambios](https://github.com/anthonycano534/pj-space/releases)

Prototipo en desarrollo. Instalá las actualizaciones sobre la versión anterior para conservar el escritorio.

La base oficial es la primera versión (0.1), restaurada como 0.1.1. La versión 0.1.4 implementa los cambios confirmados: Atrás sin cerrar el inicio, pantalla completa con dos iconos flotantes y tamaños de texto por tarjeta. Mantiene los escritorios, gestos, blanco/negro y componentes de 0.1.2/0.1.3. Ver [CAMBIOS-0.1.4.md](CAMBIOS-0.1.4.md) y [BASE-OFICIAL.md](BASE-OFICIAL.md). La publicación 0.2 se retiró; su código sigue en el historial.

## Abrir

Abrí esta carpeta como proyecto en Android Studio. Usá JDK 17 para Gradle y dejá sincronizar las dependencias. El SDK local está indicado en `local.properties` (archivo específico de esta PC, excluido de Git). Kotlin + Compose, paquete provisional `com.pjspace.launcher`, Android mínimo 8.0 / API 26.

En esta PC, la copia portátil de Java está en `../../work/jdk17/jdk-17.0.21+9` respecto de esta carpeta. Seleccionala en Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK. Java 25 de Android Studio no es compatible con esta versión de Gradle. `Build-PJSpace.ps1` selecciona automáticamente esa copia portátil; en otra PC admite `-JdkPath`.

## Usar

- Engranaje arriba a la derecha → Ajustes → Elegir como pantalla de inicio: Android solicita seleccionar el launcher. Para volver al anterior, usá Ajustes de Android → Aplicaciones predeterminadas → Inicio.
- Tres páginas: escritorio vacío a la izquierda, escritorio original en el centro y Apps a la derecha. Arrastrá horizontalmente para navegar; Inicio vuelve al centro.
- Apps: búsqueda y apertura de aplicaciones del perfil actual.
- Pincel arriba a la izquierda: activa Modo edición. Arrastrá la barra superior de una tarjeta y ↘ para cambiar su tamaño. Listo o el pincel termina la edición. Los controles flotan y no reducen ni desplazan el escritorio.
- + Nota agrega una nota propia. Editar abre el editor de texto y tamaño de letra, con Guardar/Cancelar. En Inicio, las notas son de lectura y no abren el teclado.
- + Reloj agrega el componente digital propio con hora, día y fecha. Tamaño permite cambiar el tamaño de hora; ampliá su tarjeta para que los números tengan espacio. No necesita widgets externos. + Widget externo conserva el selector del sistema, como opción secundaria; los ya colocados se mantienen.
- Atrás cierra diálogos/edición o vuelve al principal. En el principal permanece sin cerrar el launcher. Deslizá desde el borde para mostrar temporalmente las barras del sistema.
- Arriba y abajo sobre el fondo libre abren las apps específicas elegidas en Ajustes. El selector permite buscar entre todas las apps disponibles. Izquierda y derecha quedan reservados para cambiar de página. Los gestos no se activan mientras editás.
- Cada escritorio guarda sus propias tarjetas. El izquierdo empieza vacío. Al actualizar desde una versión anterior, asigná nuevamente Arriba y Abajo a las aplicaciones que quieras.
- Posiciones, tamaños, notas y gestos se guardan localmente. Las coordenadas proporcionales se adaptan al espacio disponible.

## Alcance y próximos pasos

Es un prototipo, no una versión lista para publicar. Falta validar en teléfono la selección HOME, los proveedores de widgets, rotación, teclado, accesibilidad y los gestos. El motor permite superposición; todavía no hay orden de capas editable, plantillas, accesos fijados, restauración de copias ni panel de información. Los widgets aún no respetan restricciones específicas de tamaño del proveedor. El reloj usa la hora local; las notas son texto simple. Los perfiles de trabajo y accesos especiales no están integrados.

Se eligieron versiones fijas y conservadoras de las dependencias para hacer reproducible esta primera base. No se modificó otro proyecto ni la instalación de Android Studio.

## Validación manual

1. Ejecutar la app y elegir PJ Space como inicio. Presionar Home desde otra app.
2. Buscar y abrir una aplicación; regresar a Inicio.
3. Mover y redimensionar tarjetas hasta los bordes. Crear dos notas con textos diferentes.
4. Cerrar el proceso y reabrir: comprobar notas y geometría.
5. Asignar dos aplicaciones concretas a Arriba y Abajo y probarlas sobre el fondo. Comprobar que los gestos horizontales solo cambian de escritorio.
6. Comprobar el reloj propio y la edición de notas con Guardar/Cancelar. Agregar un widget externo y comprobar también su selector.
7. Rotar el dispositivo y repetir con teclado abierto y tamaño de fuente grande.

Las pruebas unitarias cubren límites del escritorio y selección del eje de los gestos; no reemplazan estas pruebas de dispositivo.

Referencia: [configuración oficial de Compose](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler).

# PJ Space · base oficial · versión 0.1.7

Estrená celular sin comprar otro. Todo lo útil al alcance de la mano.

**Prender. Resolver. Apagar.** Un launcher local, sin anuncios, analítica ni feed.

[Descargar el APK más reciente](https://github.com/anthonycano534/pj-space/releases/latest/download/PJSpace.apk) · [Versiones y cambios](https://github.com/anthonycano534/pj-space/releases)

Prototipo en desarrollo. Instalá las actualizaciones sobre la versión anterior para conservar el escritorio.

La base oficial es la primera versión (0.1), restaurada como 0.1.1. La versión 0.1.7 separa reloj/barra e incorpora alarmas, accesos con icono/app personalizables, calendario mensual, pendientes con progreso, gestos cortos y orden de apps. Mantiene Atrás, escritorios, blanco/negro y tamaños por tarjeta. La versión 0.1.7 incorpora edición directa de apps, ocultación/nombres, escritorio adicional, cuadrícula, copia de accesos, barra movible, formatos de notas, bloques de pendientes, días importantes y logo propio. Ver [CAMBIOS-0.1.7.md](CAMBIOS-0.1.7.md) y [BASE-OFICIAL.md](BASE-OFICIAL.md). La publicación 0.2 se retiró; su código sigue en el historial.

## Abrir

Abrí esta carpeta como proyecto en Android Studio. Usá JDK 17 para Gradle y dejá sincronizar las dependencias. El SDK local está indicado en `local.properties` (archivo específico de esta PC, excluido de Git). Kotlin + Compose, paquete provisional `com.pjspace.launcher`, Android mínimo 8.0 / API 26.

En esta PC, la copia portátil de Java está en `../../work/jdk17/jdk-17.0.21+9` respecto de esta carpeta. Seleccionala en Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK. Java 25 de Android Studio no es compatible con esta versión de Gradle. `Build-PJSpace.ps1` selecciona automáticamente esa copia portátil; en otra PC admite `-JdkPath`.

## Usar

- En Aplicaciones, engranaje arriba a la derecha → Ajustes → Elegir como pantalla de inicio: Android solicita seleccionar el launcher. Para volver al anterior, usá Ajustes de Android → Aplicaciones predeterminadas → Inicio.
- Tres páginas iniciales; Ajustes permite una cuarta a la izquierda: escritorio vacío a la izquierda, escritorio original en el centro y Apps a la derecha. Arrastrá horizontalmente para navegar; Inicio vuelve al centro.
- Apps: búsqueda y apertura de aplicaciones del perfil actual. El teclado solo aparece al tocar el botón alargado de lupa; Atrás o × cierra la búsqueda.
- En Aplicaciones, pincel arriba a la izquierda: activa Modo edición. Arrastrá la barra superior de una tarjeta y ↘ para cambiar su tamaño. Listo o el pincel termina la edición. Los controles flotan y no reducen ni desplazan el escritorio.
- Añadir componente ofrece Nota, Reloj, Barra, Acceso, Calendario y Pendientes. Widget externo sigue disponible. Mover desde ↕ y redimensionar desde ↘; Editar abre las opciones del componente.
- Las notas tienen borde blanco y esquinas redondeadas. Su editor guarda texto, tamaño, alineación y tipografía; en Inicio son de lectura y no abren teclado.
- El reloj abre alarmas al tocarlo en Inicio. La barra del pájaro es independiente y guarda su propio texto/app. Se pueden mover por separado; la actualización migra la barra anterior sin duplicarla.
- Los accesos movibles y las dos esquinas inferiores permiten elegir icono y app. Las esquinas siguen fijas y se configuran en Ajustes. Sus valores predeterminados son marcador y cámara.
- Calendario muestra el mes, permite navegar y marca hoy. Pendientes guarda tareas y progreso: marcar en Inicio; agregar/editar/eliminar en Modo edición.
- Aplicaciones → pincel: arrastrar filas directamente y tocar Listo para guardar. Mantener presionada una app fuera de edición permite ocultarla, renombrarla, abrir información o solicitar su desinstalación. La búsqueda respeta el orden y las apps nuevas se agregan al final del personalizado.
- Atrás cierra diálogos/edición o vuelve al principal. En el principal permanece sin cerrar el launcher. Deslizá desde el borde para mostrar temporalmente las barras del sistema.
- Arriba y abajo sobre el fondo libre abren las apps elegidas en Ajustes con un recorrido corto. No actúan sobre los scrolls de los componentes. Izquierda y derecha cambian de página. Los gestos no se activan mientras editás.
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


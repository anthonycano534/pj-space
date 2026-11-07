# PJ Space 0.1.2 · escritorios y aplicaciones por gesto

Primer cambio funcional sobre la base oficial restaurada 0.1.1. No se recuperan los cambios de pantalla completa de la versión 0.2.

## Requerimiento actualizado

- El escritorio principal queda en el centro y conserva las tarjetas existentes.
- A su izquierda hay un escritorio que empieza vacío. Se le pueden agregar notas, reloj y widgets desde Editar escritorio. Su contenido se guarda por separado.
- A la derecha está la lista de aplicaciones instaladas que se pueden abrir en el perfil actual, con búsqueda.
- Arrastrar hacia la izquierda muestra la página de la derecha (Apps); arrastrar hacia la derecha muestra la página izquierda. Es navegación espacial normal entre páginas, sin ejecutar una aplicación.
- Los gestos arriba y abajo, sobre el fondo libre de los escritorios, abren una aplicación concreta elegida en Ajustes. El selector incluye todas las actividades de inicio que Android permite abrir en el perfil actual, no una lista fija de acciones.
- El selector tiene búsqueda, cancelación y Sin asignar. Guarda el componente específico de la app, también si hay varias con nombres iguales.
- Los gestos se bloquean durante la edición para no confundirlos con mover/redimensionar una tarjeta. Desplazar la lista de apps no ejecuta el gesto vertical.
- Inicio de Android o el botón Inicio de PJ Space vuelve al escritorio central.

Las asignaciones antiguas de acciones (como Aplicaciones o Cámara) ya no se usan. Al actualizar, elegí las aplicaciones de arriba y abajo nuevamente en Ajustes. Las notas y geometría del escritorio original se conservan en su clave de almacenamiento anterior.

## Prueba corta en teléfono

1. Instalar sobre 0.1.1 sin desinstalar y comprobar notas/tarjetas centrales.
2. Arrastrar hacia la izquierda: ver Apps; buscar y abrir una app. Inicio vuelve al centro.
3. Arrastrar hacia la derecha: ver el escritorio izquierdo vacío. Agregar y escribir una nota; volver al centro y comprobar que no se mezclan contenidos.
4. Ajustes → Arriba: buscar y elegir una app cualquiera. Repetir Abajo con una app distinta. Probar ambos sobre una zona libre del escritorio.
5. Probar gestos horizontales con apps verticales asignadas: deben cambiar de página sin abrirlas.
6. Editar, mover y redimensionar una tarjeta: no debe cambiar de página ni abrir apps.
7. Cerrar el proceso/reabrir: ambos escritorios y las dos asignaciones deben seguir guardados.

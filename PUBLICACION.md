# Publicación en GitHub

Repositorio: `anthonycano534/pj-space`, público.

## Publicación principal actual

1. Subir el código fuente de esta carpeta respetando `.gitignore`.
2. Crear la versión `v0.1.1` con las notas de `BASE-OFICIAL.md` (restauración de 0.1).
3. Adjuntar el APK de restauración con el nombre **PJSpace.apk**.
4. Marcarla como la versión más reciente.

El enlace de descarga es `https://github.com/anthonycano534/pj-space/releases/latest/download/PJSpace.apk`.

## Siguientes versiones

Incrementar `versionCode` y `versionName`, compilar y comprobar el APK, y publicar una nueva versión con el mismo nombre de archivo adjunto. Así el enlace anterior continúa apuntando al APK más reciente.

La restauración 0.1.1 usa versionCode 3 para actualizar desde 0.2 (versionCode 2). Debe marcarse explícitamente como Latest aunque su etiqueta sea menor que 0.2.0. La publicación anterior no se elimina.

Mantener la misma clave de firma permite instalar las actualizaciones sin desinstalar ni perder datos. La clave de depuración usada por este prototipo permanece fuera del repositorio; no se debe subir ningún archivo de claves ni credenciales. Para una futura distribución de producción se definirá una clave de lanzamiento y su copia de seguridad.

No generar APK con una clave de depuración distinta en cada ejecución de GitHub Actions: Android impediría actualizar sobre el APK instalado. Por ahora se publican las compilaciones locales verificadas, firmadas con la misma clave.

Referencia: [enlaces a la última versión y sus descargas](https://docs.github.com/en/repositories/releasing-projects-on-github/linking-to-releases).

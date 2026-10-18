# Análisis técnico de Geode Launcher para Android

## Artefacto analizado

- Archivo aportado para análisis: `Geode_skybound.apk`
- Nombre/versión visibles en recursos y cadenas: Geode Launcher **1.8.0**
- Paquete Java observado en cadenas y nombres de componentes: `com.geode.launcher`
- ABI incluida: `arm64-v8a` y `armeabi-v7a`
- Biblioteca nativa del launcher: `liblauncherfix.so`
- La APK analizada **no contiene** `libcocos2dcpp.so` ni `Geode.so`.

Este documento describe el artefacto subido; no certifica compatibilidad con cualquier GDPS ni con Geometry Dash 2.2081/2.2090.

## Evidencias encontradas

### 1. Es un launcher dedicado, no solo un overlay

Las cadenas del DEX incluyen componentes como:

- `com.geode.launcher.MainActivity`
- `com.geode.launcher.GeometryDashActivity`
- `com.geode.launcher.InstallReceiver`
- `com.geode.launcher.UserDirectoryProvider`
- pantallas de configuración, registros y logs de Geode.

También aparecen rutas de datos de mods y diagnóstico:

- `game/geode/mods`
- `game/geode/logs/`
- `game/geode/crashlogs/`
- `game/geode/resources/geode.loader/`
- `game_version.txt`

Esto apunta a una aplicación que gestiona el entorno y los datos del juego, con actividades propias para iniciar el juego y gestionar Geode.

### 2. El loader y el juego son piezas separadas

La APK no empaqueta la biblioteca del juego `libcocos2dcpp.so`, ni una biblioteca `Geode.so` lista para cargar. En cambio, contiene referencias a:

- `https://api.geode-sdk.org/v1/loader/versions/latest`
- releases del repositorio `geode-sdk/geode`
- releases del repositorio `geode-sdk/android-launcher`
- nombres como `LoaderPayload`, `LoaderVersion`, `Geode.so` y `resources.zip`.

La lectura más probable es que el launcher obtiene/actualiza el payload de Geode por separado y lo prepara para el entorno del juego. La APK del launcher por sí sola no demuestra que el loader pueda funcionar sin una instalación compatible del juego.

### 3. Hay código nativo que prepara/parchea la biblioteca del juego

La APK incluye `lib/arm64-v8a/liblauncherfix.so` y su equivalente `armeabi-v7a`. Los símbolos JNI exportados incluyen:

- `Java_com_geode_launcher_LauncherFix_setDataPath`
- `Java_com_geode_launcher_LauncherFix_setOriginalDataPath`
- `Java_com_geode_launcher_LauncherFix_enableExceptionsRenaming`
- `Java_com_geode_launcher_LauncherFix_performPatches`

Las cadenas de la biblioteca incluyen mensajes sobre localizar la base de `libcocos2dcpp.so`, analizar su sección dinámica y parchear símbolos/relocaciones. Esto es evidencia de un componente nativo de compatibilidad que espera trabajar con la biblioteca del juego cargada; no es equivalente a dibujar un panel Android encima del juego.

No se ha reconstruido ni auditado completamente el código nativo, así que no se deben inferir todos sus detalles internos solo a partir de las cadenas.

### 4. Permisos y gestión del sistema de archivos

El manifiesto contiene permisos relacionados con Internet, acceso a archivos/medios e instalación de paquetes, además de componentes Android propios. Esto concuerda con funciones de descarga, actualización, selección de archivos y preparación de datos. Los permisos por sí solos no conceden acceso arbitrario al proceso de otra app: la integración debe ocurrir mediante el mecanismo de lanzamiento/entorno previsto por el launcher.

## Por qué GPTM actual no hace lo mismo

La implementación actual de GPTM en este repositorio tiene una interfaz y un servicio de overlay, pero sus controles son demostrativos. Abrir Geometry Dash con un intent solo inicia el juego; una ventana superpuesta no carga código nativo dentro de él ni conecta las opciones del menú con el motor del juego.

Por tanto, no basta con añadir más botones o cambiar el diseño. Hace falta una integración real entre el launcher, los archivos de juego permitidos, el payload del loader y una biblioteca nativa compatible.

## Arquitectura que debemos implementar en GPTM

1. **Diagnóstico de instalación**: detectar el paquete instalado, versión real, ABI, ubicación accesible y disponibilidad de las bibliotecas necesarias. No asumir que el nombre del APK equivale a la versión del juego.
2. **Compatibilidad explícita**: registrar por separado los resultados comprobados para cada versión/build y ABI. 2.2081 y 2.2090 son objetivos, no compatibilidad confirmada.
3. **Gestión del loader**: obtener solo builds oficiales y compatibles del payload, verificar versión e integridad, y mostrar errores legibles si no coincide.
4. **Preparación del entorno**: usar un mecanismo de integración reproducible y compatible con Android, no un overlay como sustituto de la carga del loader.
5. **Arranque y diagnóstico**: registrar pasos de inicialización, fallos de carga y logs, con modo seguro y recuperación.
6. **Menú funcional**: conectar cada opción a una API real del loader/mod correspondiente; no mostrar interruptores como funcionales si solo cambian su estado visual.

## Próximos pasos

- Añadir primero una pantalla de diagnóstico a GPTM.
- Determinar qué paquete y build de Geometry Dash/GDPS se han instalado realmente y qué bibliotecas ofrece.
- Estudiar la interfaz pública y el flujo de lanzamiento de Geode, respetando su licencia y sin copiar código propietario de otros mods.
- Implementar un prototipo mínimo de integración y probarlo en un entorno de test antes de prometer soporte para 2.2081 o 2.2090.

## Limitaciones del análisis

Este fue un análisis estático de la APK (contenido ZIP, cadenas del DEX, manifiesto en forma de cadenas y símbolos/cadenas ELF). No se ejecutó en un dispositivo, no se descompiló por completo el bytecode y no se verificó la instalación ni el funcionamiento de un GDPS concreto.
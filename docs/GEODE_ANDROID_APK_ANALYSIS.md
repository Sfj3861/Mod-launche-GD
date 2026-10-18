# Geode Android APK analysis (reference build)

## Sample inspected

- File supplied for analysis: `Geode_skybound.apk`
- APK size: about 5.4 MiB on disk; ZIP contents total about 8.0 MB uncompressed.
- The package contains `classes.dex` and `classes2.dex`, Android resources, and native libraries for `arm64-v8a` and `armeabi-v7a`.
- The package includes `lib/arm64-v8a/liblauncherfix.so` and `lib/armeabi-v7a/liblauncherfix.so`.
- It does **not** contain `libcocos2dcpp.so` in its own native-library directory, and does not contain a bundled game APK in the inspected ZIP listing.
- DEX strings identify package namespace `com.geode.launcher`, activities including `MainActivity` and `GeometryDashActivity`, the `LauncherFix` JNI bridge, profile/log/mod paths, and a `--geode:safe-mode` launch argument.
- The native library exports JNI entry points `Java_com_geode_launcher_LauncherFix_setDataPath`, `setOriginalDataPath`, `enableExceptionsRenaming`, and `performPatches`. Its strings reference `libcocos2dcpp.so`, relocation patching and memory protection changes.

## What this indicates

Geode Android is not merely an overlay menu that opens Geometry Dash. It has a dedicated launcher/application layer and a native bridge used to prepare or patch the game's native runtime. The launcher manages game/profile state, safe mode, logs, mods and resources, and its native helper contains logic intended to interact with the game's loaded native library. The app package itself is not the Geometry Dash engine: the sample's ZIP contents do not include the game's `libcocos2dcpp.so`.

The likely high-level lifecycle is:

1. The Android launcher discovers or prepares a compatible Geometry Dash installation/profile.
2. It configures the selected profile's data paths and launch options.
3. The game activity starts with Geode-specific launch state (including a safe-mode option).
4. The native bridge performs runtime patch preparation against the game process/library.
5. Geode's native loader and mods must be available in the profile's expected locations for actual in-game functionality.

This is a structural inference from the APK's contents and symbol/string evidence, not a complete decompilation or runtime trace. The precise profile-install/update sequence, the exact injection/loading mechanism, and which code runs at each stage still need verification from source or instrumentation.

## Why a simple GPTM overlay does not work like Geode

- Android's overlay permission only lets an app draw over another app; it does not load native code into the game's process.
- A button or toggle in GPTM can change its own UI state, but it cannot change Geometry Dash gameplay unless a game-side/native integration consumes that setting.
- Launching `com.robtopx.geometryjump` alone only opens the game. It does not install a loader, patch the native runtime, register mods or connect mod APIs.
- A `.nova`/`.geode` archive is only a container. It becomes functional only when a compatible loader discovers it, validates its manifest and loads a payload for the exact game build and ABI.

## Implications for GPTM / NOVA

1. Separate the project into an Android launcher/profile manager, a native bootstrap layer, a loader/runtime, and individual mod packages.
2. Add a diagnostic screen that reports installed package, game version/build, ABI, presence of `libcocos2dcpp.so`, profile paths, loader state and logs. Do not silently claim a mod is active based on a UI toggle.
3. Define an explicit compatibility profile per game build (including 2.2081 and 2.2090); detect the actual build rather than guessing from APK filename/version labels.
4. Keep safe mode and failure recovery from the beginning so a bad native module does not create a launch loop.
5. Implement and test one minimal native initialization feature before adding gameplay features or declaring `.nova` runnable.
6. Do not copy proprietary Geode binaries or source into GPTM. Use the public Geode Android source/documentation to understand the architecture, and implement GPTM's own code and package format.

## Confidence / limitations

High confidence: package structure, listed native libraries, JNI symbol names and relevant strings are present in the supplied APK. Moderate confidence: high-level architectural interpretation. Not established from static ZIP inspection alone: exact runtime call order, the complete mod-loading algorithm, support for the user's particular GDPS build, or compatibility with GD 2.2081/2.2090. No APK was executed, and no successful build or device test is claimed.
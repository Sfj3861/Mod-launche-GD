# GPTM — GPT Mod Menu (Android)

A standalone Android launcher prototype for Geometry Dash, featuring a movable GPTM overlay and a categorized menu.

## Project status

- Android Kotlin project in `launcher/`.
- Floating GPTM bubble and menu panel.
- Automated debug APK build using GitHub Actions.
- Planned categories for player/practice, visuals/HUD, editor/build, level/QoL and diagnostics.

**Important:** this is currently an interface prototype. The menu switches do not modify Geometry Dash. No native injection or game patching is implemented, and compatibility with Geometry Dash 2.2090 has not been verified. Android's overlay permission does not grant access to another app's process.

See [launcher build instructions](launcher/README.md) and the [GPTM roadmap](docs/GPTM_ROADMAP.md).

## Build the APK

1. Open the **Actions** tab.
2. Run **Build GPTM Android APK**.
3. After a successful run, download the `gptm-android-debug-apk` artifact.

A successful build confirms compilation only; it does not prove that the app works with your installed game.

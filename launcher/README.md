# GPTM Android Launcher

Standalone Android launcher prototype for GPTM — GPT Mod Menu, with a movable floating menu for Geometry Dash.

## Current functionality

- Android launcher screen with a button to open Geometry Dash.
- User-controlled Android Display over other apps permission.
- Movable GPTM bubble and categorized overlay panel.
- Planned categories for practice/player, visuals/HUD, editor/build, level/QoL and diagnostics.
- GitHub Actions workflow to build a debug APK artifact.

## Important limitations

This is currently an interface prototype. The toggles only change their displayed state and show a notice; they do not modify Geometry Dash gameplay. This app does not inject native code, patch the game, or generate in-game trigger objects. Android's overlay permission only permits drawing a window above other apps; it does not grant access to another app's process.

The launcher tries to open package com.robtopx.geometryjump. If your installed game uses another package ID, the launch button will report that it cannot find it. Geometry Dash 2.2090 compatibility and native integration have not been verified.

## Build

Open the repository's Actions tab and run Build GPTM Android APK. Download the gptm-android-debug-apk artifact from a successful workflow run. A successful build confirms compilation only; install and test on your own device before relying on it.

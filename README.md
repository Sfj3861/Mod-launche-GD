# GPTM — GPT Mod Menu for Geometry Dash Android

GPTM is intended to become a **Geode-style mod loader** for an already-installed copy of Geometry Dash, not just a shortcut launcher.

## Required end state

- Require Geometry Dash to be installed on the device; GPTM does not bundle the game's copyrighted APK or assets.
- Target both Geometry Dash Android **2.2081** and **2.209**. Keep separate compatibility profiles and verify the exact installed 2.209 build (for example, 2.2090/2.2091 identifiers if present on the device).
- Provide a GPTM mod menu and a real loading/integration layer. A floating overlay alone is not a mod loader.
- Preserve the user's original game install and clearly report unsupported versions or ABIs rather than claiming compatibility.

## Current status

The current Kotlin app now includes an initial read-only installation diagnostic for Profile A: it reports the detected package, Android versionName/versionCode, device ABI, visible native-library path and whether `libcocos2dcpp.so` is visible there. It tries known Geometry Dash package IDs and visible launcher labels for GDPS builds. This is a diagnostic foundation only: exact 2.2081 identification may remain unknown when Android metadata is generic, and it does not load native mods. Menu switches still do not modify Geometry Dash. **The requested Geode-style integration is not implemented yet.** The 2.2081 and 2.209 targets remain requirements, not verified compatibility claims.

## Architecture direction

A normal Android app cannot simply embed another app's Activity or gain access to its process through overlay permission. To behave like a mod loader, GPTM needs a verified integration/bootstrap design for the installed game, native libraries and each supported game build. That design must account for Android package/signature rules, ABI, game library symbols, and whether a supported extension point exists. Do not claim that a clone-like in-app experience is possible just by adding a WebView or floating window.

Implementation should proceed in verifiable stages:

1. Detect whether Geometry Dash is installed and identify its actual version, package, ABI and native library build.
2. Build and test separate compatibility profiles for 2.2081 and 2.209; detect the exact build identifier and report unknown builds as unsupported.
3. Establish a documented, testable loading/bootstrap mechanism before wiring any menu toggles to game behavior.
4. Implement the GPTM menu as a front end to verified mod APIs, with capability checks.
5. Build through GitHub Actions and test on-device independently on both target versions. A successful APK build is not proof of in-game compatibility.

## Build the current prototype

Open **Actions → Build GPTM Android APK** and run the workflow. Download `gptm-android-debug-apk` after a successful run. This artifact is only the current launcher/UI prototype until native loading is implemented.

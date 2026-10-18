# GPTM — GPT Mod Menu: technical roadmap

## Product goal

GPTM should work as a Geode-style loader for Geometry Dash Android, requiring the game to be installed. The intended targets are **2.2081 and 2.209**. For the 2.209 target, detect and record the exact installed build identifier (such as 2.2090/2.2091 if applicable); do not assume these builds are interchangeable. It must not bundle copyrighted game assets or pretend that an overlay is equivalent to native mod loading.

## Current reality

The Kotlin app currently contains a launcher screen and a floating overlay panel. The panel is UI only; toggles are not connected to Geometry Dash. No native loader, hook system, or in-app hosting mechanism has been established. Both game versions are target requirements, not tested compatibility.

## Why a normal launcher is insufficient

Android isolates applications into separate processes. A normal application cannot host another application's Activity inside its own Activity or access the game's process merely by requesting `SYSTEM_ALERT_WINDOW`. A real loader requires a supported integration/bootstrap strategy and must be validated against the game's actual package, version, ABI, native libraries and symbols. Android package/signature restrictions and distribution method also affect the design.

## Staged implementation

1. **Installation/version detector**
   - Detect whether Geometry Dash is installed.
   - Identify the installed package and available native ABI/library information.
   - Distinguish 2.2081 from 2.209 using reliable in-app/build information, not the downloaded APK filename; retain the exact 2.209 build identifier.
   - Show a clear unsupported state for other or unknown builds.

2. **Compatibility adapters**
   - Keep separate compatibility profiles for 2.2081 and 2.209, and track any 2.209 build-specific differences.
   - Verify each against an actual installed build and device architecture before enabling features.
   - Never infer compatibility merely because the app compiles.

3. **Loader/bootstrap**
   - Select and document a viable, testable integration method before writing native hooks.
   - Keep the original game install and user levels protected; don't silently overwrite or modify them.
   - Do not represent an overlay, WebView, or external launch intent as native loading.

4. **GPTM menu and mod API**
   - Connect menu entries only to implemented and tested APIs.
   - Display unsupported/unavailable states for features without an adapter.
   - Keep diagnostics and feature status visible.

5. **Verification**
   - Build with GitHub Actions.
   - Test installation, launch, loader initialization and basic gameplay independently on 2.2081 and the exact 2.209 build(s) targeted.
   - Publish a compatibility matrix listing Android version, ABI, game build and features actually tested.

## Acceptance criteria

- Geometry Dash must already be installed.
- GPTM must detect unsupported or missing game installations cleanly.
- Both target game versions must be individually tested before being described as supported.
- The game must actually initialize through the loader and expose a functioning mod integration API; a floating menu by itself does not meet the goal.
- Do not bundle the original game's APK/assets or copy proprietary code from other mod menus.

# GPTM.nova

This folder contains the source files packaged into the custom .nova archive.

**Status: scaffold only.** This package is not executable inside Geometry Dash. It requires a compatible NOVA native runtime and a real game-side payload. The APK-based Kotlin launcher is not itself a game mod and is not included as an executable payload.

The Android launcher now includes a package intake validator. It can import a .nova archive, inspect its manifest and ZIP structure, report basic ABI/profile compatibility, and store accepted packages in its private library. Validation does not mean the package can run in-game.

- `manifest.json`: package metadata and target game profiles.
- `payload/README.md`: status of the future runtime payload.

Do not rename this package to `.geode`; .nova is a separate proposed format.

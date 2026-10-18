# GPTM runtime payload — not implemented

This folder is the declared entry point for the future GPTM game-side module.

The previous Kotlin overlay is Android application code and cannot run as a Geometry Dash in-process mod by being copied into this package. A real payload must be implemented against a documented NOVE loader API and built for the target Android ABI/game build.

Until that exists:
- no code is injected into Geometry Dash;
- no gameplay, editor, botting or visual feature is active;
- no NONG is downloaded or installed automatically;
- the package must be treated as non-runnable by installers.

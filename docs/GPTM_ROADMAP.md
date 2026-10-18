# GPTM Official — .nova mod roadmap

## Product goal

GPTM is transitioning from a standalone Android APK to a custom **.nova installable-mod package**. The release workflow emits `GPTM.nova`, not an APK. The intended first game target is Geometry Dash Android 2.2081 (Profile A), with 2.209 treated as a separate profile.

## Important technical boundary

A file extension does not make code executable inside Geometry Dash. A .nova package needs a compatible loader that recognizes its manifest, validates the package and loads its game-side payload. The previous Kotlin overlay is Android application code, not a native Geometry Dash plugin, so it cannot simply be renamed to .nova.

## Current status

- The package manifest and packaging workflow use the `nova` format identifier and produce a `.nova` archive.
- The old APK build is not part of the release workflow.
- The existing Kotlin overlay is retained as reference source only.
- Native game-side payload, NOVA loader integration, hooks and tested mod API are not implemented.
- Botting, editor helpers, gameplay mods and automatic NONG management are not functional yet.

## .nova package contract

- Container: ZIP archive with the `.nova` extension.
- Required manifest: `manifest.json` at archive root.
- Manifest includes format identifier, schema version, stable mod ID, display name, version, target game profiles, entry point and implementation status.
- A loader must reject unknown format versions, unsupported game builds, invalid paths and incomplete payloads.
- The package must never claim to be runnable unless a compatible loader and valid payload are present.

## Implementation phases

1. Package format: validate manifest fields and generate a reproducible .nova archive.
2. Loader contract: define the NOVA loader API, lifecycle, permissions, dependency resolution and error reporting.
3. Native payload: implement a minimal game-side module for the selected Android ABI and exact Geometry Dash build.
4. First integration: load one low-risk feature and verify it inside the game on a real device.
5. Editor and botting: add features only through tested game interfaces, with backups for level edits.
6. NONG management: validate Song File Hub's supported interface and a safe Android storage path before enabling downloads/imports.
7. Compatibility expansion: validate 2.2081 and 2.209 independently.

## Acceptance criteria

- The .nova file is produced and its manifest validates.
- A compatible NOVA loader can discover and validate GPTM.
- A real payload initializes inside the selected Geometry Dash build.
- At least one advertised feature is tested in-game.
- Unsupported game builds fail safely; original game and user-level data remain protected.

A successful archive build proves packaging only, not that GPTM is installable or functional in-game.

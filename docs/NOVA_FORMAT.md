# NOVA package format — draft v1

## Purpose

A `.nova` file is a ZIP archive with a root `manifest.json`. It is GPTM's proposed custom mod format, not an APK and not a Geode `.geode` package.

## Required manifest fields

- `format`: exactly `nova`.
- `format_version`: supported schema version (currently 1).
- `id`: stable reverse-domain-style mod identifier.
- `name`, `version`, and `author`.
- `target`: game, versions/profiles and supported ABIs.
- `entrypoint`: an existing, safe path inside the archive.
- `runtime`: required loader runtime identifier.
- `status`: implementation status. `package-scaffold-only` is metadata only and must never be executed.

## Implemented in NOVA Loader v0

The Android launcher now has an **Import / Validate .NOVA** flow. The initial validator:

- Imports a selected archive into the app's private `files/nova-mods` library.
- Requires a root `manifest.json` with format `nova` and schema version 1.
- Checks required metadata, ID syntax, declared game, entrypoint existence and safe archive paths.
- Rejects duplicate ZIP entry names, path traversal, absolute paths, archives over 32 MiB, expanded content over 64 MiB, more than 2,048 entries, and manifests over 256 KiB.
- Reports target ABI and profile compatibility against the detected device/game information.
- Does not extract arbitrary files and does not execute package payloads.

## Important limitations

This is a package intake/validation layer, **not yet a native in-game loader**. It does not yet verify duplicate JSON keys, declared cryptographic hashes, dependency graphs, permission grants, signatures, or a payload's ABI at the binary level. Profile detection is best-effort because Android `versionName` / `versionCode` may not identify the exact in-game build.

The Geode Android APK analysis suggests separating the product into an Android launcher/profile manager, native bootstrap, in-game loader/runtime, and mod packages. The observed Geode native bridge is evidence about architecture only; GPTM does not reuse its binaries or assume its loading mechanism can be copied directly.

## Runtime status

A `.nova` extension does not make Kotlin app code execute inside Geometry Dash. A compatible NOVA runtime, native/game-side payload, safe-mode recovery, and a tested mod API must be implemented and verified on real devices before any package can be described as runnable. At present, the validator deliberately reports packages as **not executable**.

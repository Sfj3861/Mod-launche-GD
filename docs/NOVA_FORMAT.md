# NOVA package format — draft v1

## Purpose

A `.nova` file is a ZIP archive with a root `manifest.json`. It is GPTM's proposed custom mod format, not an APK and not a Geode `.geode` package.

## Required manifest fields

- `format`: exactly `nova`.
- `format_version`: supported schema version (currently 1).
- `id`: stable reverse-domain mod identifier.
- `name`, `version`, and `author`.
- `target`: game, versions/profiles and supported ABIs.
- `entrypoint`: path within the archive.
- `runtime`: required loader runtime identifier.
- `status`: implementation status. `package-scaffold-only` must never be executed.

## Installer requirements

A future NOVA loader must reject path traversal, absolute paths, duplicate manifest keys, unsupported schema versions, oversized archives, missing entry points, unsupported game builds and unsupported ABIs. It should verify declared hashes, enforce declared permissions, and preserve backups/rollback.

## Runtime status

The current GPTM package is metadata plus documentation only. A .nova extension cannot make Kotlin app code execute inside Geometry Dash. A compatible loader, a defined runtime API, and a native/game-side payload must be implemented and tested on a real device before GPTM can be described as an installable, runnable mod.

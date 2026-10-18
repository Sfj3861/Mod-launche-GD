# NOVA Loader / GPTM — Geometry Dash Android

This repository is developing a custom **NOVA mod package format and Android loader** for an already-installed Geometry Dash build. The supplied Geode Android APK was inspected to understand its separation between launcher, native bridge, loader/runtime, and mods. GPTM does not reuse Geode's proprietary binaries.

## What is implemented now

- Android launcher with read-only game installation diagnostics.
- **Import / Validate .NOVA** file picker.
- Private package library at the app's internal `files/nova-mods` directory.
- ZIP/manifest checks, including safe paths, duplicate ZIP entries, archive-size limits, entrypoint existence, schema version, declared game, and basic ABI/profile compatibility reporting.
- GitHub Actions jobs to package `GPTM.nova` and compile the Android debug APK.

## What is not implemented yet

- A native bootstrap that initializes inside Geometry Dash.
- A game-side NOVA runtime or stable mod API.
- Actual loading/execution of .nova payloads.
- Verified support for Geometry Dash Android 2.2081 or 2.2090.
- Working gameplay features controlled by the overlay.

**Important:** the current NOVA validator deliberately does not execute package payloads. A package being accepted into the library does not mean it can run inside Geometry Dash. The Geode APK findings are architectural evidence, not proof that its exact loader mechanism can be reused.

## Build and download

Open [GitHub Actions](https://github.com/Sfj3861/Mod-launche-GD/actions). The workflow **Build NOVA package and Android loader** produces:

- `GPTM-nova-mod`: a `.nova` archive containing the current metadata/documentation scaffold.
- `nova-loader-android-debug`: the Android debug APK with the NOVA package validator.

Build success proves compilation and packaging only. It does not prove that a native mod loads inside Geometry Dash.

## Technical notes

- [Geode Android APK analysis](docs/GEODE_ANDROID_APK_ANALYSIS.md)
- [NOVA package format and current validator](docs/NOVA_FORMAT.md)
- [GPTM / NOVA roadmap](docs/GPTM_ROADMAP.md)

Geometry Dash's original APK and assets are not bundled. Target profiles for 2.2081 and 2.2090 must be verified independently on real devices before compatibility is claimed.

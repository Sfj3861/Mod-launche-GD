# GPTM Official — technical roadmap

## Product goal

Build a real mod-loader integration for Geometry Dash Android, targeting **2.2081 first (Profile A)** and **2.209 as a separate profile**. Exact installed build, ABI and native symbols must be identified before enabling either profile. A floating overlay is not a loader.

## Current status — 1.0.0-dev.1

- The launcher has a game installation/version diagnostics screen.
- The floating menu now has Player, Visuals, Editor and System tabs.
- Gameplay toggles were removed from the UI; features show explicit implementation states instead.
- The app still has **no native loader/bootstrap, no native hooks and no mod API**. Native gameplay features are not available. This build must not be advertised as a working mod menu yet.

## Architecture constraints

Android isolates app processes. `SYSTEM_ALERT_WINDOW` only allows a window above another app; it does not grant access to Geometry Dash's process or memory. A real loader requires a supported bootstrap/integration strategy and must be validated against the actual game package, exact version, ABI, native libraries and symbols. Package/signature restrictions and the distribution method affect what is possible. Do not silently overwrite the original game installation or bundle copyrighted game assets.

## Official implementation phases

### Phase 1 — Product structure

- Keep the menu organized into Player, Visuals, Editor and System.
- Never show a gameplay toggle as functional unless connected to a tested implementation.
- Keep compatibility and initialization states visible.

### Phase 2 — Profile A discovery

- Identify the installed package and exact build using reliable evidence, not only `versionName` or the APK filename.
- Determine device ABI and inspect accessible native library metadata.
- Document what can and cannot be read under Android package isolation.
- Stop with an explicit unsupported state if build identity cannot be confirmed.

### Phase 3 — Loader/bootstrap feasibility

- Choose and document a legitimate, testable integration/bootstrap method before implementing hooks.
- Study supported public loader interfaces and the existing static analysis notes; do not copy proprietary code from another launcher.
- Keep original user levels and the original install protected.

### Phase 4 — First real integration

- Implement a minimal native initialization handshake for the selected build.
- Connect one small, low-risk feature to a real API and expose its status in the menu.
- Test launch, initialization, game stability and feature behavior on-device before adding more features.

### Phase 5 — Expand and validate

- Add Player, Visuals and Editor features only through implemented APIs.
- Develop 2.209 as a separate compatibility adapter after recording its exact build identifier.
- Publish a compatibility matrix containing Android version, ABI, exact game build and individually tested features.

## Acceptance criteria

- Missing and unsupported game installations are handled cleanly.
- The loader initializes inside the selected game build, not merely beside it.
- At least one feature is shown working in actual gameplay before GPTM is described as a functional mod menu.
- 2.2081 and 2.209 are tested independently; compiling the APK alone is not evidence of compatibility.

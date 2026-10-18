# GPTM Official — technical roadmap

## Product goal

Build a real mod-loader integration for Geometry Dash Android, targeting **2.2081 first (Profile A)** and **2.209 as a separate profile**. Exact installed build, ABI and native symbols must be identified before enabling either profile. A floating overlay is not a loader.

## Current status — 1.0.0-dev.1

- Launcher diagnostics and movable overlay exist.
- Floating menu tabs: Player, Visuals, Botting, Editor, Mods, Music and System.
- English/Spanish selection for the floating menu.
- Botting, editor and mod catalogues list planned features with explicit pending/not-installed states; they do not yet modify gameplay.
- Music tab opens Song File Hub in the browser.
- There is **no native loader/bootstrap, no native hooks and no mod API** yet. Automatic NONG download/import and in-game song mapping are not implemented.

## Architecture constraints

Android isolates app processes. `SYSTEM_ALERT_WINDOW` only allows a window above another app; it does not grant access to Geometry Dash's process or private files. A real loader requires a supported bootstrap/integration strategy and validation against the actual game package, exact version, ABI, native libraries and symbols. Song management additionally needs a verified source interface and a safe, supported path to the game's song storage. Do not silently overwrite the original game installation or bundle copyrighted game assets.

## Official implementation phases

### Phase 1 — Product structure

- Keep the menu organized and bilingual.
- Never show a gameplay or editor toggle as functional unless connected to a tested implementation.
- Make compatibility and initialization states visible.
- Maintain the planned botting, editor and mod catalogues with honest statuses.

### Phase 2 — Profile A discovery

- Identify the installed package and exact build using reliable evidence, not only `versionName` or the APK filename.
- Determine device ABI and inspect accessible native library metadata.
- Document what can and cannot be read under Android package isolation.
- Stop with an explicit unsupported state if build identity cannot be confirmed.

### Phase 3 — Loader/bootstrap feasibility

- Choose and document a legitimate, testable integration/bootstrap method before implementing hooks.
- Study supported public loader interfaces and existing static-analysis notes; do not copy proprietary code from another launcher.
- Keep original user levels and the original install protected.

### Phase 4 — First real integration

- Implement a minimal native initialization handshake for the selected build.
- Connect one small, low-risk feature to a real API and test it in-game.
- Test launch, initialization, game stability and feature behavior on-device before adding more features.

### Phase 5 — Editor and botting tools

- Implement editor helpers through supported game APIs, with backups before modifying user levels.
- Implement macro recording/playback and frame controls only after input timing and game state can be captured reliably.
- Keep unimplemented controls visibly unavailable.

### Phase 6 — NONG management

- Verify Song File Hub's supported public interface and usage conditions.
- Add search/download metadata only after confirming source access and attribution requirements.
- Validate a safe import/storage path for the selected Android build before claiming a song is usable in-game.
- Never overwrite original game files without explicit, recoverable user action.

### Phase 7 — Expand and validate

- Add mods only through implemented and tested APIs.
- Develop 2.209 as a separate compatibility adapter after recording its exact build identifier.
- Publish a compatibility matrix containing Android version, ABI, exact game build and individually tested features.

## Acceptance criteria

- Missing and unsupported game installations are handled cleanly.
- The loader initializes inside the selected game build, not merely beside it.
- At least one feature is shown working in actual gameplay before GPTM is described as a functional mod menu.
- NONGs are not described as installed until they can actually be selected and played in Geometry Dash.
- 2.2081 and 2.209 are tested independently; compiling the APK alone is not evidence of compatibility.

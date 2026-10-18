# GPTM — GPT Mod Menu

GPTM is an original, standalone Android project for a categorized Geometry Dash companion/mod-menu interface. It does not copy proprietary source code or assets from Eclipse, Mega Hack, or QOLmod.

## Current prototype

The overlay groups planned settings into:
- **Player / Practice:** checkpoints, Start Position helper, auto-retry, practice music sync, noclip testing, ignore-damage testing, speed/TPS controls, trajectory and input display.
- **Visuals / HUD:** FPS, hitboxes, attempts, screen shake/pulses/particles, low-detail helper, progress, click indicator and layout visibility.
- **Editor / Build:** C-to-Triggers, object/group ID helpers, copy/paste and free-move helpers, rotation helper, trigger-plan preview, object count and layout helper.
- **Level / QoL:** level ID and stats, song info, quick restart, hiding the menu during gameplay, saved preferences.
- **Diagnostics:** compatibility report, native integration status and debug logging.

**These are UI placeholders.** The switches currently only change their displayed state and show a notice; they do not alter Geometry Dash gameplay. This launcher is not a native mod loader and does not inject into the game's process.

## Compatibility gate

Before native integration is attempted, verify the actual installed game's manifest/version, package ID, ABI, native library build and symbols. An APK filename is not authoritative evidence of the in-game version. This repository does not yet establish a tested native hook path for GD 2.2090.

## Roadmap

1. **GPTM shell:** categories, search, favourites, setting persistence and a clear “not connected” status.
2. **Standalone utilities:** implement features that can work without altering the game, with honest capability reporting.
3. **Integration research:** identify supported extension points and confirm the target game's version and ABI. Do not assume overlay permission grants process access.
4. **Feature adapters:** connect options only to verified APIs, with capability checks and a visible unsupported state when unavailable.
5. **C→T translator:** design a restricted command parser and reviewable trigger-plan preview; create editor objects only through verified editor APIs.
6. **Validation:** build in CI, test on a separate game copy, preserve original APKs and levels, and record which features were actually tested.

## Engineering rules

- Never execute arbitrary C/C++ source from user input. Use a constrained command translator.
- Never label a feature “working” until it is connected and tested on the target version.
- Keep experimental gameplay-affecting features opt-in.
- A successful APK build verifies compilation only, not in-game compatibility.

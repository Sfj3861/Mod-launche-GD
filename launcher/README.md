# GPTM Official — .nova mod package

GPTM is being migrated from a standalone Android APK to the custom **.nova** mod-package format. The packaging workflow creates `GPTM.nova`; it does not build an APK.

## Current status

**Experimental package scaffold — not yet loadable in Geometry Dash.** A .nova archive defines GPTM's identity and target game profiles, but it cannot execute inside another Android app by itself. A compatible NOVA loader and a real game-side payload are required before it can be installed and run as an in-game mod.

## Package contents

- `manifest.json`: package identity, version, target builds, entry point and implementation status.
- `README.md`: package-specific notes.
- `payload/README.md`: explicit status of the missing game-side payload.

## Planned features

- Menu sections: Player, Visuals, Botting, Editor, Mods, Music and System.
- Spanish and English interface.
- Planned botting tools: macro recording/playback, frame-step and input timeline.
- Planned editor tools: object/group ID inspector, trigger planning, layout helpers and backups.
- Planned mod catalogue and Song File Hub link.

These are feature plans, not working game modifications. The old Kotlin overlay remains in the repository as reference source only; it is not packaged as an Android app and does not become an in-game mod automatically.

## Build the package

1. Open the repository's Actions tab.
2. Run or open **Package GPTM NOVA mod**.
3. Download the **GPTM-nova-mod** artifact.
4. Do not try to load it in Geometry Dash until a compatible NOVA loader exists.

## Compatibility

The intended first target is Geometry Dash Android 2.2081 (Profile A), with 2.209 handled separately. Neither profile is confirmed compatible yet. A successful archive build validates packaging only, not runtime compatibility.

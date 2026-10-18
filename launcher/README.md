# GPTM Official — NOVE mod package

GPTM is being migrated from a standalone Android APK to the custom .nove package format. The GitHub Actions workflow now packages the contents of nove-package/ as GPTM.nove; it no longer builds an APK.

## Current package status

**Experimental package scaffold — not yet loadable in Geometry Dash.** The package defines GPTM's identity, target game profiles and future entry point, but a valid .nove archive alone cannot execute inside another Android app. A compatible NOVE loader and a real game-side payload are required before it can be installed and run as an in-game mod.

## Package contents

- manifest.json: package identity, version, target builds, entry point and implementation status.
- README.md: package-specific installation and compatibility notes.
- payload/README.md: explicit status of the missing game-side payload.

## Current feature plan

- Menu sections: Player, Visuals, Botting, Editor, Mods, Music and System.
- Spanish and English interface.
- Planned botting tools: macro recording/playback, frame-step and input timeline.
- Planned editor tools: object/group ID inspector, trigger planning, layout helpers and backups.
- Planned mod catalogue and Song File Hub link.

These are feature plans, not working game modifications. The old Android overlay implementation remains in the repository as reference only; it is not packaged as an Android app and does not become an in-game mod automatically.

## Build the package

1. Open the repository's Actions tab.
2. Run or open **Package GPTM NOVE mod**.
3. Download the **GPTM-nove-mod** artifact.
4. Do not install it into Geometry Dash until a compatible NOVE loader is available.

## Compatibility

The intended first target is Geometry Dash Android 2.2081 (Profile A), with 2.209 handled separately. Neither profile is confirmed compatible yet. Creating the .nove archive validates packaging only, not runtime compatibility.

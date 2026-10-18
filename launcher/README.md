# GPTM Official — Android

GPTM is being developed toward a real mod-loader integration for Geometry Dash Android. The current official development line is **1.0.0-dev.1**, targeting Geometry Dash 2.2081 first (Profile A).

## Menu additions in the current development line

- Floating menu sections: Player, Visuals, Botting, Editor, Mods, Music and System.
- English/Spanish language selection for the floating menu.
- Botting feature plan: macro recording/playback, frame-step, frame advance, input timeline and route library. These are catalogue entries only until a game adapter exists.
- Editor feature plan: object/group ID inspector, trigger planning, layout helper, object tools, group/decor links and level backup. These are not integrated into the game yet.
- Initial mod catalogue with explicit "not installed" status; the catalogue does not install third-party mods.
- Song File Hub website shortcut at https://songfilehub.com/.

## Not implemented yet

- Native loader/bootstrap inside the Geometry Dash process.
- Native hooks or a tested mod API.
- Functional in-game botting, editor modifications or gameplay mods.
- Automatic NONG downloading, importing, mapping to levels or replacement of the game's song files.
- Confirmed compatibility with Geometry Dash 2.2081 or 2.209 on a real device.

Song File Hub can be opened in the browser from the Music tab. The current app does not claim to download and install NONGs directly into Geometry Dash. That requires a validated integration and a supported storage path; Android app-overlay permission alone cannot modify another app's private files or process.

## Development order

1. Organize the menu, language selection and truthful feature statuses.
2. Verify installed game package, exact build, ABI and accessible native libraries.
3. Document and implement a viable loader/bootstrap for Profile A.
4. Connect one small feature through a real API and test it in-game.
5. Add NONG download/import only after validating the song database interface, licensing/permissions and target game's song storage.
6. Add the 2.209 adapter separately after identifying its exact build.
7. Publish only a tested compatibility matrix.

## Build

Open the repository's Actions tab and download the `gptm-android-debug-apk` artifact from a successful run. The artifact is a development build, not a claim of working native mod support.

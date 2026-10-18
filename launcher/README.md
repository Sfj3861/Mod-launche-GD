# GPTM Official — Android

GPTM is being developed toward a real mod-loader integration for Geometry Dash Android. The current official development line is **1.0.0-dev.1**, targeting Geometry Dash 2.2081 first (Profile A).

## Implemented in this development build

- Standalone launcher screen and game-installation diagnostics.
- Movable floating GPTM button.
- Organized overlay menu with tabs: Player, Visuals, Editor and System.
- Feature entries display their real integration state; gameplay switches are not faked.
- GitHub Actions workflow builds a debug APK artifact.

## Not implemented yet

- Native loader/bootstrap inside the Geometry Dash process.
- Native hooks or a tested mod API.
- Gameplay-changing options such as noclip, speedhack, hitboxes or FPS controls.
- Confirmed compatibility with Geometry Dash 2.2081 or 2.209 on a real device.

The floating window is only an overlay. Android's overlay permission does not grant access to another app's process, and compiling the launcher does not prove native compatibility. Menu options must remain unavailable until the relevant game adapter is implemented and tested.

## Development order

1. Organize the official menu and remove misleading switches.
2. Verify the installed game package, exact build, ABI and accessible native libraries.
3. Document and implement a viable loader/bootstrap for Profile A.
4. Connect one small feature through a real API and test it in-game.
5. Add the 2.209 adapter separately after identifying its exact build.
6. Publish only a tested compatibility matrix.

## Build

Open the repository's Actions tab and download the gptm-android-debug-apk artifact from a successful run. The artifact is a development build, not a claim of working native mod support.

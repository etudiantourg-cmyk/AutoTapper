# AutoTapper

Android app (Kotlin) for periodic accessibility-based taps at a configurable screen coordinate.

## Build
GitHub Actions builds a debug APK on every push to `main` and on manual dispatch. Download the `AutoTapper-debug-apk` artifact from the Actions run.

## Use
1. Install the APK.
2. Open AutoTapper and enable its accessibility service in Android Settings.
3. Set X/Y screen coordinates, interval, and optional random offset radius.
4. Save settings, then start/stop tapping from the app.

## Important limitation
This version performs periodic taps. It does **not** inspect game visuals or detect the text/event "1x". Coordinates are screen pixels and may need adjustment for the device and display orientation. Use only where automated input is permitted.

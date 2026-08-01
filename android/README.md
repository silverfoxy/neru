# Neru Android MVP

This module proves physical-keyboard activation, coordinate-grid rendering, and
tap injection through an Android `AccessibilityService`.

See [desktop parity and Go/JNI plan](docs/DESKTOP_PARITY_AND_JNI.md) for the
feature inventory and the migration path to shared Go config and grid logic.

To build the arm64 native bridge locally or in CI, install Go plus the Android
NDK and set `ANDROID_NDK_HOME` (or `ANDROID_NDK_ROOT`), then run:

```sh
android/scripts/build-jni-bridge.sh
android/scripts/build-jni-shim.sh
```

Both resulting `.so` files are written to `android/.native-build/arm64-v8a/`
and Gradle stages them automatically in `lib/arm64-v8a/` while assembling an
APK. The app will use `NativeNeruBridge.loadConfigJSON(path)` once the APK
build packages them.

`NERU_ANDROID_API` defaults to `24` and `NERU_ANDROID_OUTPUT_DIR` can override
the output directory. On Termux, the scripts fall back to its Android-targeting
compiler and JDK JNI headers when no NDK environment variable is set.

## Requirements

- Android 7.0 (API 24) or newer
- A physical keyboard
- JDK 17
- Android SDK 36 and Build Tools 36
- Gradle 9.5, or Android Studio with Android Gradle Plugin 9.3 support

## Build and install

Open `android/` in Android Studio, or run from this directory after installing
the SDK and Gradle:

```sh
gradle :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Open Neru on the device, choose **Open Accessibility Settings**, and enable
**Neru keyboard navigation**. Android shows a sensitive-access warning because
the service can observe hardware keys and inject gestures.

## Controls

- `Ctrl+Shift+G`: show or hide the grid from any app
- `A S D F G H J K L`: enter a displayed coordinate and then a subgrid key
- `Backspace`: return to the previous grid depth
- `Return`: hide the overlay and tap the center of the selected area
- `Escape`: close without tapping

The first two keys are the displayed row/column coordinate. The third key picks
one of the nine labelled subcells. Press Return when the final target is shown.

## Scope

This is intentionally an Android-native interaction spike. It does not yet bind
the Go mode/config packages, expose configurable hotkeys, implement hints, or
produce a Play Store release. The next architectural step is a small public Go
package for the platform-neutral grid state, bound into this app once
the device interaction is validated.

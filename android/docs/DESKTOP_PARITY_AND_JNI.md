# Android desktop parity and Go/JNI plan

The Android app should reuse Neru's Go configuration and navigation policy. Kotlin
owns Android UI and gesture injection; Go remains the source of truth for TOML,
defaults, validation, hotkey grammar, and grid state.

## Capability inventory

| Desktop capability | Android status | Next step |
| --- | --- | --- |
| TOML defaults and validation | Partial Kotlin implementation | Route through Go bridge; return diagnostics instead of silent fallbacks. |
| Global activation | Integrated via Key Mapper broadcast | Keep Key Mapper as global key filter. |
| Focused accessibility overlay | Integrated | Keep as Android adapter. |
| Coordinate grid | Partial fixed 9×9 | Reuse Go `grid` session; remove nine-key limit. |
| Recursive grid | Partial configurable rows/columns | Reuse Go `recursivegrid` session; add layers/min-size/max-depth. |
| Mode hotkey actions | Partial tap chains and long press | Reuse Go hotkey parsing; expose Android-supported action diagnostics. |
| Virtual pointer | Partial Android click-point | Keep marker/nudging Android-native. |
| Scroll mode | Not started | Add focused overlay plus accessibility scroll gestures. |
| Hints and vision hints | Not started | Require Android accessibility and screenshot/vision adapters. |
| Themes and styling | Partial | Use Go resolved theme/default values. |
| Middle click, down/up, modifiers | Not started | Map only where Android gesture APIs support the action. |

## Go-owned policy

- `internal/config`: TOML decoding, defaults, theme resolution, validation, and
  hotkey normalization.
- `internal/core/domain/grid`: flexible coordinate-grid cell generation,
  prefixes, routing, and Backspace behavior.
- `internal/core/domain/recursivegrid`: layout, recursion, and state routing.

## Android-owned adapters

- App-private config file I/O and user-visible validation errors.
- Accessibility overlays, focused keyboard delivery, and Key Mapper broadcasts.
- Tap, long-press, scroll, and future Android gesture injection.
- Virtual-pointer rendering and Android-specific UX.

## JNI contract

Create `cmd/neru-android-bridge` inside this repository so it can import the
existing `internal/...` packages. Expose a small JSON ABI:

```text
parseConfig(toml, appearance) -> { config, diagnostics }

createGridSession(config, bounds) -> sessionId
gridInput(sessionId, key) -> GridStateSnapshot
gridBackspace(sessionId) -> GridStateSnapshot

createRecursiveGridSession(config, bounds) -> sessionId
recursiveGridInput(sessionId, key) -> RecursiveGridStateSnapshot
recursiveGridBackspace(sessionId) -> RecursiveGridStateSnapshot
destroySession(sessionId)
```

Snapshots carry visible cells, labels, valid prefixes, selected bounds, and
target point. Kotlin draws the snapshot and invokes Android gestures only after
Go resolves the target. Unsupported desktop actions must return a capability
diagnostic, not be ignored.

## Build plan

Use `gomobile bind` or Go `-buildmode=c-shared` plus a minimal JNI C shim. The
Termux builder will build `libneru_android_bridge.so` for `arm64-v8a`, package
it under `lib/arm64-v8a/`, and Kotlin will load it with
`System.loadLibrary("neru_android_bridge")`.

The current environment has Android-targeting Clang but no SDK NDK directory or
JNI headers. Keep the Kotlin reader as a temporary fallback until an on-device
JNI smoke test passes.

## Migration commits

1. Add this parity contract and a Go bridge API with host tests.
2. Build and load a JNI smoke-test library on Android.
3. Move TOML parsing/defaults/validation into Go.
4. Move recursive-grid sessions into Go and compare snapshots in tests.
5. Move flexible coordinate-grid sessions into Go; delete the Android nine-key
   restriction.
6. Add Android gesture adapters, then scroll and hints.

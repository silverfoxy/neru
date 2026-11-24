# Android parity: architecture and delivery roadmap

Status: proposal, audited against upstream `e94d05ec` (2026-11-24), fork Android MVP `5e5ae636`, and fork JNI prototype `111bb9a0`. This is a **new branch from upstream/main**, not a merge of the stale fork main. No Android device validation has been performed.

## Product boundary and definition of parity

Goal: Android can import and edit a portable Neru TOML configuration, apply supported settings consistently with desktop, and deliver the core keyboard-driven navigation flows. **Parity is semantic where Android has the capability**, not a promise that macOS-only APIs exist on a phone. A written but unsupported desktop setting must survive round-trip and produce a clear diagnostic, never silently masquerade as active. Maintain an explicit per-option and per-action platform support matrix; update `neru doctor`/Android diagnostics with evidence-based status.

Definition of done for each feature: same shared policy/default/validation behavior as desktop; Android adapter implements the operation; host tests cover policy; emulator and physical-device tests cover input/overlay/gesture behavior; permission failures, rotation, display insets, multiwindow, and reload are handled; docs and capability matrix match reality. Builds alone are not device parity.

## Verified starting point

- Fork `main` at `a2f61841` has no Android changes and was 706 upstream commits behind `e94d05ec` on inspection. The `android` branch has a Kotlin AccessibilityService, overlay, fixed 9×9 grid, key handling and tap injection; it is an interaction spike, not desktop-config parity (`android/README.md`, `NeruAccessibilityService.kt`, `GridNavigator.kt`).
- `android-jni-parity` extends `android` by 14 commits: an existing design at `android/docs/DESKTOP_PARITY_AND_JNI.md`, Go config snapshot/grid helper, JNI shim and build scripts/CI. The exported native function loads config only (`cmd/neru-android-bridge/main.go`); Kotlin still runs its own grid. No on-device JNI result was found.
- Current upstream reorganized `internal/core/domain/grid` to `internal/domain/grid`; a straight branch merge risks adapting old imports and stale config semantics. Config currently recognizes only darwin/linux/windows in `internal/config/per_platform.go`; an unknown OS falls back to Linux. Android must not inherit Linux silently.
- The old `docs/ARCHITECTURE.md` platform table is stale relative to `docs/ROADMAP.md` and `docs/reference/platform-support.md`. Inventory the code, not that table alone.

## Target architecture

See `neru-android-architecture.html` in this directory. Shared Go owns config TOML/schema/defaults/validation/derived policy and navigation state transitions. Kotlin/Android owns lifecycle, UI/accessibility tree, screen bounds, key and accessibility events, overlays, gestures, permissions and file import/export. A narrow versioned bridge transports **resolved snapshots, diagnostics, inputs and outcomes**, not raw Go objects. No shell or desktop daemon is implied on Android. Keep package boundaries and C ownership explicit; avoid native callbacks into dead activities/services.

**Decision gate before committing to JNI:** measure APK size, cold start, bridge latency, config schema reuse and debugging cost against a Kotlin adapter consuming a generated platform-neutral schema/policy vectors. Favor Go bridge if maintaining two implementations produces behavioral drift; retain the prototype only after device smoke and lifecycle tests. Do not start the whole product from scratch to settle this one boundary.

## Capability/setting inventory to turn into executable matrix

| Area | Android today | Target and gate |
| --- | --- | --- |
| TOML defaults/validation/overrides | Prototype Go snapshot, not applied to service | Portable import/export, validation diagnostics, safe reload/rollback; same shared schema; define Android defaults and `android` in per-platform tables. |
| Global activation | Accessibility key events; optional Key Mapper receiver in JNI branch | Document key-source limitations, supported chords and permission flow; no claim of arbitrary global desktop hotkeys. |
| Grid | Fixed Kotlin 9×9 | Go-driven variable cells/prefixes/subgrid, accurate bounds and gesture target; device geometry tests. |
| Recursive grid | Prototype partial Kotlin behavior | Go-owned recursion/session and visual snapshots; test depth, bounds, Backspace, cancellation. |
| Hints | Absent | Android accessibility-node discovery first; vision fallback only with consent and supported capture path; labels/filters/action semantics. |
| Scroll / pointer / drag | Tap and some long-press prototype | Implement Android-feasible gestures and action diagnostics; declare unsupported button/modifier semantics. |
| Themes / typography / overlays | Kotlin draw path | Map resolved shared style to Android renderer; rotation, density, insets, contrast tests. |
| Mac/Linux/Windows-specific options | No Android declaration | Preserve as inert with clear warnings; no fake menu bar, X11/Wayland, systray, desktop shell or cursor semantics. |
| CLI/IPC/hot reload | No Android daemon CLI | Android config UI/import and reload API with equivalent validation/rollback; do not advertise CLI parity unless shipped. |

## Milestones and upstreamable PR slices

**M0 — Baseline and contract.** Preserve both Android branches; branch from current upstream. Produce an automated option/action/mode matrix by comparing `internal/config` schema/platform support, desktop example configs and Android behavior. Add golden TOML cases (scalar vs platform-table, overrides, unknown/inert/invalid fields) and record reproducible device test matrix. Gate: no unsupported feature is labeled supported. PR: docs and neutral contract tests.

**M1 — Android adapter MVP on current tree.** Port the isolated Kotlin AccessibilityService/overlay/gesture interaction from `android` with modern Android Gradle setup, explicit permission UX and build checks. Do not port obsolete Go imports. Gate: CI APK build, emulator interaction, physical-device grid-and-tap including rotation/insets; otherwise publish draft only. PR: isolated Android foundation.

**M2 — Portable config foundation.** Add Android to parity enums, per-platform TOML parsing/serialization, schema, defaults and option support registry. Unknown platforms must fail explicitly, not map to Linux. Build load/validate/round-trip golden tests and diagnostics; keep Android storage isolated. Gate: desktop regression tests, Android/desktop config vectors, unknown-option preservation policy. PR: shared config groundwork, followed by Android import/validation UI.

**M3 — Bridge and rollback.** Adapt the existing `internal/androidbridge`, C ABI and JNI prototype to current upstream packages; define ABI version/error/result ownership, concurrency and service lifecycle. Evaluate generated schema alternative. Ship a config parse JNI smoke test on emulator/device; do not remove Kotlin fallback until passing. Gate: signed/packaged APK loads arm64 library, handles malformed TOML and service restart without crash. PR: small Go bridge + tests; separate JNI packaging PR.

**M4 — Grid/recursive-grid policy parity.** Expose create/input/backspace/destroy session APIs with snapshots, wire Kotlin drawing and gesture dispatch to Go decisions. Remove fixed 9×9 policy only after comparison tests. Gate: golden desktop↔Android state sequences and on-device target accuracy. PRs: grid then recursive grid; each with fallback removal only when proven.

**M5 — Configuration and mode completeness.** Inventory each key and supported action across hints, scroll, pointer/drag, styling, application rules and reload. Implement supported items in slices; expose unsupported diagnostics and preserve portable files. Gate: coverage matrix generated/checked by tests, on-device acceptance per mode. PRs: one mode/adapter at a time.

**M6 — Release and maintenance.** Permissions/privacy review, crash and latency budgets, accessibility disclosure, install/update docs, ABI and Android API compatibility CI, reproducible native builds. Gate: device test report, Android artifact, maintainers' review, stable support matrix. No claim of full parity for impossible OS-specific features.

## Fork/upstream strategy

Keep `origin/android` and `origin/android-jni-parity` as read-only references. Build each PR from a fresh upstream-main base; avoid an all-at-once fork merge or rewriting the historical branch. Split pure-Go platform-agnostic configuration and policy changes from Android packaging and UI. Open draft PRs for unverified device paths, discuss contract and Android scope with upstream maintainers before large code ports. If upstream declines Android-specific code, maintain a small downstream Android app that consumes upstream-compatible config/policy artifacts; do not maintain a private fork of the entire desktop core without a compatibility/test plan. Revisit a rewrite only if the bridge spike fails measurable gates, not because the fork main is stale.

## Immediate execution checklist

- [x] Pin all three revisions and inspect two Android branches.
- [x] Create a fresh local planning branch on upstream/main and document target architecture.
- [ ] Run upstream Go tests and Android Gradle build in a prepared environment (Go/Gradle/ADB unavailable on this host at audit time).
- [ ] Generate exhaustive config option/action inventory and golden compatibility fixtures.
- [ ] Confirm target Android version, device(s), desired hotkey source and upstream maintainer appetite.
- [ ] Implement M0 contract tests, then M1 in independently reviewable PRs.

## Source map

`internal/config/{config.go,per_platform.go,platform_support.go,loader/load.go}`; `internal/domain/{grid,recursivegrid,parity}`; `docs/reference/platform-support.md` at upstream `e94d05ec`. Android MVP `5e5ae636`: `android/app/src/main/java/com/y3owk1n/neru/android/{NeruAccessibilityService.kt,GridNavigator.kt,GridOverlayView.kt}`. JNI prototype `111bb9a0`: `android/docs/DESKTOP_PARITY_AND_JNI.md`, `cmd/neru-android-bridge/main.go`, `internal/androidbridge/`, `.github/workflows/android.yml`.

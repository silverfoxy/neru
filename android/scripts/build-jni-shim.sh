#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
source "$ROOT_DIR/android/scripts/android-toolchain.sh"
neru_android_configure_toolchain

ABI_DIR="${NERU_ANDROID_OUTPUT_DIR:-$ROOT_DIR/android/.native-build/$NERU_ANDROID_ABI}"

if [[ ! -f "$ABI_DIR/libneru_android_bridge.so" ]]; then
    "$ROOT_DIR/android/scripts/build-jni-bridge.sh"
fi

include_flags=(-I"$NERU_JNI_INCLUDE")
if [[ -n "$NERU_JNI_PLATFORM_INCLUDE" ]]; then
    include_flags+=(-I"$NERU_JNI_PLATFORM_INCLUDE")
fi

"$NERU_ANDROID_CC" -shared -fPIC \
    "${include_flags[@]}" \
    "$ROOT_DIR/android/app/src/main/cpp/neru_jni.c" \
    -L"$ABI_DIR" \
    -lneru_android_bridge \
    -Wl,-rpath,'$ORIGIN' \
    -o "$ABI_DIR/libneru_jni.so"

echo "Built $ABI_DIR/libneru_jni.so"

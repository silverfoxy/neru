#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
source "$ROOT_DIR/android/scripts/android-toolchain.sh"
neru_android_configure_toolchain

OUT_DIR="${NERU_ANDROID_OUTPUT_DIR:-$ROOT_DIR/android/.native-build/$NERU_ANDROID_ABI}"

mkdir -p "$OUT_DIR"
GOOS=android GOARCH=arm64 CGO_ENABLED=1 CC="$NERU_ANDROID_CC" CXX="$NERU_ANDROID_CXX" go -C "$ROOT_DIR" build -buildmode=c-shared \
    -o "$OUT_DIR/libneru_android_bridge.so" \
    ./cmd/neru-android-bridge

echo "Built $OUT_DIR/libneru_android_bridge.so"

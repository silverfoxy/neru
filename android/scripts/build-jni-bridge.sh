#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT_DIR="$ROOT_DIR/android/.termux-build/jni/arm64-v8a"

mkdir -p "$OUT_DIR"
CGO_ENABLED=1 go build -buildmode=c-shared \
    -o "$OUT_DIR/libneru_android_bridge.so" \
    "$ROOT_DIR/cmd/neru-android-bridge"

echo "Built $OUT_DIR/libneru_android_bridge.so"

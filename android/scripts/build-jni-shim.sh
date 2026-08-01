#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ABI_DIR="$ROOT_DIR/android/.termux-build/jni/arm64-v8a"
JNI_INCLUDE="${NERU_JNI_INCLUDE:-${JAVA_HOME:-}/include}"
JNI_PLATFORM_INCLUDE="${NERU_JNI_PLATFORM_INCLUDE:-$JNI_INCLUDE/linux}"

if [[ ! -f "$JNI_INCLUDE/jni.h" ]]; then
    echo "jni.h not found; set NERU_JNI_INCLUDE to an NDK or JDK include directory" >&2
    exit 1
fi

if [[ ! -f "$ABI_DIR/libneru_android_bridge.so" ]]; then
    "$ROOT_DIR/android/scripts/build-jni-bridge.sh"
fi

aarch64-linux-android-clang -shared -fPIC \
    -I"$JNI_INCLUDE" \
    -I"$JNI_PLATFORM_INCLUDE" \
    "$ROOT_DIR/android/app/src/main/cpp/neru_jni.c" \
    -L"$ABI_DIR" \
    -lneru_android_bridge \
    -Wl,-rpath,'$ORIGIN' \
    -o "$ABI_DIR/libneru_jni.so"

echo "Built $ABI_DIR/libneru_jni.so"

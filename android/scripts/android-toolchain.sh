#!/usr/bin/env bash
# Shared Android toolchain discovery for local development and CI.
#
# Prefer an Android NDK supplied through ANDROID_NDK_HOME or ANDROID_NDK_ROOT.
# Termux's Android-targeting compiler is retained only as a convenience
# fallback, so callers do not need Termux-specific paths in CI configuration.

neru_android_configure_toolchain() {
    : "${NERU_ANDROID_ABI:=arm64-v8a}"
    : "${NERU_ANDROID_API:=24}"

    if [[ "$NERU_ANDROID_ABI" != "arm64-v8a" ]]; then
        echo "unsupported NERU_ANDROID_ABI: $NERU_ANDROID_ABI (currently arm64-v8a only)" >&2
        return 1
    fi

    local ndk_root="${ANDROID_NDK_HOME:-${ANDROID_NDK_ROOT:-}}"
    if [[ -n "$ndk_root" ]]; then
        local host_os host_arch host_tag toolchain
        host_os="$(uname -s | tr '[:upper:]' '[:lower:]')"
        host_arch="$(uname -m)"
        case "$host_arch" in
            x86_64) host_arch=x86_64 ;;
            arm64|aarch64) host_arch=arm64 ;;
            *) echo "unsupported NDK host architecture: $host_arch" >&2; return 1 ;;
        esac
        case "$host_os" in
            linux|darwin) host_tag="$host_os-$host_arch" ;;
            mingw*|msys*|cygwin*) host_tag="windows-x86_64" ;;
            *) echo "unsupported NDK host OS: $host_os" >&2; return 1 ;;
        esac

        toolchain="$ndk_root/toolchains/llvm/prebuilt/$host_tag"
        NERU_ANDROID_CC="${NERU_ANDROID_CC:-$toolchain/bin/aarch64-linux-android${NERU_ANDROID_API}-clang}"
        NERU_ANDROID_CXX="${NERU_ANDROID_CXX:-$toolchain/bin/aarch64-linux-android${NERU_ANDROID_API}-clang++}"
        NERU_JNI_INCLUDE="${NERU_JNI_INCLUDE:-$toolchain/sysroot/usr/include}"
        NERU_JNI_PLATFORM_INCLUDE="${NERU_JNI_PLATFORM_INCLUDE:-}"
    else
        NERU_ANDROID_CC="${NERU_ANDROID_CC:-aarch64-linux-android-clang}"
        NERU_ANDROID_CXX="${NERU_ANDROID_CXX:-aarch64-linux-android-clang++}"
        NERU_JNI_INCLUDE="${NERU_JNI_INCLUDE:-${JAVA_HOME:-}/include}"
        NERU_JNI_PLATFORM_INCLUDE="${NERU_JNI_PLATFORM_INCLUDE:-$NERU_JNI_INCLUDE/linux}"
    fi

    if ! command -v "$NERU_ANDROID_CC" >/dev/null 2>&1; then
        echo "Android C compiler not found: $NERU_ANDROID_CC" >&2
        echo "Set ANDROID_NDK_HOME or NERU_ANDROID_CC." >&2
        return 1
    fi
    if [[ ! -f "$NERU_JNI_INCLUDE/jni.h" ]]; then
        echo "jni.h not found at $NERU_JNI_INCLUDE/jni.h" >&2
        echo "Set ANDROID_NDK_HOME or NERU_JNI_INCLUDE." >&2
        return 1
    fi

    export NERU_ANDROID_ABI NERU_ANDROID_API NERU_ANDROID_CC NERU_ANDROID_CXX
    export NERU_JNI_INCLUDE NERU_JNI_PLATFORM_INCLUDE
}

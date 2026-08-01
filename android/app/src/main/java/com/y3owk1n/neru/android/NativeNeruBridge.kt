package com.y3owk1n.neru.android

/**
 * Small Android boundary around the Go-owned configuration policy.
 *
 * The two libraries must be packaged together under the same ABI directory.
 * Native callers receive JSON so this boundary stays stable while the Go
 * configuration model evolves.
 */
object NativeNeruBridge {
    init {
        System.loadLibrary("neru_android_bridge")
        System.loadLibrary("neru_jni")
    }

    external fun loadConfigJSON(path: String): String
}

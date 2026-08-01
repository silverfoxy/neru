#include <jni.h>

// These functions are exported by cmd/neru-android-bridge. Keep this thin
// layer free of application policy: Go owns decoding and validation while
// Kotlin owns Android file locations and presentation.
extern char *NeruLoadConfigJSON(char *path);
extern void NeruFreeString(char *value);

JNIEXPORT jstring JNICALL
Java_com_y3owk1n_neru_android_NativeNeruBridge_loadConfigJSON(
    JNIEnv *env,
    jobject instance,
    jstring path) {
    (void)instance;

    if (path == NULL) {
        return (*env)->NewStringUTF(env, "{\"diagnostics\":[{\"message\":\"config path is required\"}]}");
    }

    const char *path_chars = (*env)->GetStringUTFChars(env, path, NULL);
    if (path_chars == NULL) {
        return NULL;
    }

    char *json = NeruLoadConfigJSON((char *)path_chars);
    (*env)->ReleaseStringUTFChars(env, path, path_chars);
    if (json == NULL) {
        return (*env)->NewStringUTF(env, "{\"diagnostics\":[{\"message\":\"native config load failed\"}]}");
    }

    jstring result = (*env)->NewStringUTF(env, json);
    NeruFreeString(json);
    return result;
}

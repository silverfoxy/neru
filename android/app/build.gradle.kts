plugins {
    id("com.android.application")
}

val repositoryRoot = rootProject.projectDir.parentFile
val nativeBuildDirectory = repositoryRoot.resolve("android/.native-build/arm64-v8a")
val stagedNativeDirectory = layout.buildDirectory.dir("generated/neru-jni/arm64-v8a")

android {
    namespace = "com.y3owk1n.neru.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.y3owk1n.neru.android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    sourceSets {
        getByName("main").jniLibs.srcDir(stagedNativeDirectory)
    }
}

val buildNeruGoBridge = tasks.register<Exec>("buildNeruGoBridge") {
    group = "build"
    description = "Builds Neru's Go configuration bridge for Android arm64."
    workingDir(repositoryRoot)
    commandLine(repositoryRoot.resolve("android/scripts/build-jni-bridge.sh").absolutePath)
}

val buildNeruJniShim = tasks.register<Exec>("buildNeruJniShim") {
    group = "build"
    description = "Builds the JNI shim linked to Neru's Go bridge."
    dependsOn(buildNeruGoBridge)
    workingDir(repositoryRoot)
    commandLine(repositoryRoot.resolve("android/scripts/build-jni-shim.sh").absolutePath)
}

val syncNeruNativeLibraries = tasks.register<Sync>("syncNeruNativeLibraries") {
    group = "build"
    description = "Stages Neru's native libraries for APK packaging."
    dependsOn(buildNeruJniShim)
    from(nativeBuildDirectory) {
        include("libneru_android_bridge.so", "libneru_jni.so")
    }
    into(stagedNativeDirectory)
}

// AGP creates one merge task per build variant. Attaching lazily keeps debug,
// release, and future product flavors consistent without hard-coding task names.
tasks.configureEach {
    if (name.startsWith("merge") && name.endsWith("NativeLibs")) {
        dependsOn(syncNeruNativeLibraries)
    }
}

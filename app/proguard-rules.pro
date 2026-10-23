# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

## InnerTube API & Data Models (Critical for release builds)
-keep class com.music.innertube.** { *; }
-keepclassmembers class com.music.innertube.** { *; }
-keepclassmembers class com.music.innertube.** {
    *** Companion;
}
-keepclasseswithmembers class com.music.innertube.** {
    kotlinx.serialization.KSerializer serializer(...);
}

## Sonic Core, Models, Playback & Multi-Source Resolvers
-keep class com.music.sonic.models.** { *; }
-keepclassmembers class com.music.sonic.models.** { *; }
-keep class com.music.sonic.playback.** { *; }
-keepclassmembers class com.music.sonic.playback.** { *; }
-keep class com.music.sonic.data.sources.** { *; }
-keepclassmembers class com.music.sonic.data.sources.** { *; }
-keep class com.music.sonic.utils.YTPlayerUtils** { *; }
-keepclassmembers class com.music.sonic.utils.YTPlayerUtils** { *; }
-keep class com.music.sonic.utils.cipher.** { *; }
-keep class com.music.sonic.utils.sabr.** { *; }
-keep class com.music.sonic.utils.potoken.** { *; }

# WEB_REMIX Streaming - WebView JavaScript interfaces
-keepclassmembers class com.music.sonic.utils.sabr.EjsNTransformSolver$SolverWebView {
    @android.webkit.JavascriptInterface public *;
}
-keepclassmembers class com.music.sonic.utils.cipher.CipherWebView {
    @android.webkit.JavascriptInterface public *;
}
-keepclassmembers class com.music.sonic.utils.potoken.PoTokenWebView {
    @android.webkit.JavascriptInterface public *;
}

# Keep coroutine continuation for WebView callbacks
-keepclassmembers class * {
    void resume(...);
    void resumeWithException(...);
}

## Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-keepclassmembers class androidx.media3.** { *; }
-dontwarn androidx.media3.**

## Protobuf & Nanojson
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**
-keep class com.grack.nanojson.** { *; }
-dontwarn com.grack.nanojson.**

## Retrofit & Gson
-keep class com.squareup.retrofit2.** { *; }
-dontwarn com.squareup.retrofit2.**
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

## Kotlin Serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `Companion` object fields of serializable classes.
-if @kotlinx.serialization.Serializable class **
-keepclasseswithmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclasseswithmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclasseswithmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-dontwarn javax.servlet.ServletContainerInitializer
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn org.slf4j.impl.StaticLoggerBinder

## Rules for NewPipeExtractor
-keep class org.schabi.newpipe.extractor.services.youtube.protos.** { *; }
-keep class org.schabi.newpipe.extractor.timeago.patterns.** { *; }
-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.javascript.engine.** { *; }
-dontwarn org.mozilla.javascript.JavaToJSONConverters
-dontwarn org.mozilla.javascript.tools.**
-keep class javax.script.** { *; }
-dontwarn javax.script.**
-keep class jdk.dynalink.** { *; }
-dontwarn jdk.dynalink.**

## Logging (does not affect Timber)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    ## Leave in release builds
    #public static int i(...);
    #public static int w(...);
    #public static int e(...);
}

# Generated automatically by the Android Gradle plugin.
-dontwarn java.beans.BeanDescriptor
-dontwarn java.beans.BeanInfo
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.PropertyDescriptor

# Keep all classes within the kuromoji package
-keep class com.atilika.kuromoji.** { *; }

## Queue Persistence Rules
# Keep queue-related classes to prevent serialization issues in release builds
-keep class com.music.sonic.models.PersistQueue { *; }
-keep class com.music.sonic.models.PersistPlayerState { *; }
-keep class com.music.sonic.models.QueueData { *; }
-keep class com.music.sonic.models.QueueType { *; }
-keep class com.music.sonic.playback.queues.** { *; }

# Keep serialization methods for queue persistence
-keepclassmembers class * implements java.io.Serializable {
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
}

## UCrop Rules
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

## Google Cast Rules
-keep class com.music.sonic.cast.** { *; }
-keep class com.google.android.gms.cast.** { *; }
-keep class androidx.mediarouter.** { *; }

## JSoup re2j optional dependency
-dontwarn com.google.re2j.**

# Vibra fingerprint library
-keep class com.music.sonic.recognition.VibraSignature { *; }
-keepclassmembers class com.music.sonic.recognition.VibraSignature {
    native <methods>;
}

## Kotlin Reflection Fix
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**

## Ktor Serialization
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.ktor.**

## Shazam Models
-keep class com.music.shazamkit.models.** { *; }
-keepclassmembers class com.music.shazamkit.models.** {
    *;
}

## Listen Together Serialization
-keep class com.music.sonic.listentogether.** { *; }
-keepclassmembers class com.music.sonic.listentogether.** {
    *;
}
-keepclassmembers class com.music.sonic.listentogether.** {
    *** Companion;
}
-keepclasseswithmembers class com.music.sonic.listentogether.** {
    kotlinx.serialization.KSerializer serializer(...);
}

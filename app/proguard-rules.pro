# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# TODO: (Pirimid SDK Integration)
-dontwarn androidx.databinding.**
-keep class com.onemoney.DataBinderMapperImpl.* { *; }
-dontwarn com.onemoney.DataBinderMapperImpl.*
-keepclassmembers class * extends android.app.Activity {
       public void *(android.view.View);
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keep class com.onemoney.sdk.models.** { *; }
-keep class com.onemoney.sdk.activity.** { *; }
-keep class com.onemoney.sdk.fragments.** { *; }
-keep class com.onemoney.sdk.component.** { *; }
-keep class com.onemoney.sdk.utils.** { *; }
-keep class com.onemoney.sdk.adapter.** { *; }
-keep class com.onemoney.sdk.interfaces.** { *; }
-keep class com.onemoney.sdk.volleyservices.** { *; }
-keep class com.sdk.pirimid_sdk.** { *; }
-dontwarn com.onemoney.sdk.**
-dontwarn retrofit2.**
-dontwarn org.codehaus.mojo.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations
-keepattributes EnclosingMethod
-keepclasseswithmembers class * {
    @retrofit2.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.* <methods>;
}
-dontnote retrofit2.Platform
-keepattributes Signature
-keepattributes Exceptions
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.examples.android.model.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers enum * { *; }
-keep class net.mreunionlabs.wob.model.request.** { *; }
-keep class net.mreunionlabs.wob.model.response.** { *; }
-keep class net.mreunionlabs.wob.model.gson.** { *; }
-keep class com.sdk.pirimid_sdk.** {public *;}
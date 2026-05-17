# Consumer ProGuard rules for up-antivirus-common
# These rules are applied to apps that include this library

# ==== LOGGING REMOVAL ====
# Remove all Android logging for security
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

# ==== LIBRARY API PRESERVATION ====
# Keep public models that consuming apps need
-keep class com.unplugged.upantiviruscommon.model.** { *; }
-keep class com.unplugged.upantiviruscommon.malware.** { *; }
-keep class com.unplugged.upantiviruscommon.datastore.RemoteDataStore { *; }
-keep class com.unplugged.upantiviruscommon.retrofit.** { *; }
-keep class com.unplugged.upantiviruscommon.utils.** { *; }

# ==== GSON SERIALIZATION ====
# Keep GSON annotations and serialized fields
-keep class com.google.gson.** { *; }
-keep class com.unplugged.upantiviruscommon.model.** {
    @com.google.gson.annotations.SerializedName <fields>;
    *;
}
-keep class com.unplugged.upantiviruscommon.** {
    @com.google.gson.annotations.SerializedName <fields>;
    <init>();
    *** get*();
    *** set*(...);
}

# ==== RETROFIT NETWORKING ====
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ==== THIRD PARTY LIBRARIES ====
# Picasso image loading
-keep class com.squareup.picasso.** { *; }
-dontwarn com.squareup.picasso.**

# DNS Java for network operations
-keep class org.xbill.DNS.** { *; }
-dontwarn org.xbill.DNS.**

# Lottie animations
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# Lazy Sodium cryptography
-keep class com.goterl.lazysodium.** { *; }
-keep class com.goterl.lazysodium.interfaces.** { *; }
-dontwarn com.goterl.lazysodium.**

# JNA (Java Native Access)
-keep class com.sun.jna.** { *; }
-keep class net.java.dev.jna.** { *; }
-dontwarn com.sun.jna.**
-dontwarn net.java.dev.jna.**

# ==== UP ACCOUNT INTEGRATION ====
# Keep account modules (they're imported as AAR files)
-keep class com.unplugged.account.** { *; }
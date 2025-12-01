# ProGuard rules for up-antivirus-common module
# Security-focused obfuscation for shared common components

# ==== AGGRESSIVE OBFUSCATION ====
-overloadaggressively
-repackageclasses 'com'
-allowaccessmodification

# Hide source files completely for security
-renamesourcefileattribute ""
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable

# ==== PROJECT-SPECIFIC DATA MODELS ====
# Keep specific model classes with serialization annotations
-keep class com.unplugged.upantiviruscommon.model.Version {
    @com.google.gson.annotations.SerializedName <fields>;
    *;
}
-keep class com.unplugged.upantiviruscommon.model.ScanParams { *; }
-keep class com.unplugged.upantiviruscommon.model.ScanProgress { *; }
-keep class com.unplugged.upantiviruscommon.model.ScanUpdate { *; }
-keep class com.unplugged.upantiviruscommon.model.ApkInfo { *; }
-keep class com.unplugged.upantiviruscommon.model.DatabaseInfo { *; }
-keep class com.unplugged.upantiviruscommon.model.AppInfo { *; }

# Keep malware and threat related classes
-keep class com.unplugged.upantiviruscommon.malware.** { *; }

# Keep remote data store
-keep class com.unplugged.upantiviruscommon.datastore.RemoteDataStore { *; }

# Keep utility classes
-keep class com.unplugged.upantiviruscommon.utils.DateTimeUtils { *; }

# ==== NETWORKING ====
# Retrofit interfaces
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Gson serialization
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keep class com.unplugged.upantiviruscommon.** {
    @com.google.gson.annotations.SerializedName <fields>;
    <init>();
    *** get*();
    *** set*(...);
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

# ==== SECURITY HARDENING ====
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Suppress warnings for external dependencies
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
-dontwarn com.google.errorprone.annotations.**
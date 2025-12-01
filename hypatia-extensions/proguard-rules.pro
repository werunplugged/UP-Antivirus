# ProGuard rules for hypatia-extensions module
# Security-focused obfuscation for Hypatia malware scanner integration

# ==== AGGRESSIVE OBFUSCATION ====
-overloadaggressively
-repackageclasses 'hyp'
-allowaccessmodification

# Hide source files completely for security
-renamesourcefileattribute ""
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable

# ==== PROJECT-SPECIFIC CLASSES ====
# Keep main Hypatia access point interface
-keep interface com.unplugged.hypatia_extensions.HypatiaAccessPoint { *; }

# Keep main Hypatia class and its essential methods
-keep class com.unplugged.hypatia_extensions.Hypatia {
    <init>(...);
    *** scan(...);
    *** updateDatabase(...);
    *** getDatabaseInfo(...);
    *** stopScan(...);
}

# Keep scan parameters and malware mapper
-keep class com.unplugged.hypatia_extensions.ScanParams { *; }
-keep class com.unplugged.hypatia_extensions.HypatiaMalwareMapper { *; }

# ==== HYPATIA CORE SCANNER LIBRARY ====
# Keep essential classes from us.spotco.malwarescanner
-keep class us.spotco.malwarescanner.Database {
    *** downloadDatabase(...);
    *** updateDatabase(...);
    *** getDatabaseInfo(...);
    interface UpdateListener;
}
-keep class us.spotco.malwarescanner.MalwareScanner {
    *** scanFile(...);
    *** scanFileBytes(...);
}
-keep class us.spotco.malwarescanner.MalwareScannerService {
    <init>(...);
    *** scan(...);
}
-keep class us.spotco.malwarescanner.Utils {
    *** setDatabaseUrl(...);
    *** setContext(...);
}

# ==== THIRD PARTY LIBRARIES ====
# Alerter for notifications
-keep class com.tapadoo.alerter.** { *; }
-dontwarn com.tapadoo.alerter.**

# BouncyCastle cryptography
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-dontwarn org.bouncycastle.x509.**
-dontwarn org.bouncycastle.jce.provider.**

# Google Guava
-keep class com.google.common.** { *; }
-dontwarn com.google.common.**
-dontwarn com.google.j2objc.annotations.**

# Apache Commons IO
-keep class org.apache.commons.** { *; }
-dontwarn org.apache.commons.**

# MultiDex support
-keep class androidx.multidex.** { *; }
-dontwarn androidx.multidex.**

# ==== SECURITY HARDENING ====
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Suppress warnings for external dependencies
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn sun.misc.Unsafe
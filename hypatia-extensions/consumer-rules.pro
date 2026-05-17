# Consumer ProGuard rules for hypatia-extensions
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
# Keep public API that consuming apps need
-keep interface com.unplugged.hypatia_extensions.HypatiaAccessPoint { *; }
-keep class com.unplugged.hypatia_extensions.Hypatia {
    <init>(...);
    public *** startScan(...);
    public *** stopScan(...);
    public *** updateDatabase(...);
    public *** getMalwareScanner(...);
    public *** isDatabaseLoaded(...);
    public *** loadDatabase(...);
    public *** isDatabaseAvailable(...);
    public *** enableMalwareService(...);
    public *** disableMalwareService(...);
}
-keep class com.unplugged.hypatia_extensions.ScanParams { *; }
-keep class com.unplugged.hypatia_extensions.HypatiaMalwareMapper { *; }

# ==== HYPATIA NATIVE SCANNER LIBRARY ====
# Keep essential Hypatia scanner classes that use native code
-keep class us.spotco.malwarescanner.** { *; }
-keep interface us.spotco.malwarescanner.malware.HypatiaMalwareScannerListener { *; }

# ==== THIRD-PARTY LIBRARIES ====
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

# Alerter notifications
-keep class com.tapadoo.alerter.** { *; }
-dontwarn com.tapadoo.alerter.**

# MultiDex support
-keep class androidx.multidex.** { *; }
-dontwarn androidx.multidex.**

# Misc dependency warnings
-dontwarn sun.misc.Unsafe
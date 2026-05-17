# Consumer ProGuard rules for signature-scanner
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
-keep interface com.unplugged.signature_scanner.SignatureScannerAccessPoint { *; }
-keep interface com.unplugged.signature_scanner.repository.AppRepository { *; }
-keep interface com.unplugged.signature_scanner.repository.BlacklistPackageRepository { *; }
-keep class com.unplugged.signature_scanner.repository.AppRepositoryImpl { *; }
-keep class com.unplugged.signature_scanner.model.** { *; }

# Keep scanner implementation classes
-keep class com.unplugged.signature_scanner.appscanner.PackageScanner {
    <init>(...);
    public *** startScan(...);
    public *** cancel(...);
    public *** isRunning(...);
}
-keep class com.unplugged.signature_scanner.appscanner.ScanPackageTask {
    <init>(...);
    public *** start(...);
    public *** cancel(...);
}

# ==== ROOM DATABASE ====
# Keep database entities and DAOs  
-keep class com.unplugged.signature_scanner.model.BlacklistPackageEntity { *; }
-keep interface com.unplugged.signature_scanner.database.BlacklistPackageDao { *; }

# Fix naming inconsistency - keep both variations
-keep interface com.unplugged.signature_scanner.database.BlacklistedPackageDao { *; }

# ==== DATA MAPPERS ====
# Keep mapper interfaces and implementations
-keep interface com.unplugged.signature_scanner.model.BlacklistMapper { *; }
-keep class com.unplugged.signature_scanner.model.DefaultBlacklistPackageMapper { *; }

# ==== THIRD-PARTY LIBRARIES ====
# OpenCSV for parsing blacklist files
-keep class com.opencsv.** { *; }
-keep class au.com.bytecode.opencsv.** { *; }
-dontwarn com.opencsv.**
-dontwarn au.com.bytecode.opencsv.**

# ==== GSON SERIALIZATION ====
-keep class com.google.gson.** { *; }
-keep class com.unplugged.signature_scanner.model.** {
    @com.google.gson.annotations.SerializedName <fields>;
    <init>();
    *** get*();
    *** set*(...);
}

# ==== RETROFIT NETWORKING ====
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ==== ROOM DATABASE ====
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class * extends androidx.room.RoomDatabase
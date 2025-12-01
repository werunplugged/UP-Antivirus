# ProGuard rules for signature-scanner module
# Security-focused obfuscation for malware signature scanning

# ==== AGGRESSIVE OBFUSCATION ====
-overloadaggressively
-repackageclasses 'sig'
-allowaccessmodification

# Hide source files completely for security
-renamesourcefileattribute ""
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable

# ==== PROJECT-SPECIFIC CLASSES ====
# Keep main access point interface
-keep interface com.unplugged.signature_scanner.SignatureScannerAccessPoint { *; }

# Keep specific Room database entities and DAOs
-keep class com.unplugged.signature_scanner.model.BlacklistPackageEntity { *; }
-keep interface com.unplugged.signature_scanner.database.BlacklistPackageDao { *; }

# Keep specific repository interfaces and implementations
-keep interface com.unplugged.signature_scanner.repository.AppRepository { *; }
-keep interface com.unplugged.signature_scanner.repository.BlacklistPackageRepository { *; }
-keep class com.unplugged.signature_scanner.repository.AppRepositoryImpl { *; }

# Keep blacklist mapper classes
-keep interface com.unplugged.signature_scanner.model.BlacklistMapper { *; }
-keep class com.unplugged.signature_scanner.model.DefaultBlacklistPackageMapper { *; }

# Keep scanner core classes
-keep class com.unplugged.signature_scanner.appscanner.ScanPackageTask {
    <init>(...);
    *** start(...);
    *** cancel(...);
}
-keep class com.unplugged.signature_scanner.appscanner.PackageScanner {
    <init>(...);
    *** scan(...);
}

# ==== THIRD PARTY LIBRARIES ====
# OpenCSV for blacklist parsing
-keep class com.opencsv.** { *; }
-keep class au.com.bytecode.opencsv.** { *; }
-dontwarn com.opencsv.**
-dontwarn au.com.bytecode.opencsv.**

# ==== GSON SERIALIZATION ====
-keep class com.google.gson.** { *; }
-keepattributes Signature
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

# ==== SECURITY HARDENING ====
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Suppress warnings
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
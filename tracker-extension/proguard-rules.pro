# ProGuard rules for tracker-extension module
# Security-focused obfuscation for tracker detection

# ==== AGGRESSIVE OBFUSCATION ====
-overloadaggressively
-repackageclasses 'trk'
-allowaccessmodification

# Hide source files completely for security
-renamesourcefileattribute ""
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable

# ==== PROJECT-SPECIFIC CLASSES ====
# Keep main tracker access point interface
-keep interface com.example.trackerextension.TrackersAccessPoint { *; }

# Keep tracker control and listener interfaces
-keep class com.example.trackerextension.TrackerControl {
    <init>(...);
    *** analyse(...);
    *** stop(...);
}
-keep interface com.example.trackerextension.TrackerListener { *; }

# ==== SECURITY HARDENING ====
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Suppress warnings
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
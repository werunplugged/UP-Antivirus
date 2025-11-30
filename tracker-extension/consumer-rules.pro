# Consumer ProGuard rules for tracker-extension
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
# Keep public API of tracker-extension that consuming apps need
-keep interface com.example.trackerextension.TrackersAccessPoint { *; }
-keep interface com.example.trackerextension.TrackerListener { *; }
-keep class com.example.trackerextension.TrackerControl {
    <init>(...);
    public *** analyse(...);
    public *** stop(...);
    public *** isRunning(...);
    public *** setRunning(...);
    public *** cancel(...);
}
-keep class com.example.trackerextension.TrackerModel { *; }
-keep class com.example.trackerextension.TrackerScanUpdate { *; }

# ==== PARCELABLE SUPPORT ====
# Preserve Parcelable implementation for TrackerModel
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}
-keep class com.example.trackerextension.TrackerModel implements android.os.Parcelable {
    *;
}

# ==== NATIVE LIBRARY INTEGRATION ====
# Keep tracker analysis library classes
-keep class net.kollnig.missioncontrol.analysis.TrackerLibraryAnalyser { *; }
-dontwarn net.kollnig.missioncontrol.**
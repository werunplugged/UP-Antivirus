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
-keep class com.unplugged.upantiviruscommon.model.** {
    @com.google.gson.annotations.SerializedName <fields>;
    *;
}
# ProGuard configuration for UP Antivirus - Main App Module
# Project-specific security-focused obfuscation rules

# ==== LOGGING REMOVAL ====
# Remove all Android logging for security - highly sensitive for antivirus app
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

# Remove project-specific custom logging (Utils.printLog)
-assumenosideeffects class com.unplugged.up_antivirus.base.Utils {
    public static void printLog(java.lang.Class, java.lang.String);
    public static java.lang.Boolean isDebugModeEnabled();
}

# Remove print statements
-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}

# Remove System logs and warnings
-assumenosideeffects class java.lang.System {
    public static java.io.PrintStream out;
    public static java.io.PrintStream err;
}

# Remove remaining logging frameworks
-assumenosideeffects class ** {
    *** println(...);
    *** printf(...);
    *** trace(...);
    *** debug(...);
    *** info(...);
    *** warn(...);
    *** error(...);
}

# ==== AGGRESSIVE SECURITY OBFUSCATION ====
-overloadaggressively
-repackageclasses 'a'
-allowaccessmodification
-mergeinterfacesaggressively

# Hide source files and debugging info for maximum security
-renamesourcefileattribute ""
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable

# ==== PROJECT-SPECIFIC CORE CLASSES ====
# Keep main Application class and its essential methods
-keep class com.unplugged.up_antivirus.common.AntivirusApp {
    void onCreate();
    void listenPackagesState();
    void unregisterReceiver();
}

# Keep essential Android framework extensions
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service {
    public <init>();
}
-keep public class * extends android.content.BroadcastReceiver {
    public <init>();
    void onReceive(android.content.Context, android.content.Intent);
}

# Keep Hilt Application
-keep @dagger.hilt.android.HiltAndroidApp class * {
    <init>();
}

# ==== ROOM DATABASE - PROJECT SPECIFIC ====
# Keep specific database classes
-keep class com.unplugged.up_antivirus.data.AntivirusRoomDatabase { *; }

# Keep all Entity classes and their fields
-keep class com.unplugged.up_antivirus.data.history.model.HistoryEntity { *; }
-keep class com.unplugged.up_antivirus.data.malware.model.MalwareEntity { *; }
-keep class com.unplugged.up_antivirus.data.tracker.model.TrackerEntity { *; }

# Keep DAO interfaces
-keep interface com.unplugged.up_antivirus.data.history.HistoryDao { *; }
-keep interface com.unplugged.up_antivirus.data.malware.MalwareDao { *; }
-keep interface com.unplugged.up_antivirus.data.tracker.TrackerDao { *; }

# ==== DEPENDENCY INJECTION - HILT SPECIFIC ====
-keep @dagger.hilt.android.AndroidEntryPoint class * {
    <init>(...);
    *** on*(...);
}

-keep @javax.inject.Inject class * { *; }
-keep class * {
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <init>(...);
}

# Keep ViewModels with Hilt
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ==== USE CASES AND REPOSITORIES ====
# Keep domain layer classes that might be used via reflection
-keep interface com.unplugged.up_antivirus.domain.** { *; }
-keep class com.unplugged.up_antivirus.domain.** { *; }
-keep interface com.unplugged.up_antivirus.scanner.repository.** { *; }
-keep class com.unplugged.up_antivirus.scanner.repository.** { *; }

# Keep all Use Case classes explicitly
-keep class com.unplugged.up_antivirus.domain.use_case.** { *; }

# Keep repository implementations and data sources
-keep class com.unplugged.up_antivirus.data.**.** { *; }

# ==== ACCOUNT INTEGRATION ====
# Keep account-related classes for UP account integration
-keep class com.unplugged.account.** { *; }
-keep class com.unplugged.accounthelper.AccountSubscription { *; }
-keep interface com.unplugged.up_antivirus.domain.account.AccountRepository { *; }
-keep interface com.unplugged.up_antivirus.data.account.AccountRemoteSource { *; }

# ==== NAVIGATION COMPONENTS ====
-keepnames class androidx.navigation.**
-keep class * extends androidx.fragment.app.Fragment {
    public <init>();
}

# ==== VIEW BINDING ====
-keep class com.unplugged.antivirus.databinding.** { *; }

# ==== WORK MANAGER - PROJECT SPECIFIC ====
-keep class com.unplugged.up_antivirus.workers.ScheduledScanWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.unplugged.up_antivirus.workers.ScannerWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
# Keep worker constants file (top-level declarations)
-keep class com.unplugged.up_antivirus.workers.WorkerUtilsKt { *; }

# ==== NETWORKING - GSON MODELS ====
-keep class com.unplugged.upantiviruscommon.model.** {
    @com.google.gson.annotations.SerializedName <fields>;
    *;
}

# ==== UI COMPONENTS ====
# Keep RecyclerView Adapters
-keep class com.unplugged.up_antivirus.ui.**.** { *; }

# Keep custom view components and decorations
-keep class com.unplugged.up_antivirus.ui.CellMarginDecoration { *; }

# Keep enum classes used in UI
-keep enum com.unplugged.up_antivirus.ui.settings.scheduler.ScheduleChoice { *; }

# ==== UTILITY CLASSES ====
# Keep extension functions file (top-level declarations)  
-keep class com.unplugged.up_antivirus.common.ExtensionsKt { *; }
-keep class com.unplugged.up_antivirus.common.StringProvider { *; }

# ==== SERVICES AND RECEIVERS ====
# Keep services explicitly (beyond generic rule)
-keep class com.unplugged.up_antivirus.data.receiver.PackageMonitorService {
    <init>();
    *** onCreate();
    *** onStartCommand(...);
    *** onDestroy();
}
-keep class com.unplugged.up_antivirus.ui.scan.ScanService {
    <init>();
}

# Keep broadcast receivers explicitly
-keep class com.unplugged.up_antivirus.data.receiver.PackageStateReceiver {
    <init>(...);
    *** onReceive(...);
}
-keep class com.unplugged.up_antivirus.ui.UpLogoutReceiver {
    <init>(...);
    *** onReceive(...);
}

# ==== THIRD PARTY LIBRARIES (PROJECT SPECIFIC) ====
# Glide
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Alerter for notifications
-keep class com.tapadoo.alerter.** { *; }

# CircularProgressBar
-keep class com.mikhaellopez.circularprogressbar.** { *; }

# MultiDexLib2 for tracker detection
-keep class com.github.lanchon.dexpatcher.multidexlib2.** { *; }
-dontwarn com.github.lanchon.dexpatcher.multidexlib2.**

# OkHttp logging interceptor
-keep class okhttp3.logging.** { *; }
-dontwarn okhttp3.logging.**

# Navigation components
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# ==== KOTLIN COROUTINES ====
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler
-keep class kotlinx.coroutines.** { *; }

# ==== SECURITY MEASURES ====
# Remove debugging attributes for release security
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Additional obfuscation for sensitive antivirus logic
-keep class com.unplugged.up_antivirus.base.BaseView { *; }
-keep class com.unplugged.up_antivirus.base.BaseActivity { *; }

# Warnings suppression
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
-dontwarn com.google.errorprone.annotations.**

# ==== R8 MISSING RULES ====
# Generated warnings suppression from missing_rules.txt
-dontwarn com.caverock.androidsvg.RenderOptions
-dontwarn com.caverock.androidsvg.SVG
-dontwarn com.caverock.androidsvg.SVGParseException
-dontwarn com.maxmind.db.CHMCache
-dontwarn com.maxmind.db.NodeCache
-dontwarn com.maxmind.geoip2.DatabaseReader$Builder
-dontwarn com.maxmind.geoip2.DatabaseReader
-dontwarn com.maxmind.geoip2.exception.GeoIp2Exception
-dontwarn com.maxmind.geoip2.model.CountryResponse
-dontwarn com.maxmind.geoip2.record.Country
-dontwarn com.sun.jna.platform.win32.Win32Exception
-dontwarn java.beans.BeanInfo
-dontwarn java.beans.IndexedPropertyDescriptor
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.PropertyDescriptor
-dontwarn java.beans.PropertyEditor
-dontwarn java.beans.PropertyEditorManager
-dontwarn javax.naming.NamingException
-dontwarn javax.naming.directory.DirContext
-dontwarn javax.naming.directory.InitialDirContext
-dontwarn javax.servlet.ServletContextListener
-dontwarn lombok.Generated
-dontwarn org.apache.avalon.framework.logger.Logger
-dontwarn org.apache.log.Hierarchy
-dontwarn org.apache.log.Logger
-dontwarn org.apache.log4j.Level
-dontwarn org.apache.log4j.Logger
-dontwarn org.apache.log4j.Priority
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMDCBinder
-dontwarn org.slf4j.impl.StaticMarkerBinder
-dontwarn sun.net.spi.nameservice.NameService
-dontwarn sun.net.spi.nameservice.NameServiceDescriptor
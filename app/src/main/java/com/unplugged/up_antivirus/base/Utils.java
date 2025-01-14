
package com.unplugged.up_antivirus.base;

import android.util.Log;

import com.unplugged.antivirus.BuildConfig;

public class Utils {
    /***
     *
     * @param classRef accepts the class and fetch the simpleName of it
     * @param message accepts the string message
     *                printLog(Class<?> classRef, String message) method created in Utils.java to support debug logs printing and only when we run app in debug mode to secure the logs from production based on value return from isDebugModeEnabled().
     */
    public static void printLog(Class<?> classRef, String message) {
        if (isDebugModeEnabled()) {
            Log.d(classRef.getSimpleName(), "message: " + message);
        }
    }

    /***
     * isDebugModeEnabled() method created in Utils.java to return if app is running in debug mode, it is created to debug the app in production also by running from Android Studio and marking its value to true even the app is running in release mode
     */
    public static Boolean isDebugModeEnabled() {
        return BuildConfig.DEBUG;
    }
}

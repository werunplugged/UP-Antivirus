package com.unplugged.up_antivirus.data.preferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson
import com.unplugged.upantiviruscommon.model.ScanParams
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPreferencesSource @Inject constructor (@ApplicationContext context: Context) {

    private val gson = Gson()

    private val preferencesName = "com.unplugged.antivirus.preferences"
    private val shouldShowOnBoardingScreenKey = "should_show_on_boarding"
    private val lastActivityTime = "last_activity"
    private val scanPreferencesKey = "scan_preferences"
    private val lastBlacklistPackageUpdateTime = "last_blacklist_package_update_time"
    private val scheduledScanningIntervalKey = "scheduledScanningIntervalKey"
    private val scheduledDatabaseUpdatingIntervalKey = "scheduledDatabaseUpdatingIntervalKey"
    private val realTimeProtectionKey = "realTimeProtection"
    private val prefs = context.getSharedPreferences(preferencesName, MODE_PRIVATE)


    fun getScheduledScanningInterval(): Int {
        return prefs.getInt(scheduledScanningIntervalKey, -1)
    }

    fun setScheduledScanningInterval(choice: Int) {
        prefs.edit().putInt(scheduledScanningIntervalKey, choice).apply()
    }
    fun getScheduledDatabaseUpdatingInterval(): Int {
        return prefs.getInt(scheduledDatabaseUpdatingIntervalKey, -1)
    }

    fun setScheduledDatabaseUpdatingInterval(choice: Int) {
        prefs.edit().putInt(scheduledDatabaseUpdatingIntervalKey, choice).apply()
    }

    fun getSetShouldShowOnBoardingScreen(setShouldShow: Boolean): Boolean {
        val tempValue = prefs.getBoolean(shouldShowOnBoardingScreenKey, true)
        setShouldShowOnBoardingScreen(setShouldShow)
        return tempValue
    }

    fun setShouldShowOnBoardingScreen(shouldShow: Boolean) {
        prefs.edit().putBoolean(shouldShowOnBoardingScreenKey, shouldShow).apply()
    }

    fun setLastActivitySentTime(timestamp: Long?) {
        prefs.edit().putLong(lastActivityTime, timestamp ?: -1).apply()
    }

    fun getLastActivitySentTime(): Long {
        return prefs.getLong(lastActivityTime, -1)
    }

    fun getScanPreferences(): ScanParams {
        val json = prefs.getString(scanPreferencesKey, null)
        return if (json == null)
            return ScanParams()
        else
            gson.fromJson(json, ScanParams::class.java)
    }

    fun setScanPreferences(scanParams: ScanParams) {
        val json = gson.toJson(scanParams)
        prefs.edit().putString(scanPreferencesKey, json).apply()
    }

    fun setLastBlacklistPackageUpdateTime(milliseconds: Long) {
        prefs.edit().putLong(lastBlacklistPackageUpdateTime, milliseconds).apply()
    }

    fun getLastBlacklistPackageUpdateTime(): Long {
        return prefs.getLong(lastBlacklistPackageUpdateTime, 0)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun setRealTimeProtectionValue(enable: Boolean){
        prefs.edit().putBoolean(realTimeProtectionKey, enable).apply()
    }

    fun getRealTimeProtectionValue(): Boolean{
        return prefs.getBoolean(realTimeProtectionKey, true)
    }

    fun saveData(key: String, value: Any) {
        val editor = prefs.edit()
        when(value){
            is Int -> {editor.putInt(key, value)}
            is String -> {editor.putString(key, value)}
            is Boolean -> {editor.putBoolean(key, value)}
            is Long -> {editor.putLong(key, value)}
            is Float -> {editor.putFloat(key, value)}
        }
        editor.apply()
    }

    fun loadDataVersion(key: String): Int {
        return prefs.getInt(key, -1)
    }

    fun getAllowCellularDownload(key: String): Boolean {
        return prefs.getBoolean(key, false)
    }

    fun isNewAppDatabase(key: String): Boolean{
        return prefs.getBoolean(key, false)
    }
}
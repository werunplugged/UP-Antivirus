package com.unplugged.up_antivirus.domain.preferences

import com.unplugged.upantiviruscommon.model.ScanParams


interface PreferencesRepository {
    fun getShouldShowOnBoardingScreen(setShouldShow: Boolean): Boolean
    fun setShouldShowOnBoardingScreen(shouldShow: Boolean)
    fun getScheduledScanningInterval(): Int
    fun setScheduledScanningInterval(choice: Int)
    fun getScheduledDatabaseUpdatingInterval(): Int
    fun setScheduledDatabaseUpdatingInterval(choice: Int)
    fun getScanParams(): ScanParams
    fun setScanParams(scanParams: ScanParams)
    fun setBlacklistPackageLastUpdateTime(milliseconds: Long)
    fun getBlacklistPackageLastUpdateTime(): Long
    fun setRealTimeProtectionValue(enable: Boolean)
    fun getRealTimeProtectionValue(): Boolean
    fun saveData(key: String, value: Any)
    fun loadDataVersion(key: String): Int
    fun setAllowCellularDownload(value: Boolean)
    fun getAllowCellularDownload(): Boolean
    fun isNewAppDatabase(key: String): Boolean
}
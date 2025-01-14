package com.unplugged.up_antivirus.data.preferences

import com.unplugged.upantiviruscommon.model.ScanParams
import com.unplugged.up_antivirus.domain.preferences.PreferencesRepository
import javax.inject.Inject

class DefaultPreferencesRepository @Inject constructor(
    private val preferencesSource: SharedPreferencesSource
) : PreferencesRepository {

    private val DOWNLOAD_OVER_CELLULAR_KEY = "downloadOverCellular"

    override fun getShouldShowOnBoardingScreen(setShouldShow: Boolean): Boolean {
        return preferencesSource.getSetShouldShowOnBoardingScreen(setShouldShow)
    }

    override fun setShouldShowOnBoardingScreen(shouldShow: Boolean) {
        preferencesSource.setShouldShowOnBoardingScreen(shouldShow)
    }

    override fun getScheduledScanningInterval(): Int {
        return preferencesSource.getScheduledScanningInterval()
    }

    override fun setScheduledScanningInterval(choice: Int) {
        preferencesSource.setScheduledScanningInterval(choice)
    }
    override fun getScheduledDatabaseUpdatingInterval(): Int {
        return preferencesSource.getScheduledScanningInterval()
    }

    override fun setScheduledDatabaseUpdatingInterval(choice: Int) {
        preferencesSource.setScheduledScanningInterval(choice)
    }

    override fun setScanParams(scanParams: ScanParams) {
        preferencesSource.setScanPreferences(scanParams)
    }

    override fun getScanParams(): ScanParams {
        return preferencesSource.getScanPreferences()
    }

    override fun setBlacklistPackageLastUpdateTime(milliseconds: Long) {
        preferencesSource.setLastBlacklistPackageUpdateTime(milliseconds)
    }

    override fun getBlacklistPackageLastUpdateTime(): Long {
        return preferencesSource.getLastBlacklistPackageUpdateTime()
    }

    override fun setRealTimeProtectionValue(enable: Boolean) {
        preferencesSource.setRealTimeProtectionValue(enable)
    }

    override fun getRealTimeProtectionValue(): Boolean {
        return preferencesSource.getRealTimeProtectionValue()
    }

    override fun saveData(key: String, value: Any) {
        preferencesSource.saveData(key, value)
    }

    override fun loadDataVersion(key: String): Int {
        return preferencesSource.loadDataVersion(key)
    }

    override fun setAllowCellularDownload(value: Boolean) {
        preferencesSource.saveData(DOWNLOAD_OVER_CELLULAR_KEY, value)
    }

    override fun getAllowCellularDownload(): Boolean {
        return preferencesSource.getAllowCellularDownload(DOWNLOAD_OVER_CELLULAR_KEY)
    }

    override fun isNewAppDatabase(key: String): Boolean {
        return preferencesSource.isNewAppDatabase(key)
    }
}
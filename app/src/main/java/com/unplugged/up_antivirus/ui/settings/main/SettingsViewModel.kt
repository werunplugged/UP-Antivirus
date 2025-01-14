package com.unplugged.up_antivirus.ui.settings.main

import androidx.lifecycle.ViewModel
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.common.StringProvider
import com.unplugged.up_antivirus.domain.preferences.PreferencesRepository
import com.unplugged.up_antivirus.domain.use_case.LogoutUseCase
import com.unplugged.up_antivirus.ui.settings.SettingsRepository
import com.unplugged.up_antivirus.ui.settings.legals.LegalItem
import com.unplugged.up_antivirus.ui.settings.scheduler.ScheduleChoice
import com.unplugged.up_antivirus.workers.ScheduledScanWorker
import com.unplugged.up_antivirus.workers.UNIQUE_SCHEDULED_SCAN_WORKER_REQUEST
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val stringProvider: StringProvider,
    private val settingsRepository: SettingsRepository,
    private val logoutUseCase: LogoutUseCase,
    private val workManager: WorkManager,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    fun getScheduledScanningIntervalString(interval: Int): String {
        return when (ScheduleChoice.fromValue(interval)) {
            ScheduleChoice.Daily -> stringProvider.getString(R.string.up_av_daily)
            ScheduleChoice.ThreeTimesAWeek -> stringProvider.getString(R.string.up_av__3_times_a_week)
            ScheduleChoice.Weekly -> stringProvider.getString(R.string.up_av_weekly)
            ScheduleChoice.Disabled -> stringProvider.getString(R.string.up_av_disable)
        }
    }

    fun getSettingsList(): List<String> {
        return settingsRepository.settingsList
    }

    fun getLegalsSettingsList(): List<LegalItem> {
        return settingsRepository.legalsList
    }

    fun getScheduledScanningInterval(): Int {
        return preferencesRepository.getScheduledScanningInterval()
    }

    fun scheduleScanning(choice: Int) {
        preferencesRepository.setScheduledScanningInterval(choice)

        if (choice == ScheduleChoice.Disabled.ordinal) {
            workManager.cancelAllWorkByTag(UNIQUE_SCHEDULED_SCAN_WORKER_REQUEST)
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType = NetworkType.CONNECTED)
            .build()

        workManager.cancelAllWorkByTag(UNIQUE_SCHEDULED_SCAN_WORKER_REQUEST)

        //create periodic work request
        val workRequest: PeriodicWorkRequest = PeriodicWorkRequest.Builder(
            ScheduledScanWorker::class.java,
            1,
            TimeUnit.DAYS,
            PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS,
            TimeUnit.MILLISECONDS
        ).setConstraints(constraints)
            .addTag(UNIQUE_SCHEDULED_SCAN_WORKER_REQUEST)
            .build()

        //start the work
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_SCHEDULED_SCAN_WORKER_REQUEST,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun setRealTimeProtection(enable: Boolean) {
        preferencesRepository.setRealTimeProtectionValue(enable)
    }

    fun getRealTimeProtection(): Boolean {
        return preferencesRepository.getRealTimeProtectionValue()
    }

    fun setDownloadOverCellular(value: Boolean) {
        preferencesRepository.setAllowCellularDownload(value)
    }

    fun getDownloadOverCellular(): Boolean {
        return preferencesRepository.getAllowCellularDownload()
    }
}
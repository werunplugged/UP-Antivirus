package com.unplugged.up_antivirus.domain.use_case

import androidx.work.WorkManager
import com.unplugged.up_antivirus.common.notifications.NotificationManager
import com.unplugged.upantiviruscommon.model.ScanParams
import com.unplugged.up_antivirus.domain.preferences.PreferencesRepository
import javax.inject.Inject

class GetScanPreferencesUseCase @Inject constructor(private val preferencesRepository: PreferencesRepository, private val notificationManager: NotificationManager, private val workManager: WorkManager,
) {

    operator fun invoke(): ScanParams {
        return preferencesRepository.getScanParams()
    }

    fun getPreferencesRepository(): PreferencesRepository{
        return preferencesRepository
    }

    fun getNotificationManager(): NotificationManager{
        return notificationManager
    }

    fun getWorkManager(): WorkManager{
        return workManager
    }
}
package com.unplugged.up_antivirus.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.unplugged.up_antivirus.base.Utils
import com.unplugged.up_antivirus.common.AntivirusApp
import com.unplugged.up_antivirus.domain.use_case.GetScanPreferencesUseCase
import com.unplugged.up_antivirus.ui.settings.scheduler.ScheduleChoice
import java.util.Calendar
import javax.inject.Inject

class ScheduledScanWorker @Inject constructor(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Utils.printLog(ScheduledScanWorker::class.java, "start of doWork()")

        val scheduleChoiceValue = getScanPreferencesUseCase().getPreferencesRepository().getScheduledScanningInterval()
        val scheduleChoice = ScheduleChoice.fromValue(scheduleChoiceValue)

        when (scheduleChoice) {
            ScheduleChoice.Daily -> {
                startScan()
            }

            ScheduleChoice.ThreeTimesAWeek -> {
                val cal = Calendar.getInstance()
                val dayOfWeek = cal[Calendar.DAY_OF_WEEK]
                if (dayOfWeek == Calendar.MONDAY ||
                    dayOfWeek == Calendar.WEDNESDAY ||
                    dayOfWeek == Calendar.FRIDAY
                ) {
                    startScan()
                }
            }

            ScheduleChoice.Weekly -> {
                val cal = Calendar.getInstance()
                if (cal[Calendar.DAY_OF_WEEK] == Calendar.MONDAY) {
                    startScan()
                }
            }

            ScheduleChoice.Disabled -> {
                getScanPreferencesUseCase().getNotificationManager().dismissNotification()
            }
        }
        Utils.printLog(ScheduledScanWorker::class.java, "end of doWork()")
        return Result.success()
    }

    private fun startScan() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val scannerWorkerRequest = OneTimeWorkRequestBuilder<ScannerWorker>()
            .setConstraints(constraints)
            .build()

        getScanPreferencesUseCase().getWorkManager().beginUniqueWork(
            UNIQUE_SCAN_WORKER_REQUEST,
            ExistingWorkPolicy.REPLACE,
            scannerWorkerRequest
        ).enqueue()
    }

    private fun getScanPreferencesUseCase(): GetScanPreferencesUseCase {
        return (context.applicationContext as AntivirusApp).getScanPreferencesUseCase
    }
}
package com.unplugged.up_antivirus.workers

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.common.AntivirusApp
import com.unplugged.up_antivirus.domain.use_case.UpdateDatabaseUseCase
import com.unplugged.up_antivirus.ui.scan.ScanService
import com.unplugged.upantiviruscommon.model.Connectivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ScannerWorker @Inject constructor(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private var availableDatabase = false

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                val updateDatabaseUseCase = getUpdateDatabaseUseCase()

                if (newAppDatabase()) {
                    if (isNewVersionAvailable()) {
                        when (updateDatabaseUseCase.internetConnectivity()) {
                            Connectivity.WIFI -> {
                                updateDatabaseUseCase.performUpdate()
                                availableDatabase = true
                            }
                            Connectivity.CELLULAR -> {
                                if (updateDatabaseUseCase.getAllowDownloadOverCellular()) {
                                    updateDatabaseUseCase.performUpdate()
                                    updateDatabaseUseCase.setAllowDownloadOverCellular(false)
                                    availableDatabase = true
                                }
                            }
                            Connectivity.NONE -> {
                                availableDatabase = true
                            }
                        }
                    } else {
                        availableDatabase = true
                    }
                } else {
                    when (updateDatabaseUseCase.internetConnectivity()) {
                        Connectivity.WIFI -> {
                            if(updateDatabaseUseCase.performUpdate()){
                                availableDatabase = true
                            }
                        }
                        Connectivity.CELLULAR -> {
                            if (updateDatabaseUseCase.getAllowDownloadOverCellular()) {
                                if(updateDatabaseUseCase.performUpdate()){
                                    availableDatabase = true
                                } else {
                                    updateDatabaseUseCase.errorNotification(
                                        context.getString(R.string.up_av_scan_not_performed),
                                        context.getString(R.string.up_av_database_unavailable_an_error_occurred_while_downloading_the_required_files)
                                    )
                                    availableDatabase = false
                                }
                                updateDatabaseUseCase.setAllowDownloadOverCellular(false)
                            } else {
                                updateDatabaseUseCase.errorNotification(
                                    context.getString(R.string.up_av_scan_not_performed),
                                    context.getString(R.string.up_av_database_unavailable_cellular_data_usage_is_disabled_for_downloads)
                                )
                                availableDatabase = false
                            }
                        }

                        Connectivity.NONE -> {
                            updateDatabaseUseCase.errorNotification(
                                context.getString(R.string.up_av_scan_not_performed),
                                context.getString(R.string.up_av_database_download_failed_no_internet_connection_available)
                            )
                            availableDatabase = false
                        }
                    }
                }

                if (!updateDatabaseUseCase.isDatabaseLoaded() && availableDatabase) {
                    updateDatabaseUseCase.loadDatabase()
                }

                if (availableDatabase) {
                    startScanService()
                    Result.success(prepareOutputData("success"))
                } else {
                    Result.failure(prepareOutputData("No database available"))
                }
            }
        } catch (e: Exception) {
            Result.failure(prepareOutputData("$e"))
        }
    }

    private fun newAppDatabase(): Boolean {
        return getUpdateDatabaseUseCase().isNewAppDatabase()
    }

    private fun prepareOutputData(message: String): Data {
        return Data.Builder()
            .putString(WORKER_RESULT, message)
            .build()
    }

    private fun startScanService() {
        val intent = Intent(context, ScanService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private suspend fun isNewVersionAvailable(): Boolean {
        return getUpdateDatabaseUseCase().checkHypatiaDatabaseVersion()
    }

    private fun getUpdateDatabaseUseCase(): UpdateDatabaseUseCase {
        return (context.applicationContext as AntivirusApp).updateDatabaseUseCase
    }
}

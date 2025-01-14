package com.unplugged.signature_scanner.appscanner

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.unplugged.signature_scanner.R
import com.unplugged.upantiviruscommon.model.AppListState
import com.unplugged.signature_scanner.repository.AppRepository
import com.unplugged.upantiviruscommon.malware.MalwareModel
import com.unplugged.upantiviruscommon.malware.MalwareScannerListener
import com.unplugged.upantiviruscommon.malware.ScanMessage
import com.unplugged.upantiviruscommon.malware.ScanStats
import com.unplugged.upantiviruscommon.malware.ThreatStatus
import com.unplugged.upantiviruscommon.model.ScannerType

class ScanPackageTask(private val context: Context, val appRepository: AppRepository) {

    private var canceled = false
    private var running = false

    fun isRunning() = running

    private var scanMessage = ScanMessage("", ScannerType.BLACKLIST)
    suspend fun getAllInstalledApps(listener: MalwareScannerListener): AppListState {
        return appRepository.getInstalledApps(listener)
    }

    /**
     * Fetch all apps on device, loads DB of black-listed apps from json file,
     * compares and return those who appears on both lists.
     */
    suspend fun searchBlacklistedAppsOnDevice(listener: MalwareScannerListener) {
        canceled = false
        running = true

        Log.d("AppScannerUseCase", "start comparing apps, isCanceled: $canceled")
        scanMessage.message = context.getString(R.string.up_av_loading_device_applications)
        listener.malwareProgressMessage(scanMessage)
        val deviceApps = getAllInstalledApps(listener).let { it.systemApps + it.userApps }
        Log.d("AppScannerUseCase", "loaded device apps: ${deviceApps.size}")
        if (canceled) {
            onCancelDetected()
            return
        }

        scanMessage.message = context.getString(R.string.up_av_loading_blacklisted_applications_database)
        listener.malwareProgressMessage(scanMessage)
        if (canceled) {
            onCancelDetected()
            return
        }

        if (deviceApps.isNotEmpty()) {
            deviceApps.forEachIndexed { index, it ->
                if (canceled) {
                    onCancelDetected()
                    return
                }
                scanMessage.message = context.getString(R.string.up_av_scanning, it.packageName)
                listener.malwareProgressMessage(scanMessage)

                val progress = ((index + 1) / deviceApps.size.toDouble()) * 100.0
                listener.malwareOnProgress(progress, 100, ScannerType.BLACKLIST)

                if (appRepository.isBlacklistedApp(it.apkInfo)) {
                    listener.onMalwareDetected(
                        MalwareModel(
                            id = 0,
                            scanId = 0,
                            name = it.packageName,
                            description = it.name,
                            filePath = MalwareModel.BLACK_LIST_PACKAGE,
                            status = ThreatStatus.EXIST
                        )
                    )
                }
            }
            listener.malwareOnFinish(ScanStats(-1, -1, deviceApps.size, -1, false, ScannerType.BLACKLIST))
        } else {
            // If no apps to check, set progress to 100 and format.
            listener.malwareOnProgress(100.0, 100, ScannerType.BLACKLIST)
            listener.malwareOnFinish(ScanStats(-1, -1, deviceApps.size, -1, false, ScannerType.BLACKLIST))
        }

        running = false

        Log.d("AppScannerUseCase", "finish comparing apps, isCanceled: $canceled")
    }

    private fun onCancelDetected() {
        running = false
    }

    fun cancel() {
        Log.d("AppScannerUseCase", "cancel called")
        canceled = true
    }

    suspend fun searchBlacklistedAppsOnDevice(listener: MalwareScannerListener, packageName: String) {
        canceled = false

        val scanStartTime = SystemClock.elapsedRealtime()
        scanMessage.message = context.getString(R.string.up_av_loading_device_applications)
        listener.malwareProgressMessage(scanMessage)
        val deviceApp = appRepository.getInstalledApp(packageName)

        if (deviceApp != null) {
            scanMessage.message = context.getString(R.string.up_av_loading_blacklisted_applications_database)
            listener.malwareProgressMessage(scanMessage)

            if (canceled) {
                return
            }

            if (appRepository.isBlacklistedApp(deviceApp.apkInfo)) {
                listener.onMalwareDetected(
                    MalwareModel(
                        id = 0,
                        scanId = 0,
                        name = deviceApp.packageName,
                        description = deviceApp.name,
                        filePath = MalwareModel.BLACK_LIST_PACKAGE,
                        status = ThreatStatus.EXIST
                    )
                )
            }
        }
        val secondsSpent = (SystemClock.elapsedRealtime() - scanStartTime) / 1000L
        listener.malwareOnFinish(ScanStats(secondsSpent, -1, 1, -1, false, ScannerType.BLACKLIST))
    }
}
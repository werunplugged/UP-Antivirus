package com.unplugged.up_antivirus.data.receiver

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.example.trackerextension.Tracker
import com.example.trackerextension.TrackerListener
import com.example.trackerextension.TrackerModel
import com.example.trackerextension.TrackerScanUpdate
import com.example.trackerextension.TrackersAccessPoint
import com.unplugged.antivirus.R
import com.unplugged.signature_scanner.appscanner.PackageScanner
import com.unplugged.signature_scanner.appscanner.ScanPackageTask
import com.unplugged.up_antivirus.common.notifications.NotificationManager
import com.unplugged.upantiviruscommon.utils.DateTimeUtils
import com.unplugged.up_antivirus.data.tracker.model.TrackerListConverter
import com.unplugged.up_antivirus.data.history.model.HistoryModel
import com.unplugged.up_antivirus.domain.use_case.HistoryActionsUseCase
import com.unplugged.up_antivirus.domain.use_case.SaveMalwareUseCase
import com.unplugged.up_antivirus.domain.use_case.SaveTrackerUseCase
import com.unplugged.upantiviruscommon.malware.MalwareModel
import com.unplugged.upantiviruscommon.malware.MalwareScannerListener
import com.unplugged.upantiviruscommon.malware.ScanMessage
import com.unplugged.upantiviruscommon.malware.ScanStats
import com.unplugged.upantiviruscommon.model.ScannerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

class CheckAppTask @Inject constructor(
    private val scanPackageTask: ScanPackageTask,
    private var historyActionsUseCase: HistoryActionsUseCase,
    private var saveMalwareUseCase: SaveMalwareUseCase,
    private var saveTrackerUseCase: SaveTrackerUseCase,
    private val trackerAccessPoint: TrackersAccessPoint,
    private val notificationManager: NotificationManager
) {
    private lateinit var malwareScanner: PackageScanner
    private lateinit var trackerModel: TrackerModel

    var malwareList = mutableListOf<MalwareModel>()
    var trackersFound = listOf<Tracker>()

    private lateinit var context: Context

    private var historyItemId = 0

    fun checkApp(context: Context, packageName: String, showResultNotification: Boolean) {
        Log.d("CheckAppTask", "app installed: -> $packageName")

        this.context = context

        if (isSystemApp(context, packageName) || packageName == "com.unplugged.messenger") return
        malwareScanner = PackageScanner(context, scanPackageTask.appRepository)

        CoroutineScope(Dispatchers.Default).launch {
            scanTrackers(packageName, showResultNotification)
        }
    }

    private suspend fun scanTrackers(
        packageName: String,
        showResultNotification: Boolean
    ) {
        // Define tracker scan listener
        val trackerListener = object : TrackerListener {
            override fun onTrackerFound(tracker: TrackerScanUpdate) {
                tracker.let {
                    trackersFound = it.upTracker.trackers
                }

                val appName = getApplicationName(packageName)!!
                val trackers = TrackerListConverter().fromTrackerList(trackersFound)

                trackerModel = TrackerModel(0, -1, appName, tracker.upTracker.appId, trackers)

                Log.d("CheckAppTask", "onTrackerFound, trackers: $trackersFound")
            }

            override fun trackerOnFinish() {
                Log.d("CheckAppTask", "trackerOnFinish")

                CoroutineScope(Dispatchers.Default).launch {
                    scanMalware(malwareScanner, packageName, showResultNotification)
                }
            }

            override fun trackerOnProgress(progress: Double) {
                // No need to use trackerOnProgress since this is all done in the background
            }
        }

        trackerAccessPoint.analyse(listOf(packageName), trackerListener)
    }

    private suspend fun scanMalware(
        packageScanner: PackageScanner,
        packageName: String,
        showResultNotification: Boolean
    ) {
        // Define malware scan listener
        val malwareListener = object : MalwareScannerListener {
            override fun malwareProgressMessage(scanMessage: ScanMessage) {
                Log.d("CheckAppTask", "malwareProgressMessage: ${scanMessage.message}")
            }

            override fun malwareOnProgress(progress: Double, total: Long, type: ScannerType) {
                // No need to use malwareOnProgress since this is all done in the background
            }

            override fun onMalwareDetected(malware: MalwareModel) {
                Log.d("CheckAppTask", "onMalwareDetected: $malware")
                malwareList.add(malware)
            }

            override fun malwareOnFinish(stats: ScanStats) {
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d("CheckAppTask", "malwareOnFinish: $stats")

                    if (malwareList.isNotEmpty() || trackersFound.isNotEmpty()) {
                        historyItemId = saveHistoryItem(stats, malwareList.size, trackersFound.size)
                        stats.historyItemId = historyItemId

                        trackerModel.scanId = historyItemId

                        malwareList.map {
                            if (historyItemId != 0) {
                                it.scanId = historyItemId
                            }
                        }

                        saveMalwareUseCase(malwareList)
                        saveTrackerUseCase(trackerModel)
                    }

                    Log.d("CheckAppTask", "malwareOnFinish, malware: $malwareList")
                    if (showResultNotification && historyItemId != 0) {
                        Log.d("notificationsError", "malwareOnFinish: here")
                        showResultNotification(malwareList.size, trackersFound.size, packageName, historyItemId)
                    }

                    clearLists()
                }
            }

            override fun malwareOnError(error: String?) {
                // Handle error if needed
            }
        }

        packageScanner.startScan(malwareListener, packageName)
    }

    private fun clearLists(){
        Log.d("notificationsError", "clearLists: here")
        trackersFound = emptyList()
        malwareList.clear()
        historyItemId = 0
    }

    private fun generateRandom5DigitNumber(): Int {
        return Random.nextInt(10001, 99999)
    }

    private fun showResultNotification(
        malwareFoundCount: Int,
        trackersFoundCount: Int,
        packageName: String,
        historyItemId: Int
    ) {
        val appName = getApplicationName(packageName)

        // Decide notification content based on scan results
        val notificationContent =
            if (malwareFoundCount == 0 && trackersFoundCount == 0) {
                context.getString(R.string.up_av_notification_body_no_threats_detected)
            } else {
                context.getString(
                    R.string.up_av_notification_body_threats_detected,
                    malwareFoundCount,
                    trackersFoundCount
                )
            }

        val notificationId = generateRandom5DigitNumber()

        // Show the notification
        notificationManager.showNotification(
            notificationId,
            historyItemId,
            context.getString(R.string.up_av_notification_head_app_scanned, appName),
            notificationContent
        )
    }

    private fun getApplicationName(packageName: String?): String? {
        if (packageName == null) return null

        val appInfo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } else {
                context.packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA
                )
            }

        return context.packageManager.getApplicationLabel(appInfo).toString()
    }

    private suspend fun saveHistoryItem(
        scanStats: ScanStats,
        malwareFound: Int,
        trackersFound: Int
    ): Int {
        val dateTimeString = DateTimeUtils.getCurrentDateTimeString()
        val historyItem = HistoryModel(
            0,
            context.getString(R.string.up_av_app_installation_scan_s),
            dateTimeString,
            malwareFound,
            trackersFound,
            0,
            1,
            scanStats.megabytesHashed
        )

        return historyActionsUseCase(historyItem)
    }

    private fun isSystemApp(context: Context, packageName: String): Boolean {
        val packageManager = context.packageManager
        return try {
            val applicationInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
            } else {
                packageManager.getApplicationInfo(packageName, 0)
            }
            (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: PackageManager.NameNotFoundException) {
            // Handle the case where the package name is not found
            e.printStackTrace()
            false // Or throw an exception, depending on your needs
        }
    }
}
package com.unplugged.up_antivirus.scanner.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.example.trackerextension.TrackerListener
import com.example.trackerextension.TrackerModel
import com.example.trackerextension.TrackerScanUpdate
import com.example.trackerextension.TrackersAccessPoint
import com.unplugged.antivirus.R
import com.unplugged.hypatia_extensions.HypatiaAccessPoint
import com.unplugged.hypatia_extensions.HypatiaMalwareMapper
import com.unplugged.signature_scanner.SignatureScannerAccessPoint
import com.unplugged.up_antivirus.common.StringProvider
import com.unplugged.up_antivirus.common.notifications.NotificationManager
import com.unplugged.upantiviruscommon.utils.DateTimeUtils
import com.unplugged.up_antivirus.data.tracker.model.TrackerListConverter
import com.unplugged.upantiviruscommon.model.Resource
import com.unplugged.upantiviruscommon.model.ScanUpdate
import com.unplugged.up_antivirus.data.history.model.HistoryModel
import com.unplugged.up_antivirus.domain.use_case.CancelScanningUseCase
import com.unplugged.up_antivirus.domain.use_case.GetApplicationInfoUseCase
import com.unplugged.up_antivirus.domain.use_case.SaveHistoryUseCase
import com.unplugged.up_antivirus.domain.use_case.SaveMalwareUseCase
import com.unplugged.up_antivirus.domain.use_case.SaveTrackerUseCase
import com.unplugged.up_antivirus.domain.use_case.UpdateScanDoneUseCase
import com.unplugged.upantiviruscommon.model.ScanParams
import com.unplugged.upantiviruscommon.model.ScanProgress
import com.unplugged.upantiviruscommon.malware.ScanMessage
import com.unplugged.upantiviruscommon.malware.MalwareModel
import com.unplugged.upantiviruscommon.malware.MalwareScannerListener
import com.unplugged.upantiviruscommon.malware.ScanStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import us.spotco.malwarescanner.MalwareScanner
import us.spotco.malwarescanner.malware.HypatiaMalwareScannerListener
import javax.inject.Inject
import android.os.Handler
import android.os.Looper
import com.unplugged.signature_scanner.repository.AppRepository
import com.unplugged.up_antivirus.domain.use_case.GetUserAppListForTrackersUseCase
import com.unplugged.up_antivirus.domain.use_case.StopScanServiceUseCase
import com.unplugged.upantiviruscommon.model.ScannerType
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.withContext


class DefaultScannerRepository @Inject constructor(
    private val hypatia: HypatiaAccessPoint,
    private val trackers: TrackersAccessPoint,
    private val blacklist: SignatureScannerAccessPoint,
    private val stringProvider: StringProvider,
    private val saveHistoryUseCase: SaveHistoryUseCase,
    private val updateScanDoneUseCase: UpdateScanDoneUseCase,
    private val saveMalwareUseCase: SaveMalwareUseCase,
    private var saveTrackerUseCase: SaveTrackerUseCase,
    private val stopScanServiceUseCase: StopScanServiceUseCase,
    private var getApplicationInfoUseCase: GetApplicationInfoUseCase,
    private val getUserAppListForTrackersUseCase: GetUserAppListForTrackersUseCase,
    private val notificationManager: NotificationManager,
    private val appRepository: AppRepository
) : ScannerRepository, MalwareScannerListener, HypatiaMalwareScannerListener {

    private var hypatiaScanner: MalwareScanner? = hypatia.getMalwareScanner(this)
    private val malwareList = mutableListOf<MalwareModel>()
    private var trackersFound = 0
    private lateinit var scanJob: Job
    private var scanProgress = MutableLiveData<ScanProgress>()
    val observableScanProgress = scanProgress.distinctUntilChanged()

    private var scanStatusSharedFlow = MutableSharedFlow<Resource<ScanUpdate>>()

    private var hypatiaProgress: Double = 0.0
    private var blacklistProgress: Double = 0.0
    private var trackersProgress: Double = 0.0

    private var historyItemId = 0
    private var isQuickScan = true
    private var updatedScanStats = ScanStats(-1, -1, -1, historyItemId, false, ScannerType.NONE)

    override suspend fun startScan(scanParams: ScanParams) {
        isQuickScan = scanParams.isQuickScan
        hypatiaProgress = 0.0
        blacklistProgress = 0.0
        trackersProgress = 0.0
        malwareOnProgress(0.0, 100, ScannerType.HYPATIA)
        malwareOnProgress(0.0, 100, ScannerType.BLACKLIST)
        malwareOnProgress(0.0, 100, ScannerType.TRACKERS)

        scanJob = CoroutineScope(Dispatchers.IO).launch {
            val jobs = mutableListOf<Job>()

            jobs.add(CoroutineScope(Dispatchers.IO).launch {
                hypatiaScanner = null
                hypatiaScanner = hypatia.getMalwareScanner(this@DefaultScannerRepository)
                hypatia.startScan(isQuickScan)
            })

            jobs.add(CoroutineScope(Dispatchers.IO).launch {
                blacklist.startScan(this@DefaultScannerRepository)
            })

            jobs.add(CoroutineScope(Dispatchers.IO).launch {
                trackers.setRunning(true)
                val nonSystemAppsIds =
                    getUserAppListForTrackersUseCase(this@DefaultScannerRepository)
                malwareProgressMessage(
                    ScanMessage(
                        stringProvider.getString(R.string.up_av_finished_loading_apps_starting_scan),
                        ScannerType.TRACKERS
                    )
                )
                trackerStartScan(nonSystemAppsIds)
            })

            joinAll(*jobs.toTypedArray())
        }
    }

    private suspend fun trackerStartScan(nonSystemAppsIds: MutableList<String>) {
        trackers.analyse(nonSystemAppsIds, object : TrackerListener {
            override fun onTrackerFound(tracker: TrackerScanUpdate) {
                if (trackers.isRunning()) {
                    trackersFound += tracker.upTracker.trackers.size
                    val appName = (getApplicationInfoUseCase(tracker.upTracker.appId)!!).name
                    val trackers =
                        TrackerListConverter().fromTrackerList(tracker.upTracker.trackers)
                    val trackerModel =
                        TrackerModel(0, historyItemId, appName, tracker.upTracker.appId, trackers)
                    CoroutineScope(Dispatchers.IO).launch {
                        saveTrackerUseCase(trackerModel)
                    }

                    malwareProgressMessage(
                        ScanMessage(
                            tracker.upTracker.message,
                            ScannerType.TRACKERS
                        )
                    )
                }
            }

            override fun trackerOnProgress(progress: Double) {
                malwareOnProgress(progress, 100, ScannerType.TRACKERS)
            }

            override fun trackerOnFinish() {
                CoroutineScope(Dispatchers.IO).launch {
                    val trackerScanStats =
                        ScanStats(-1, -1, -1, historyItemId, false, ScannerType.TRACKERS)
                    updateScanStats(trackerScanStats)
                    malwareOnFinish(updatedScanStats)
                }
            }
        })
    }

    override fun stopScan(reason: CancelScanningUseCase.CancelReason) {
        scanJob.cancelChildren()
        scanJob.cancel()

        if (blacklist.isRunning()) {
            blacklist.cancel()
            malwareOnFinish(ScanStats(-1, -1, -1, -1, true, ScannerType.BLACKLIST))
        }

        if (hypatiaScanner?.running == true) {
            hypatia.stopScan()
            malwareOnFinish(ScanStats(-1, -1, -1, -1, true, ScannerType.HYPATIA))
        }

        if (trackers.isRunning()) {
            trackers.cancel()
            CoroutineScope(Dispatchers.IO).launch {
                saveTrackerUseCase.deleteAllForScan(historyItemId)
            }
            malwareOnFinish(ScanStats(-1, -1, -1, historyItemId, true, ScannerType.TRACKERS))
            trackersFound = 0
        }

        val historyModel = HistoryModel(historyItemId, "", "", -1, -1, -1, -1, -1)
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                saveHistoryUseCase.delete(historyModel)
                malwareList.clear()
                historyItemId = 0
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            CoroutineScope(Dispatchers.Main).launch {
                malwareOnProgress(0.0, 100, ScannerType.HYPATIA)
                malwareOnProgress(0.0, 100, ScannerType.BLACKLIST)
                malwareOnProgress(0.0, 100, ScannerType.TRACKERS)
                scanStatusSharedFlow.emit(Resource.error(Throwable(reason.name)))
            }
        }, 500)

        stopScanServiceUseCase()
    }

    override fun isScanning(): Boolean {
        return blacklist.isRunning() || hypatiaScanner?.running == true || trackers.isRunning()
    }

    override fun currentScanFlow(): Flow<Resource<ScanUpdate>> {
        return scanStatusSharedFlow
    }


    override fun malwareProgressMessage(scanMessage: ScanMessage) {
        CoroutineScope(Dispatchers.Main).launch {
            scanStatusSharedFlow.emit(Resource.success(ScanUpdate(message = scanMessage)))
        }
    }

    override fun onProgress(progress: Double, total: Int) {
        malwareOnProgress(progress, total.toLong(), ScannerType.HYPATIA)
    }

    override fun progressMessage(message: String) {
        val scanMessage = ScanMessage(message, ScannerType.HYPATIA)
        malwareProgressMessage(scanMessage)
    }

    override fun malwareOnProgress(progress: Double, total: Long, type: ScannerType) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d("ScanFlow", "ScannerRepository, malwareOnProgress: $progress, type: $type")
            scanStatusSharedFlow.emit(Resource.loading(progress, total, type))
        }

        when (type) {
            ScannerType.HYPATIA -> {
                hypatiaProgress = progress
            }

            ScannerType.BLACKLIST -> {
                blacklistProgress = progress
            }

            ScannerType.TRACKERS -> {
                trackersProgress = progress
            }

            else -> {}
        }

        val notificationProgress = (hypatiaProgress + blacklistProgress + trackersProgress) / 3

        if (notificationProgress in 0.0..100.0 && isScanning()) {
            notificationManager.makeStatusNotification(
                stringProvider.getString(R.string.up_av_starting_the_files_scanner),
                progress = notificationProgress.toInt(),
                historyItemId
            )
        }
        if (notificationProgress >= 100.0) {
            stopScanServiceUseCase()
        }
    }

    override fun onMalwareDetected(malware: MalwareModel) {
        CoroutineScope(Dispatchers.Main).launch {
            malwareList.add(malware)
            val result = ScanUpdate(upMalware = malware)
            scanStatusSharedFlow.emit(Resource.success(result))
        }
    }

    override fun malwareOnFinish(stats: ScanStats) {
        stats.let { scanStats ->
            updateScanStats(scanStats)
            CoroutineScope(Dispatchers.Main).launch {
                if (!stats.isCanceled) {
                    val result = ScanUpdate(scanStats = updatedScanStats)
                    scanStatusSharedFlow.emit(Resource.success(result))

                    if (updatedScanStats.type != ScannerType.TRACKERS) {
                        malwareList.map { it.scanId = historyItemId }
                        saveMalwareUseCase(malwareList)
                        updateScanDoneUseCase()
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!isScanning()) {
                            if (malwareList.isEmpty() && trackersFound == 0) {
                                notificationManager.makeStatusNotification(
                                    stringProvider.getString(R.string.up_av_scan_completed_no_threats_detected),
                                    null, historyItemId, true
                                )
                            } else {
                                notificationManager.makeStatusNotification(
                                    stringProvider.getString(
                                        R.string.up_av_scan_completed_number_malware_detected,
                                        malwareList.size,
                                        trackersFound
                                    ), null, historyItemId, true
                                )
                            }

                            malwareList.clear()
                            historyItemId = 0
                            trackersFound = 0
                            updatedScanStats =
                                ScanStats(-1, -1, -1, historyItemId, false, ScannerType.NONE)
                        }
                    }, 1000)
                } else {
                    //The scan was canceled.
                    Handler(Looper.getMainLooper()).postDelayed({
                        notificationManager.dismissNotification()
                    }, 1000)
                }
            }
        }
    }

    private fun updateScanStats(scanStats: ScanStats) {
        if (!scanStats.isCanceled) {
            updatedScanStats.type = scanStats.type

            if (updatedScanStats.secondsSpent <= 0) {
                updatedScanStats.secondsSpent = scanStats.secondsSpent
            }

            if (updatedScanStats.megabytesHashed <= 0) {
                updatedScanStats.megabytesHashed = scanStats.megabytesHashed
            }

            if (updatedScanStats.totalFilesScanned <= 0) {
                if(scanStats.type == ScannerType.HYPATIA) {
                    updatedScanStats.totalFilesScanned = scanStats.totalFilesScanned
                }
            }

            if (historyItemId != 0) {
                updatedScanStats.historyItemId = historyItemId
            }

            CoroutineScope(Dispatchers.IO).launch {
                updateHistoryItem(updatedScanStats, malwareList.size, trackersFound)
            }
        }
    }

    override fun malwareOnError(error: String?) {
        malwareList.clear()
        CoroutineScope(Dispatchers.Main).launch {
            scanStatusSharedFlow.emit(Resource.error(Throwable(error)))
        }
    }

    override fun onError(error: String?) {
        malwareOnError(error)
    }

    override fun onHypatiaScanFinish(stats: us.spotco.malwarescanner.malware.ScanStats) {
        val mapper = HypatiaMalwareMapper()
        val myStats = mapper.convertScanStats(stats)
        myStats.type = ScannerType.HYPATIA
        malwareOnFinish(myStats)
    }

    override fun onMalwareDetected(malware: us.spotco.malwarescanner.malware.MalwareModel) {
        val mapper = HypatiaMalwareMapper()
        onMalwareDetected(mapper.convertMalwareModel(malware))
    }

    override suspend fun createScanId(): Int {
        if (historyItemId == 0) {
            historyItemId = saveHistoryUseCase(
                HistoryModel(
                    0, "", DateTimeUtils.getCurrentDateTimeString(), 0, 0, 0, 0, 0
                )
            )
        }
        return historyItemId
    }

    override suspend fun getActiveScanId(): Int {
        return if (historyItemId != 0) {
            historyItemId
        } else {
            val latestScanId: Int = withContext(Dispatchers.IO) {
                saveHistoryUseCase.getLastEntryId() ?: 0
            }
            latestScanId
        }
    }

    override fun getScanType(): Boolean {
        return isQuickScan
    }

    private suspend fun updateHistoryItem(
        scanStats: ScanStats,
        malwareFound: Int,
        trackersFound: Int
    ) {
        val dateTimeString = DateTimeUtils.getCurrentDateTimeString()
        val scanTitle = if (isQuickScan) {
            stringProvider.getString(
                R.string.up_av_quick_scan_title,
            )
        } else {
            stringProvider.getString(
                R.string.up_av_full_scan_title,
            )
        }
        val historyModel = HistoryModel(
            historyItemId,
            scanTitle,
            dateTimeString,
            malwareFound,
            trackersFound,
            scanStats.totalFilesScanned,
            appRepository.countAllApps(),
            scanStats.megabytesHashed
        )
        saveHistoryUseCase.update(historyModel)
    }

    override fun isScannerDone(type: ScannerType): Boolean {
        when (type) {
            ScannerType.HYPATIA -> {
                if (hypatiaProgress == 100.0) return true
            }

            ScannerType.BLACKLIST -> {
                if (blacklistProgress == 100.0) return true
            }

            ScannerType.TRACKERS -> {
                if (trackersProgress == 100.0) return true
            }

            else -> {}
        }
        return false
    }

    override fun getScannerProgress(scanner: ScannerType): Double {
        return when (scanner) {
            ScannerType.HYPATIA -> {
                hypatiaProgress
            }
            ScannerType.BLACKLIST -> {
                blacklistProgress
            }
            ScannerType.TRACKERS -> {
                trackersProgress
            }
            else -> {
                0.0
            }
        }
    }
}
package com.unplugged.up_antivirus.ui.scan

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.trackerextension.TrackerModel
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.base.Utils
import com.unplugged.up_antivirus.data.history.model.HistoryModel
import com.unplugged.up_antivirus.data.tracker.model.TrackerDetailsRepository
import com.unplugged.up_antivirus.workers.ScannerWorker
import com.unplugged.upantiviruscommon.model.Resource
import com.unplugged.up_antivirus.domain.use_case.CancelScanningUseCase
import com.unplugged.up_antivirus.domain.use_case.CreateScanIdUseCase
import com.unplugged.up_antivirus.domain.use_case.GetActiveScanIdUseCase
import com.unplugged.up_antivirus.domain.use_case.GetHistoryModelByScanIdUseCase
import com.unplugged.up_antivirus.domain.use_case.GetApplicationIconUseCase
import com.unplugged.up_antivirus.domain.use_case.GetApplicationInfoUseCase
import com.unplugged.up_antivirus.domain.use_case.GetMalwaresByScanIdUseCase
import com.unplugged.up_antivirus.domain.use_case.GetScanUpdatesFlowUseCase
import com.unplugged.up_antivirus.domain.use_case.GetTrackersByScanIdUseCase
import com.unplugged.up_antivirus.domain.use_case.IsScanningUseCase
import com.unplugged.up_antivirus.domain.use_case.ObserveTrackersByScanIdUseCase
import com.unplugged.up_antivirus.domain.use_case.RemoveMalwareUseCase
import com.unplugged.up_antivirus.domain.use_case.StopScanServiceUseCase
import com.unplugged.up_antivirus.scanner.repository.DefaultScannerRepository
import com.unplugged.up_antivirus.scanner.repository.ScannerRepository
import com.unplugged.up_antivirus.workers.UNIQUE_SCAN_WORKER_REQUEST
import com.unplugged.up_antivirus.workers.WORKER_RESULT
import com.unplugged.upantiviruscommon.model.ScanProgress
import com.unplugged.upantiviruscommon.malware.MalwareModel
import com.unplugged.upantiviruscommon.malware.ScanMessage
import com.unplugged.upantiviruscommon.malware.ScanStats
import com.unplugged.upantiviruscommon.malware.ThreatStatus
import com.unplugged.upantiviruscommon.model.AppInfo
import com.unplugged.upantiviruscommon.model.ScannerType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val getScanUpdatesFlowUseCase: GetScanUpdatesFlowUseCase,
    private val cancelScanningUseCase: CancelScanningUseCase,
    private val removeMalwareUseCase: RemoveMalwareUseCase,
    private val getMalwareByScanIdUseCase: GetMalwaresByScanIdUseCase,
    private val getTrackersByScanIdUseCase: GetTrackersByScanIdUseCase,
    private val getHistoryModelByScanIdUseCase: GetHistoryModelByScanIdUseCase,
    private val observeTrackersByScanIdUseCase: ObserveTrackersByScanIdUseCase,
    private val stopScanServiceUseCase: StopScanServiceUseCase,
    private val createScanIdUseCase: CreateScanIdUseCase,
    private val getActiveScanIdUseCase: GetActiveScanIdUseCase,
    private val isScanningUseCase: IsScanningUseCase,
    private val scannerRepository: ScannerRepository,
    private val workManager: WorkManager,
    private val getApplicationIconUseCase: GetApplicationIconUseCase,
    private val trackerDetailsRepository: TrackerDetailsRepository,
    private val getApplicationInfoUseCase : GetApplicationInfoUseCase
) : ViewModel() {

    private val _historyModel = MutableLiveData<HistoryModel?>()
    var historyModel : LiveData<HistoryModel?> = _historyModel

    private val _malware = mutableListOf<MalwareModel>()

    private val _malwareData = MutableLiveData<List<MalwareModel>>()
    val malwareData: LiveData<List<MalwareModel>> = _malwareData

    val scanIdLiveData = MutableLiveData<Int>()

    lateinit var trackersLiveData: LiveData<List<TrackerModel>>

    private val _trackersData = MutableLiveData<List<TrackerModel>>()
    val trackerData: LiveData<List<TrackerModel>> = _trackersData

    private val _scanningState = MutableLiveData<ScanningState>()
    val scanningState: LiveData<ScanningState> = _scanningState

    private val _scanningProgress = MutableLiveData<ScanProgress>()
    val scanningProgress: LiveData<ScanProgress> = _scanningProgress

    private val scanStateObserver =
        Observer<ScanProgress> { value -> _scanningProgress.postValue(value) }

    private val _navigateBack = MutableLiveData(false)
    val navigateBack: LiveData<Boolean> = _navigateBack.distinctUntilChanged()

    private var scannerWorkerRequest: OneTimeWorkRequest? = null

    private val shieldLogo = MutableLiveData(false)
    val shieldLogoLiveData: LiveData<Boolean> = shieldLogo

    private val _setTitle = MutableLiveData<String>()
    val setTitle: LiveData<String> = _setTitle

    fun createScanId() {
        viewModelScope.launch {
            val scanId = createScanIdUseCase()
            scanIdLiveData.value = scanId
        }
    }

    suspend fun updateUiWhenResuming() {
        this.scanId = getActiveScanIdUseCase()
    }

    fun subscribeTrackers(scanId: Int) {
        trackersLiveData = observeTrackersByScanIdUseCase(scanId)
    }

    fun subscribeToScanStats() {
        Log.d("ScanFlow", "ScanViewModel, subscribe")
        getScanUpdatesFlowUseCase().onEach { scanUpdate ->
            Log.d("ScanFlow", "ScanViewModel, onUpdate: $scanUpdate")

            when (scanUpdate) {
                is Resource.Loading -> {
                    val progress = scanUpdate.progress?.progress
                    val type = scanUpdate.progress?.type
                    Utils.printLog(ScanViewModel::class.java, "malwareOnProgress: $progress")
                    updateProgressBars(progress!!, type!!)
                }

                is Resource.Data -> {
                    Utils.printLog(
                        ScanViewModel::class.java,
                        "startScanning onData: ${scanUpdate.data}"
                    )

                    scanUpdate.data.upMalware?.let {
                        // Allocate malware's in list, after scan finished they will be saved to DB and this will trigger the subscription with flow.
                        _malware.add(it)
                        _malwareData.postValue(_malware)
                    }

                    scanUpdate.data.scanStats?.let {
                        if (isScannerDone(ScannerType.HYPATIA) || isScannerDone(ScannerType.BLACKLIST)) {
                            onScanFinished(it)
                        }

                        _scanningState.postValue(
                            ScanningState(
                                isLoading = false,
                                scanStats = it
                            )
                        )
                    }

                    scanUpdate.data.message?.let {
                        val state = _scanningState.value ?: ScanningState()
                        val scanMessage = ScanMessage(it.message, it.type)
                        _scanningState.postValue(state.copy(message = scanMessage))
                    }
                }

                is Resource.Error -> {
                    _malware.clear()
                    _malwareData.postValue(_malware)
                    _scanningState.postValue(ScanningState(error = scanUpdate.error.message))
                }
            }
        }.launchIn(viewModelScope)
    }

    val isActiveThreatsExist: Boolean
        get() {
           return !(_malwareData.value?.filter { it.status == ThreatStatus.EXIST }.isNullOrEmpty())
        }

    var scanId: Int? = null
        set(value) {
            value?.let { it ->
                getMalwareByScanIdUseCase(it).onEach {
                    _malwareData.postValue(it)
                }.launchIn(viewModelScope)

                getTrackersByScanIdUseCase(it).onEach {
                    _trackersData.postValue(it)
                }.launchIn(viewModelScope)

                viewModelScope.launch {
                    _historyModel.postValue(getHistoryModelByScanIdUseCase(it))
                }
            }
            field = value
        }

    fun startScan() {
        if (!isScanning()) {
            _scanningState.postValue(ScanningState(isLoading = true))
            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(networkType = NetworkType.CONNECTED)
                .build()

            // Add WorkRequest to check and update DB
            scannerWorkerRequest = OneTimeWorkRequestBuilder<ScannerWorker>()
                .setConstraints(constraints)
                .build()

            val continuation = workManager.beginUniqueWork(
                UNIQUE_SCAN_WORKER_REQUEST,
                ExistingWorkPolicy.REPLACE,
                scannerWorkerRequest!!
            )

            // Enqueue the work request
            continuation.enqueue()

            // Observe the failure state only
            observeFailureResult(scannerWorkerRequest!!.id)
        }
    }

    private fun observeFailureResult(workRequestId: UUID) {
        // Observe work status by id
        workManager.getWorkInfoByIdLiveData(workRequestId).observeForever { workInfo ->
            if(workInfo != null){
                if (workInfo.state == WorkInfo.State.FAILED) {
                    // Handle failure
                    val errorMessage = workInfo.outputData.getString(WORKER_RESULT) ?: "Scan failed"
                    _scanningState.postValue(
                        ScanningState(
                            isLoading = false,
                            message = ScanMessage(errorMessage, ScannerType.HYPATIA)
                        )
                    )
                    // Navigate back to the previous activity
                    navigateBackToPreviousActivity(true)
                } else{ }
            }
        }
    }

    fun navigateBackToPreviousActivity(navigate: Boolean) {
        _navigateBack.postValue(navigate)
    }

    private fun onScanFinished(scanStats: ScanStats) {
        scanStats.historyItemId?.let { historyItemId ->
            if (historyItemId != 0) {
                _malware.map { it.scanId = historyItemId }
                scanId = historyItemId
            }
        }
    }

    fun removeThreat(malware: MalwareModel): LiveData<MalwareModel> {
        val data = MutableLiveData<MalwareModel>()

        removeMalwareUseCase(malware).onEach {
            when (it) {
                is Resource.Loading -> {
                    malware.status = ThreatStatus.PENDING
                    data.postValue(malware)
                }

                is Resource.Data -> {
                    // Probably nothing to do, the update should arrive from Room DB flow
                }

                is Resource.Error -> {
                    malware.status = ThreatStatus.FAILED
                    data.postValue(malware)
                    Utils.printLog(
                        ScanViewModel::class.java,
                        it.error.message ?: "Error removing malware"
                    )
                }
            }
        }.launchIn(viewModelScope)
        return data
    }

    fun cancelScan() {
        cancelScanningUseCase()
        stopScanService()
    }

    fun isScanning(): Boolean = isScanningUseCase()

    fun updateMalwareStatus(malware: MalwareModel) {
        viewModelScope.launch(Dispatchers.Main) {
            removeMalwareUseCase.updateMalwareStatus(malware)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Utils.printLog(ScanViewModel::class.java, "onCleared")
        (scannerRepository as DefaultScannerRepository).observableScanProgress.removeObserver(
            scanStateObserver
        )
    }

    private fun updateProgressBars(progress: Double, scannerType: ScannerType) {
        val scanProgress = ScanProgress(progress, scannerType)
        _scanningProgress.postValue(scanProgress)
    }

    fun isScannerDone(type: ScannerType): Boolean {
        return scannerRepository.isScannerDone(type)
    }

    fun isMalwareScanDone(): Boolean {
        return scannerRepository.isScannerDone(ScannerType.HYPATIA) && scannerRepository.isScannerDone(
            ScannerType.BLACKLIST
        )
    }

    fun stopScanService() {
        stopScanServiceUseCase()
    }

    fun getScanProgress(scanner: ScannerType): Double {
        return scannerRepository.getScannerProgress(scanner)
    }

    fun getScanType(context: Context, isQuickScan: Boolean): String{
        return when(isQuickScan){
            true -> context.getString(R.string.quick_scan)
            false -> context.getString(R.string.full_scan)
        }
    }

    //this function is here because we will add up phone trackers handle in the privacy
    fun setShieldLogo(activeThreat: Boolean){
        shieldLogo.postValue(activeThreat)
    }

    fun setTitleFromScan(context: Context , isEmpty: Boolean){
        val text = if(isEmpty) {
            context.getString(R.string.your_device_is_protected)
        } else{
            context.getString(R.string.your_device_is_compromised)
        }
        _setTitle.postValue(text)
    }

    fun getApplicationIcon(id: String?): Drawable? {
        return getApplicationIconUseCase.invoke(id)
    }

    fun loadTrackersDetails(context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            trackerDetailsRepository.getTrackerDetails(context)
        }
    }

    fun getAppInfo(packageName: String): AppInfo? {
        return getApplicationInfoUseCase.invoke(packageName)
    }
}
package com.unplugged.up_antivirus.ui.status

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unplugged.accounthelper.AccountHelper
import com.unplugged.upantiviruscommon.model.ScanParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.onEach
import com.unplugged.up_antivirus.data.history.model.HistoryModel
import com.unplugged.up_antivirus.domain.use_case.GetLatestScanFlowUseCase
import com.unplugged.up_antivirus.domain.use_case.GetScanPreferencesUseCase
import com.unplugged.up_antivirus.domain.use_case.IsAuthenticatedUseCase
import com.unplugged.up_antivirus.domain.use_case.LoadBlacklistPackagesUseCase
import com.unplugged.up_antivirus.domain.use_case.UpdateDatabaseUseCase
import com.unplugged.up_antivirus.domain.use_case.UpdateScanPreferencesUseCase
import com.unplugged.upantiviruscommon.model.Connectivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val getLatestScanFlowUseCase: GetLatestScanFlowUseCase,
    private val getScanPreferencesUseCase: GetScanPreferencesUseCase,
    private val updateScanPreferencesUseCase: UpdateScanPreferencesUseCase,
    private val updateDatabaseUseCase: UpdateDatabaseUseCase,
    private val loadBlacklistPackagesUseCase: LoadBlacklistPackagesUseCase,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val accountHelper: AccountHelper
) : ViewModel() {

    private val _latestScan = MutableLiveData<HistoryModel?>()
    val latestScan: LiveData<HistoryModel?> = _latestScan

    private val _authLiveData = MutableLiveData<Boolean>()
    val authLiveData: LiveData<Boolean> = _authLiveData

    private val _loadBlacklistedApps = MutableLiveData<Boolean?>()
    val blackListedAppLiveData: LiveData<Boolean?> = _loadBlacklistedApps

    init {
        getLatestScanFlowUseCase().onEach {
            _latestScan.postValue(it)
        }.launchIn(viewModelScope)
    }

    fun checkAuth() {
        _authLiveData.postValue(isAuthenticatedUseCase())
    }

    fun loadBlacklistApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _loadBlacklistedApps.postValue(true)
            loadBlacklistPackagesUseCase()
            _loadBlacklistedApps.postValue(false)
        }
    }

    fun updateScanPreferences(scanParams: ScanParams) {
        updateScanPreferencesUseCase(scanParams)
    }

    fun getScanPreferences(): ScanParams {
        return getScanPreferencesUseCase()
    }

    fun internetConnectivity(): Connectivity {
        return updateDatabaseUseCase.internetConnectivity()
    }

    fun setAllowDownloadOverCellular() {
        updateDatabaseUseCase.setAllowDownloadOverCellular(true)
    }

    suspend fun updateRequired(): Boolean {
        return updateDatabaseUseCase.checkHypatiaDatabaseVersion()
    }

    fun newAppDatabase() : Boolean{
        return updateDatabaseUseCase.isNewAppDatabase()
    }

    fun startRealTimeProtection(): Boolean{
        return getScanPreferencesUseCase.getPreferencesRepository().getRealTimeProtectionValue()
    }
}

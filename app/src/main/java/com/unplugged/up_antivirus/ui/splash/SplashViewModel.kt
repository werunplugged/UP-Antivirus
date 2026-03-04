package com.unplugged.up_antivirus.ui.splash


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unplugged.accounthelper.SessionData
import com.unplugged.up_antivirus.domain.AuthMode
import com.unplugged.up_antivirus.domain.preferences.PreferencesRepository
import com.unplugged.up_antivirus.domain.use_case.GetOrRefreshAttestationTokenUseCase
import com.unplugged.up_antivirus.domain.use_case.GetSessionUseCase
import com.unplugged.up_antivirus.domain.use_case.IsScanningUseCase
import com.unplugged.up_antivirus.domain.use_case.ShouldShowOnBoardingScreenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getSetShouldShowOnBoardingScreenUseCase: ShouldShowOnBoardingScreenUseCase,
    private val getSessionUseCase: GetSessionUseCase,
    private val isScanningUseCase: IsScanningUseCase,
    private val preferencesRepository: PreferencesRepository,
    val authMode: AuthMode,
    private val getOrRefreshAttestationTokenUseCase: GetOrRefreshAttestationTokenUseCase?
) : ViewModel() {

    private val _tokenState = MutableLiveData<TokenState>()
    val tokenState: LiveData<TokenState> = _tokenState

    fun getSession(): SessionData?{
        return getSessionUseCase()
    }

    fun shouldShowOnBoardingFirstTime(): Boolean {
        return getSetShouldShowOnBoardingScreenUseCase(setShouldShow = false)
    }

    fun isScanning(): Boolean{
        return isScanningUseCase()
    }

    fun getScanType(): Boolean{
        return preferencesRepository.getScanParams().isQuickScan
    }

    fun getOrRefreshToken() {
        val useCase = getOrRefreshAttestationTokenUseCase ?: return
        _tokenState.value = TokenState(isLoading = true)
        viewModelScope.launch {
            try {
                val result = useCase.execute()
                _tokenState.value = TokenState(tokenExist = true)
            } catch (e: Exception) {
                _tokenState.value = TokenState(error = e.message ?: "Failed to acquire attestation token")
            }
        }
    }
}
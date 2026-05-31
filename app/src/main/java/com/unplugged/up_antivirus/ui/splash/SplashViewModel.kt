package com.unplugged.up_antivirus.ui.splash


import androidx.lifecycle.ViewModel
import com.unplugged.up_antivirus.domain.preferences.PreferencesRepository
import com.unplugged.up_antivirus.domain.use_case.IsAuthenticatedUseCase
import com.unplugged.up_antivirus.domain.use_case.IsScanningUseCase
import com.unplugged.up_antivirus.domain.use_case.ShouldShowOnBoardingScreenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getSetShouldShowOnBoardingScreenUseCase: ShouldShowOnBoardingScreenUseCase,
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    private val isScanningUseCase: IsScanningUseCase,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    fun hasAttestation(): Boolean = isAuthenticatedUseCase.hasAttestation()

    fun hasSession(): Boolean = isAuthenticatedUseCase.hasSession()

    fun shouldShowOnBoardingFirstTime(): Boolean {
        return getSetShouldShowOnBoardingScreenUseCase(setShouldShow = false)
    }

    fun isScanning(): Boolean{
        return isScanningUseCase()
    }

    fun getScanType(): Boolean{
        return preferencesRepository.getScanParams().isQuickScan
    }
}

package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.upantiviruscommon.model.ScanParams
import com.unplugged.up_antivirus.domain.preferences.PreferencesRepository
import javax.inject.Inject

class UpdateScanPreferencesUseCase @Inject constructor(private val preferencesRepository: PreferencesRepository) {

    operator fun invoke(scanParams: ScanParams) {
        preferencesRepository.setScanParams(scanParams)
    }
}
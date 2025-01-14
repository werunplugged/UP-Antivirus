package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.up_antivirus.domain.preferences.PreferencesRepository
import javax.inject.Inject

class ShouldShowOnBoardingScreenUseCase @Inject constructor(private val preferencesRepository: PreferencesRepository) {

    operator fun invoke(setShouldShow: Boolean): Boolean {
        return preferencesRepository.getShouldShowOnBoardingScreen(setShouldShow)
    }
}
package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.signature_scanner.repository.AppRepository
import com.unplugged.signature_scanner.repository.BlacklistPackageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.unplugged.up_antivirus.domain.preferences.PreferencesRepository
import javax.inject.Inject

class LoadBlacklistPackagesUseCase @Inject constructor(
    private val appRepository: AppRepository,
    private val blacklistPackagesRepository: BlacklistPackageRepository,
    private val preferencesRepository: PreferencesRepository
) {

    suspend operator fun invoke() {
        withContext(Dispatchers.IO) {
            if (preferencesRepository.getBlacklistPackageLastUpdateTime() <= 0) {
                val items = appRepository.getBlackListedPackages() // read asset file
                blacklistPackagesRepository.insertAll(items.toList()) // insert to db
                preferencesRepository.setBlacklistPackageLastUpdateTime(System.currentTimeMillis()) //set last updated time
            }
        }
    }
}
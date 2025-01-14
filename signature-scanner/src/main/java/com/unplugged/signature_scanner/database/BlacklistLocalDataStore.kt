package com.unplugged.signature_scanner.database

import com.unplugged.upantiviruscommon.model.ApkInfo
import com.unplugged.signature_scanner.repository.BlacklistPackageRepository

class BlacklistLocalDataStore(private val blacklistPackagesRepository: BlacklistPackageRepository) {
    suspend fun insertToDatabase(list: List<ApkInfo>) {
        blacklistPackagesRepository.insertAll(list)
    }

    suspend fun removeFromDatabase(list: List<ApkInfo>) {
        for (apkInfo in list) {
            blacklistPackagesRepository.delete(apkInfo)
        }
    }
}
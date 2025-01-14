package com.unplugged.up_antivirus.domain.updates

import com.unplugged.upantiviruscommon.model.ScannerType

interface DatabaseRepository {

    //suspend fun updateBlacklistDatabase(currentVersion: Int) : Int
    suspend fun getDatabaseVersion(module: ScannerType): Int
    suspend fun isDatabaseLoaded(): Boolean
    suspend fun loadDatabase()
    fun errorNotification(title: String, subtitle: String)
    suspend fun updateDatabase(token: String): Boolean
    suspend fun isDatabaseAvailable(): Boolean
    suspend fun downloadHypatiaFiles()
}
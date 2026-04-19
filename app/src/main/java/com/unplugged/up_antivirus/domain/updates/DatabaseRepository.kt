package com.unplugged.up_antivirus.domain.updates

import com.unplugged.upantiviruscommon.model.ScannerType

interface DatabaseRepository {
    suspend fun getDatabaseVersion(module: ScannerType): Int
    suspend fun isDatabaseLoaded(): Boolean
    suspend fun loadDatabase()
    fun errorNotification(title: String, subtitle: String)
    suspend fun updateDatabase(token: String): UpdateResult
    suspend fun isDatabaseAvailable(): Boolean
}
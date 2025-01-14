package com.unplugged.up_antivirus.domain.account

interface AccountRepository {
    suspend fun updateScanDone()
    suspend fun syncRemainingScans()
    fun getRemainingScans(): Int
    fun reset()
}
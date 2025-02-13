package com.unplugged.up_antivirus.scanner.repository

import com.unplugged.upantiviruscommon.model.ScanParams
import com.unplugged.upantiviruscommon.model.Resource
import com.unplugged.upantiviruscommon.model.ScanUpdate
import com.unplugged.up_antivirus.domain.use_case.CancelScanningUseCase
import com.unplugged.upantiviruscommon.model.ScannerType
import kotlinx.coroutines.flow.Flow

interface ScannerRepository {
    suspend fun startScan(scanParams: ScanParams)
    fun stopScan(reason: CancelScanningUseCase.CancelReason)
    fun isScanning(): Boolean
    fun currentScanFlow(): Flow<Resource<ScanUpdate>>
    suspend fun createScanId(): Int
    suspend fun getActiveScanId(): Int
    fun getScanType(): Boolean
    fun isScannerDone(type: ScannerType):Boolean
    fun getScannerProgress(scanner: ScannerType): Double
}
package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.upantiviruscommon.model.Resource
import com.unplugged.upantiviruscommon.model.ScanUpdate
import com.unplugged.up_antivirus.scanner.repository.ScannerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetScanUpdatesFlowUseCase @Inject constructor(private val scannerRepository: ScannerRepository) {

    operator fun invoke(): Flow<Resource<ScanUpdate>> {
       return scannerRepository.currentScanFlow()
    }
}
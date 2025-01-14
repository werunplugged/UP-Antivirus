package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.up_antivirus.scanner.repository.ScannerRepository
import javax.inject.Inject

class CancelScanningUseCase @Inject constructor(private val scannerRepository: ScannerRepository) {
    operator fun invoke() {
        scannerRepository.stopScan(CancelReason.USER)
    }
    enum class CancelReason {
        USER, SESSION_REVOKE
    }
}
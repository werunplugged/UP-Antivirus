package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.up_antivirus.scanner.repository.ScannerRepository
import javax.inject.Inject


class IsScanningUseCase @Inject constructor(private val scannerRepository: ScannerRepository){
    operator fun invoke() : Boolean {
        return scannerRepository.isScanning()
    }
}
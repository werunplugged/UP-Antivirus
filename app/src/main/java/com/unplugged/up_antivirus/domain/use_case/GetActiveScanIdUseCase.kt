package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.up_antivirus.scanner.repository.ScannerRepository

class GetActiveScanIdUseCase(private val scannerRepository: ScannerRepository) {
     suspend operator fun invoke(): Int {
        return scannerRepository.getActiveScanId()
    }
}
package com.unplugged.up_antivirus.domain.use_case

import android.util.Log
import androidx.work.WorkManager
import com.unplugged.up_antivirus.scanner.repository.ScannerRepository
import com.unplugged.up_antivirus.workers.UNIQUE_SCAN_WORKER_REQUEST
import com.unplugged.up_antivirus.workers.UNIQUE_SCHEDULED_SCAN_WORKER_REQUEST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SoftLogoutUseCase @Inject constructor(
    private val workManager: WorkManager,
    private val scannerRepository: ScannerRepository,
) {
    operator fun invoke() {
        Log.d("SoftLogoutUseCase", "soft logout started")
        workManager.cancelAllWorkByTag(UNIQUE_SCHEDULED_SCAN_WORKER_REQUEST)
        workManager.cancelUniqueWork(UNIQUE_SCAN_WORKER_REQUEST)
        CoroutineScope(Dispatchers.IO).launch {
            if (scannerRepository.isScanning()) {
                scannerRepository.stopScan(CancelScanningUseCase.CancelReason.SESSION_REVOKE)
            }
        }
    }
}

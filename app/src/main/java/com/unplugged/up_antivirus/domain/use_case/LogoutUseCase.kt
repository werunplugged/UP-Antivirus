package com.unplugged.up_antivirus.domain.use_case

import android.util.Log
import androidx.work.WorkManager
import com.unplugged.up_antivirus.data.account.DefaultAccountRepository
import com.unplugged.up_antivirus.data.preferences.SharedPreferencesSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.unplugged.up_antivirus.data.history.HistoryRepository
import com.unplugged.up_antivirus.data.malware.MalwareRepository
import com.unplugged.up_antivirus.scanner.repository.ScannerRepository
import com.unplugged.up_antivirus.workers.UNIQUE_SCHEDULED_SCAN_WORKER_REQUEST
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val workManager: WorkManager,
    private val accountRepository: DefaultAccountRepository,
    private val sharedPreferencesSource: SharedPreferencesSource,
    private val historyRepository: HistoryRepository,
    private val malwareRepository: MalwareRepository,
    private val scannerRepository: ScannerRepository,
) {

    operator fun invoke() {
        Log.d("LogoutUseCase", "logout started")
        accountRepository.reset()
        sharedPreferencesSource.clearAll()
        workManager.cancelAllWorkByTag(UNIQUE_SCHEDULED_SCAN_WORKER_REQUEST)
        CoroutineScope(Dispatchers.IO).launch{
            if(scannerRepository.isScanning()) {
                scannerRepository.stopScan(CancelScanningUseCase.CancelReason.SESSION_REVOKE)
            }
            historyRepository.deleteAll()
            malwareRepository.deleteAll()
        }
    }
}

package com.unplugged.up_antivirus.ui.scan

import com.unplugged.upantiviruscommon.malware.MalwareModel
import com.unplugged.upantiviruscommon.malware.ScanMessage
import com.unplugged.upantiviruscommon.malware.ScanStats
data class ScanningState(
    val isLoading: Boolean = false,
    val malware: MalwareModel? = null,
    val scanStats: ScanStats? = null,
    val message: ScanMessage? = null,
    val error: String? = null
)
package com.unplugged.upantiviruscommon.model

import com.unplugged.upantiviruscommon.malware.ScanMessage
import com.unplugged.upantiviruscommon.malware.MalwareModel
import com.unplugged.upantiviruscommon.malware.ScanStats

data class ScanUpdate(
    val upMalware: MalwareModel? = null,
    val scanStats: ScanStats? = null,
    val message: ScanMessage? = null
)
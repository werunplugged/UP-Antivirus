package com.unplugged.up_antivirus.domain.use_case

import android.content.Context
import android.content.Intent
import com.unplugged.up_antivirus.ui.scan.ScanService
import javax.inject.Inject


class StopScanServiceUseCase @Inject constructor(private val context: Context) {
    operator fun invoke() {
        stopScanService()
    }

    private fun stopScanService() {
        val intent = Intent(context, ScanService::class.java)
        context.stopService(intent)
    }
}
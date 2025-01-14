package com.unplugged.signature_scanner.appscanner

import android.content.Context
import android.util.Log
import com.unplugged.signature_scanner.SignatureScannerAccessPoint
import com.unplugged.signature_scanner.repository.AppRepository
import com.unplugged.upantiviruscommon.malware.MalwareScannerListener


class PackageScanner(
    private val context: Context,
    private val appRepository: AppRepository
) : SignatureScannerAccessPoint {

    val map = mutableMapOf<MalwareScannerListener, ScanPackageTask>()

    override fun isRunning(): Boolean {
        Log.d("isRunning", "isRunning: $map")
        if (map.isEmpty()) return false
        return map.map { it.value }.count { it.isRunning() } > 0
    }

    override fun isRunning(listener: MalwareScannerListener): Boolean {
        return map[listener]?.isRunning() ?: false
    }

    override fun cancel() {
        map.onEach { it.value.cancel() }
        map.clear()
    }

    override fun cancel(listener: MalwareScannerListener) {
        map[listener]?.cancel()
        map.remove(listener)
    }

    override suspend fun startScan(listener: MalwareScannerListener) {
        val task = ScanPackageTask(context, appRepository)
        map[listener] = task
        task.searchBlacklistedAppsOnDevice(listener)
    }

    override suspend fun startScan(listener: MalwareScannerListener, packageName: String) {
        val task = ScanPackageTask(context, appRepository)
        map[listener] = task
        task.searchBlacklistedAppsOnDevice(listener, packageName)
    }
}
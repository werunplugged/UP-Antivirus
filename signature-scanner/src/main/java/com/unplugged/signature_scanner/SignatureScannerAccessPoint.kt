package com.unplugged.signature_scanner

import com.unplugged.upantiviruscommon.malware.MalwareScannerListener


interface SignatureScannerAccessPoint {
    suspend fun startScan(listener: MalwareScannerListener)
    suspend fun startScan(listener: MalwareScannerListener, packageName: String)
    fun isRunning(): Boolean
    fun cancel()
    fun cancel(listener: MalwareScannerListener)
    fun isRunning(listener: MalwareScannerListener): Boolean
    }
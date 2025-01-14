package com.unplugged.hypatia_extensions

import us.spotco.malwarescanner.Database
import us.spotco.malwarescanner.MalwareScanner
import us.spotco.malwarescanner.malware.HypatiaMalwareScannerListener


interface HypatiaAccessPoint {
    fun startScan(quick: Boolean)

    fun updateDatabase(token: String, listener: Database.UpdateListener)

    fun enableMalwareService()

    fun disableMalwareService()

    fun getMalwareScanner(malwareScannerListener: HypatiaMalwareScannerListener): MalwareScanner

    fun isDatabaseLoaded(): Boolean

    fun loadDatabase()

    fun isDatabaseAvailable(): Boolean

    fun stopScan()
}
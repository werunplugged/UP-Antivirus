package com.unplugged.hypatia_extensions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import us.spotco.malwarescanner.Database
import us.spotco.malwarescanner.Database.UpdateListener
import us.spotco.malwarescanner.MalwareScanner
import us.spotco.malwarescanner.MalwareScannerService
import us.spotco.malwarescanner.Utils
import us.spotco.malwarescanner.malware.HypatiaMalwareScannerListener
import java.io.File

class Hypatia(private val context: Context) : HypatiaAccessPoint {
    val MAX_PROGRESS = 100

    lateinit var malwareScanner: MalwareScanner
    lateinit var malwareScannerListener: HypatiaMalwareScannerListener

    override fun getMalwareScanner(malwareScannerListener: HypatiaMalwareScannerListener): MalwareScanner {
        this.malwareScannerListener = malwareScannerListener
        malwareScanner = MalwareScanner(context, true, malwareScannerListener)
        return malwareScanner
    }

    override fun enableMalwareService() {
        Utils.considerStartService(context)
    }

    override fun disableMalwareService() {
        if (Utils.isServiceRunning(MalwareScannerService::class.java, context)) {
            val realtimeScanner = Intent(
                context,
                MalwareScannerService::class.java
            )
            context.stopService(realtimeScanner)
        }
    }

    override fun updateDatabase(token: String, listener: UpdateListener) {
        // Call the suspend function that performs the database update
        Database.updateDatabase(context, Database.signatureDatabases, token, listener)
    }

    override fun stopScan() {
        malwareScanner.running = false
        malwareScanner.cancel(true)
    }

    override fun startScan(quick: Boolean) {
        malwareScanner.running = true
        val filesToScan = HashSet<File>()
        malwareScannerListener.onProgress(0.0, MAX_PROGRESS)
        if (quick) {
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            filesToScan.add(downloadsDir)

            context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .forEach { packageInfo ->
                    packageInfo?.let {
                        packageInfo.sourceDir?.let { filesToScan.add(File(it)) }
                        packageInfo.dataDir?.let { filesToScan.add(File(it)) }
                        packageInfo.nativeLibraryDir?.let { filesToScan.add(File(it)) }
                        packageInfo.publicSourceDir?.let { filesToScan.add(File(it)) }
                    }
                }
        } else {
            filesToScan.addAll(
                listOf(
                    Environment.getRootDirectory(),
                    File("/apex"),
                    File("/cache"),
                    File("/data"),
                    File("/data/local/tmp"),
                    File("/firmware"),
                    File("/oem"),
                    File("/odm"),
                    File("/odm_dlkm"),
                    File("/product"),
                    File("/system"),
                    File("/system_dlkm"),
                    File("/vendor"),
                    File("/vendor_dlkm")
                )
            )

            if(Build.MODEL != "UP01"){
                filesToScan.add(File("/"))
            }

            context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .forEach { packageInfo ->
                    packageInfo?.let {
                        packageInfo.sourceDir?.let { filesToScan.add(File(it)) }
                        packageInfo.dataDir?.let { filesToScan.add(File(it)) }
                        packageInfo.nativeLibraryDir?.let { filesToScan.add(File(it)) }
                        packageInfo.publicSourceDir?.let { filesToScan.add(File(it)) }
                    }
                }

            Environment.getExternalStorageDirectory()?.let { filesToScan.add(it) }

            val externalStorage = File("/storage")
            if (externalStorage.exists()) {
                filesToScan.add(externalStorage)
            }

        }
        malwareScanner.executeOnExecutor(Utils.getThreadPoolExecutor(), filesToScan)
    }

    override fun isDatabaseLoaded(): Boolean {
        return Database.isDatabaseLoaded()
    }

    override fun loadDatabase() {
        Database.loadDatabase(context, false, Database.signatureDatabases)
    }

    override fun isDatabaseAvailable(): Boolean {
        return Database.areDatabasesAvailable()
    }

}

package com.unplugged.hypatia_extensions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

    override fun updateDatabase(attToken: String, userToken: String, listener: UpdateListener) {
        // Call the suspend function that performs the database update
        Database.updateDatabase(context, Database.signatureDatabases, attToken, userToken, listener)
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
            filesToScan.addAll(fullScanRoots())

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
        }
        malwareScanner.executeOnExecutor(Utils.getThreadPoolExecutor(), filesToScan)
    }

    /**
     * Roots walked by a full scan.
     *
     * Deliberately NOT included (UNP-8704):
     *  - "/"                   : /proc/self/root is a symlink back to "/", so the recursive walk
     *                            in Utils.getFilesRecursive never terminates and OOMs the process.
     *  - /proc, /sys, /dev,
     *    /mnt                  : pseudo-filesystems and duplicate mount views. No real files to
     *                            scan, and some entries block forever on read (e.g. /proc/kmsg).
     *  - /data                 : 0771 root:root, so listFiles() returns null for an unprivileged
     *                            app. App directories arrive via ApplicationInfo.dataDir instead.
     *  - /storage              : /storage/self/primary is a symlink to /storage/emulated/0, which
     *                            is already added via Environment.getExternalStorageDirectory().
     *
     * Environment.getRootDirectory() is "/system" and is covered by the explicit entry below.
     */
    private fun fullScanRoots(): List<File> {
        val roots = mutableListOf<File>()

        roots += listOf(
            "/system", "/system_ext", "/product",
            "/vendor", "/odm",
            "/system_dlkm", "/vendor_dlkm", "/odm_dlkm",
            "/firmware", "/oem",
            "/cache",
            "/data/local/tmp"
        ).map(::File)

        roots += activeApexRoots()

        return roots.filter { it.isDirectory }
    }

    /**
     * Active APEX mounts only. Each APEX is also bind-mounted as /apex/<name>@<version> with
     * identical content; a bind mount is not a symlink, so getCanonicalFile() cannot collapse the
     * duplicate and every APEX would otherwise be enumerated twice.
     */
    private fun activeApexRoots(): List<File> =
        File("/apex").listFiles()
            ?.filter { it.isDirectory && !it.name.contains('@') }
            ?: emptyList()

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

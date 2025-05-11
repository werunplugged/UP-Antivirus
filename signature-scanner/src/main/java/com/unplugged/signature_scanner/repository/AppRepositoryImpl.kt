package com.unplugged.signature_scanner.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.util.Log
import com.opencsv.CSVReader
import com.unplugged.upantiviruscommon.malware.MalwareScannerListener
import com.unplugged.upantiviruscommon.malware.ScanMessage
import com.unplugged.upantiviruscommon.model.ApkInfo
import com.unplugged.upantiviruscommon.model.AppInfo
import com.unplugged.upantiviruscommon.model.AppListState
import com.unplugged.upantiviruscommon.model.ScannerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest

class AppRepositoryImpl(
    private val context: Context,
    private val blacklistPackageRepository: BlacklistPackageRepository,
) : AppRepository {

    override suspend fun getInstalledApp(packageName: String): AppInfo? {
        try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val packageInfo = pm.getPackageInfo(appInfo.packageName, PackageManager.GET_SIGNATURES)

            val name = pm.getApplicationLabel(appInfo).toString()

            val versionCode = packageInfo.versionCode
            val versionName = packageInfo.versionName ?: "unknown"
            val icon = appInfo.loadIcon(pm)
            val signatureHash = calculateSHA256(packageInfo.applicationInfo.sourceDir)

            val apkSize = getApkSize(packageInfo)
            val apkInfo = if (signatureHash != null && apkSize != -1L) {
                ApkInfo(signatureHash, apkSize, appInfo.packageName)
            } else null

            return AppInfo(name, packageName, versionName, versionCode.toLong(), icon, apkInfo)
        } catch (e: NameNotFoundException) {
            return null
        }
    }

    override suspend fun getInstalledApps(listener: MalwareScannerListener): AppListState {
        return getAllAppsInternal(listener)
    }

    private fun readCsvFile(): HashSet<ApkInfo> {
        return try {
            val result = HashSet<ApkInfo>()
            val csvReader = CSVReader(context.assets.open("black_list_packages.csv").reader())

            csvReader.skip(1)
            val all = csvReader.readAll()
            all.forEach {
                val apkInfo = ApkInfo(it[0].lowercase(), it[1].toLong(), it[2].lowercase())
                result.add(apkInfo)
            }
            Log.d("AppRepository", "loaded blacklist size: ${result.size}")
            csvReader.close()
            result
        } catch (e: IOException) {
            Log.d("AppRepository", "error reading from file: ${e.message}")
            HashSet()
        }
    }

    override suspend fun getBlackListedPackages(): HashSet<ApkInfo> {
        return withContext(Dispatchers.IO) {
            readCsvFile()
        }
    }

    override suspend fun isBlacklistedApp(apkInfo: ApkInfo?): Boolean {
        return withContext(Dispatchers.IO) {
            if (apkInfo == null) {
                false
            } else {
                blacklistPackageRepository.findBy(
                    apkInfo.sha256, apkInfo.size, apkInfo.packageName
                )?.let { true } ?: false
            }
        }
    }

    override fun countAllApps(): Int {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.MATCH_ALL)
        return apps.size
    }

    private fun getAllAppsInternal(listener: MalwareScannerListener): AppListState {
        try {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(PackageManager.MATCH_ALL)

            val userAppsInfo = mutableListOf<ApplicationInfo>()
            val systemAppsInfo = mutableListOf<ApplicationInfo>()

            for (app in apps) {
                if ((app.flags and ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                    systemAppsInfo.add(app)
                    listener.malwareProgressMessage(
                        ScanMessage(
                            "Loading: ${app.packageName}",
                            ScannerType.BLACKLIST
                        )
                    )
                } else {
                    userAppsInfo.add(app)
                    ScanMessage("Loading: ${app.packageName}", ScannerType.BLACKLIST)
                }
            }

            val userApps = userAppsInfo.map { extractAppInfo(pm, it) }
            val systemApps = systemAppsInfo.map { extractAppInfo(pm, it) }
            val appsList = AppListState(systemApps, userApps)
            return appsList

        } catch (e: Exception) {
            return AppListState(listOf(), listOf())
        }
    }

    private fun extractAppInfo(pm: PackageManager, appInfo: ApplicationInfo): AppInfo {
        val packageInfo = pm.getPackageInfo(appInfo.packageName, PackageManager.GET_SIGNATURES)
        val name = pm.getApplicationLabel(appInfo).toString()

        val versionCode = packageInfo.versionCode
        val versionName = packageInfo.versionName ?: "unknown"
        val icon = appInfo.loadIcon(pm)
        val signatureHash = calculateSHA256(packageInfo.applicationInfo.sourceDir)

        val apkSize = getApkSize(packageInfo)
        val apkInfo = if (signatureHash != null && apkSize != -1L) {
            ApkInfo(signatureHash, apkSize, appInfo.packageName)
        } else null

        if (appInfo.packageName.contains("org.xmlpush.v3")) {
            Log.d("AppRepository", "signature: $signatureHash, size: $apkSize")
        }

        return AppInfo(name, appInfo.packageName, versionName, versionCode.toLong(), icon, apkInfo)
    }

    private fun getApkSize(packageInfo: PackageInfo): Long {
        try {
            val applicationInfo: ApplicationInfo = packageInfo.applicationInfo
            val sourceDir = applicationInfo.sourceDir
            return getFileSize(sourceDir)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return -1
    }

    private fun getFileSize(filePath: String): Long {
        val file = java.io.File(filePath)
        return if (file.exists()) {
            file.length()
        } else {
            -1
        }
    }

    private fun calculateSHA256(filePath: String): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val inputStream: InputStream = FileInputStream(filePath)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            val hashBytes = digest.digest()

            // Convert the byte array to a hexadecimal string
            val sb = StringBuilder()
            for (hashByte in hashBytes) {
                sb.append(((hashByte.toInt() and 0xff) + 0x100).toString(16).substring(1))
            }
            sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun byteArrayToHexString(byteArray: ByteArray): String {
        val hexChars = "0123456789ABCDEF"
        val hexString = StringBuilder()

        for (byte in byteArray) {
            val firstNibble = (byte.toInt() and 0xF0) ushr 4
            val secondNibble = byte.toInt() and 0x0F

            hexString.append(hexChars[firstNibble])
            hexString.append(hexChars[secondNibble])
        }

        return hexString.toString()
    }

}
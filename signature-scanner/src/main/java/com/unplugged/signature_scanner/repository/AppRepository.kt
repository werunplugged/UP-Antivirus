package com.unplugged.signature_scanner.repository

import com.unplugged.upantiviruscommon.model.ApkInfo
import com.unplugged.upantiviruscommon.malware.MalwareScannerListener
import com.unplugged.upantiviruscommon.model.AppInfo
import com.unplugged.upantiviruscommon.model.AppListState


interface AppRepository {
    suspend fun getInstalledApp(packageName: String): AppInfo?
    suspend fun getInstalledApps(listener: MalwareScannerListener): AppListState
    suspend fun getBlackListedPackages(): HashSet<ApkInfo>
    suspend fun isBlacklistedApp(apkInfo: ApkInfo?): Boolean
}
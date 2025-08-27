package com.unplugged.up_antivirus.domain.use_case

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat.getString
import com.unplugged.antivirus.R
import com.unplugged.upantiviruscommon.malware.MalwareScannerListener
import com.unplugged.upantiviruscommon.malware.ScanMessage
import com.unplugged.upantiviruscommon.model.ScannerType
import javax.inject.Inject


class GetUserAppListForTrackersUseCase @Inject constructor(private val context: Context) {
    operator fun invoke(listener: MalwareScannerListener): MutableList<String> {
        return try {
            listener.malwareProgressMessage(
                ScanMessage(
                    getString(context, R.string.up_av_loading_apps),
                    ScannerType.TRACKERS
                )
            )

            val pm = context.packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

            val userAppsInfo = apps.filter {
                (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 && it.packageName != "com.unplugged.messenger"
            }.map {
                it.packageName
            }.toMutableList()

            userAppsInfo
        } catch (e: Exception) {
            listener.malwareProgressMessage(
                ScanMessage(
                    getString(context, R.string.up_av_error_fetching_apps_try_later),
                    ScannerType.TRACKERS
                )
            )
            mutableListOf()
        }
    }
}
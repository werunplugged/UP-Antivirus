package com.unplugged.up_antivirus.domain.use_case

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.unplugged.upantiviruscommon.model.AppInfo
import javax.inject.Inject


class GetApplicationInfoUseCase @Inject constructor(private val context: Context) {
    operator fun invoke(packageName: String?): AppInfo? {
        if (packageName == null) return null

        val applicationInfo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } else {
                context.packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA
                )
            }

        val name = context.packageManager.getApplicationLabel(applicationInfo).toString()
        val icon = applicationInfo.loadIcon(context.packageManager)

        return AppInfo(name, packageName, "", 0, icon, null)
    }
}
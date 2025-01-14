package com.unplugged.up_antivirus.domain.use_case

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat.getDrawable
import com.unplugged.antivirus.R
import javax.inject.Inject


class GetApplicationIconUseCase @Inject constructor(private val context: Context) {
    operator fun invoke(packageName: String?): Drawable? {
        return if (packageName == null) {
            null
        } else {
            try {
                context.packageManager.getApplicationIcon(packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                getDrawable(context, R.drawable.baseline_do_not_disturb_alt_24)
            }
        }
    }
}
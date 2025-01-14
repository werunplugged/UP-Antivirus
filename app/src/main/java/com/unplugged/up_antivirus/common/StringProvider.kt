package com.unplugged.up_antivirus.common

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StringProvider @Inject constructor(@ApplicationContext context: Context) {
    private val resources: Resources = context.resources
    fun getString(stringRes: Int): String {
        return resources.getString(stringRes)
    }

    fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String {
        return resources.getString(resId, *formatArgs)
    }
}
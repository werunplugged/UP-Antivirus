package com.unplugged.up_antivirus.common

import android.content.Context
import android.content.Intent
import android.database.Cursor
import com.unplugged.up_antivirus.ui.splash.SplashActivity

fun Context.restartApplication() {
    Intent(this, SplashActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }
}

fun Cursor.getString(columnName: String): String? {
    val index = getColumnIndex(columnName)
    return this.getString(index)
}

fun Cursor.getInt(columnName: String): Int {
    val index = getColumnIndex(columnName)
    return this.getInt(index)
}

fun Cursor.getLong(columnName: String): Long {
    val index = getColumnIndex(columnName)
    return this.getLong(index)
}
package com.unplugged.up_antivirus.common

import android.database.Cursor

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
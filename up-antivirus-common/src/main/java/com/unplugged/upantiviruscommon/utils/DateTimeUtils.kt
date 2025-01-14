package com.unplugged.upantiviruscommon.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    private val dateFormatter = SimpleDateFormat("d, MMM, yyyy HH:mm", Locale.ENGLISH)

    fun stringToDate(str: String): Date? {
        return try {
            val date = dateFormatter.parse(str)
            date
        } catch (e: ParseException) {
            null
        }
    }

    fun getDateTimeString(date: Date): String {
        val dateFormat = SimpleDateFormat("d, MMM, yyyy HH:mm", Locale.ENGLISH)
        val dateString = dateFormat.format(date)
        return dateString
    }

    fun getCurrentDateTimeString(): String {
        val date = Calendar.getInstance().time
        return getDateTimeString(date)
    }

    fun getDateString(date: Date?): String {
        if (date == null)
            return "Unknown"
        val dateFormat = SimpleDateFormat("d, MMM, yyyy", Locale.ENGLISH)
        val dateString = dateFormat.format(date)
        return dateString
    }

    fun getTimeString(date: Date?): String {
        if (date == null)
            return "Unknown"
        val dateFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        val dateString = dateFormat.format(date)
        return dateString
    }

    fun getCurrentTimeString(): String {
        val date = Calendar.getInstance().time
        return getTimeString(date)
    }
}

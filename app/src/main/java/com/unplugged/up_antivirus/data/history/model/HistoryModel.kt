package com.unplugged.up_antivirus.data.history.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryModel(
    val id: Int,
    val name: String,
    val date: String,
    val malwareFound: Int,
    val trackersFound: Int,
    val filesScanned: Int,
    val appsScanned: Int,
    val megabytesHashed: Long
) : Parcelable
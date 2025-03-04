package com.example.trackerextension

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrackerModel(
    val id: Int,
    var scanId: Int,
    var appName: String,
    var packageId: String,
    var trackers: String
): Parcelable

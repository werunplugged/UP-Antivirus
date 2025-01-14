package com.example.trackerextension

data class TrackerModel(
    val id: Int,
    var scanId: Int,
    var appName: String,
    var packageId: String,
    var trackers: String
)

package com.example.trackerextension

interface TrackerListener {
    fun onTrackerFound(tracker: TrackerScanUpdate)
    fun trackerOnFinish()
    fun trackerOnProgress(progress: Double)
}
package com.example.trackerextension

data class TrackerScanUpdate(
    val upTracker: TrackersAnalyseResult,
    val progress: Double = 0.0
)
package com.example.trackerextension

interface TrackersAccessPoint {
    fun isRunning(): Boolean
    fun setRunning(running: Boolean)
    fun cancel()
    suspend fun analyse(appIds: List<String>, listener: TrackerListener)
}
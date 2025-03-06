package com.unplugged.up_antivirus.data.tracker.model

import android.content.Context
import com.example.trackerextension.Tracker

interface TrackerDetailsRepository {
    suspend fun getTrackerDetails(context: Context): Map<String, TrackerDetails>
    suspend fun getTrackerDetailsByNames(context: Context, trackerNames: List<Tracker>): List<TrackerDetails>
}
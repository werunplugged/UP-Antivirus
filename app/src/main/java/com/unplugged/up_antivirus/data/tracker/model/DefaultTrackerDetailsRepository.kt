package com.unplugged.up_antivirus.data.tracker.model

import android.content.Context
import com.example.trackerextension.Tracker
import javax.inject.Inject

class DefaultTrackerDetailsRepository @Inject constructor() : TrackerDetailsRepository {

    override suspend fun getTrackerDetails(context: Context): Map<String, TrackerDetails> {
        return TrackerDetailsJsonHelper.getTrackerDetails(context, "trackers_with_details.json")
    }

    override suspend fun getTrackerDetailsByNames(context: Context, trackerNames: List<Tracker>): List<TrackerDetails> {
        return TrackerDetailsJsonHelper.getTrackerDetailsByNames(context, trackerNames)
    }
}
package com.unplugged.up_antivirus.data.tracker

import androidx.lifecycle.LiveData
import com.example.trackerextension.Tracker
import com.example.trackerextension.TrackerModel
import kotlinx.coroutines.flow.Flow


interface TrackerRepository {
    fun observeTrackersByScanId(scanId: Int): LiveData<List<TrackerModel>>
    fun getByScanId(scanId: Int): Flow<List<TrackerModel>>
    suspend fun insert(tracker: TrackerModel)
    suspend fun insert(trackers: List<TrackerModel>)
    suspend fun update(tracker: TrackerModel)
    suspend fun delete(tracker: TrackerModel)
    suspend fun deleteAllForScan(scanId: Int)
    suspend fun deleteAll()
}
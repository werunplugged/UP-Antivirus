package com.unplugged.up_antivirus.data.tracker

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import com.unplugged.up_antivirus.data.tracker.model.TrackerEntity

interface TrackerLocalSource {
    fun observeTrackersByScanId(scanId: Int): LiveData<List<TrackerEntity>>
    fun getByScanId(scanId: Int): Flow<List<TrackerEntity>>
    suspend fun insert(tracker: TrackerEntity)
    suspend fun insert(trackers: List<TrackerEntity>)
    suspend fun update(tracker: TrackerEntity)
    suspend fun delete(tracker: TrackerEntity)
    suspend fun deleteAllForScan(scanId: Int)
    suspend fun deleteAll()
}
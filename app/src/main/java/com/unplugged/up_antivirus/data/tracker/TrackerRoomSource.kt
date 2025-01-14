package com.unplugged.up_antivirus.data.tracker

import androidx.lifecycle.LiveData
import com.unplugged.up_antivirus.data.tracker.model.TrackerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TrackerRoomSource @Inject constructor(
    private val dao: TrackerDao
) : TrackerLocalSource {
    override fun observeTrackersByScanId(scanId: Int): LiveData<List<TrackerEntity>> {
        return dao.observeTrackers(scanId)
    }
    override fun getByScanId(scanId: Int): Flow<List<TrackerEntity>> {
        return dao.getByScanId(scanId)
    }

    override suspend fun insert(tracker: TrackerEntity) {
        withContext(Dispatchers.IO) {
            dao.insert(tracker)
        }
    }

    override suspend fun insert(trackers: List<TrackerEntity>) {
        withContext(Dispatchers.IO) {
            dao.insert(trackers)
        }
    }

    override suspend fun update(tracker: TrackerEntity) {
        withContext(Dispatchers.IO) {
            dao.update(tracker)
        }
    }

    override suspend fun delete(tracker: TrackerEntity) {
        withContext(Dispatchers.IO) {
            dao.delete(tracker)
        }
    }

    override suspend fun deleteAllForScan(scanId: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteAllForScan(scanId)
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}
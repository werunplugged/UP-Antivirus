package com.unplugged.up_antivirus.data.tracker


import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.unplugged.up_antivirus.data.tracker.model.TrackerMapper
import com.example.trackerextension.TrackerModel
import javax.inject.Inject
import androidx.lifecycle.map

class DefaultTrackerRepository @Inject constructor(
    private val localSource: TrackerLocalSource,
    private val mapper: TrackerMapper
) : TrackerRepository {

    override fun observeTrackersByScanId(scanId: Int): LiveData<List<TrackerModel>> {
        return localSource.observeTrackersByScanId(scanId).map {
            mapper.entityToModel(it)
        }
    }

    override fun getByScanId(scanId: Int): Flow<List<TrackerModel>> {
        return localSource.getByScanId(scanId).map { mapper.entityToModel(it) }
    }

    override suspend fun insert(tracker: TrackerModel) {
        localSource.insert(mapper.modelToEntity(tracker))
    }

    override suspend fun insert(trackers: List<TrackerModel>) {
        localSource.insert(mapper.modelToEntity(trackers))
    }

    override suspend fun update(tracker: TrackerModel) {
        localSource.update(mapper.modelToEntity(tracker))
    }

    override suspend fun delete(tracker: TrackerModel) {
        localSource.delete(mapper.modelToEntity(tracker))
    }

    override suspend fun deleteAllForScan(scanId: Int) {
        localSource.deleteAllForScan(scanId)
    }

    override suspend fun deleteAll() {
        localSource.deleteAll()
    }
}
package com.unplugged.up_antivirus.data.history


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.unplugged.up_antivirus.data.history.model.HistoryMapper
import com.unplugged.up_antivirus.data.history.model.HistoryModel
import com.unplugged.up_antivirus.data.history.model.ScanStatus
import javax.inject.Inject

class DefaultHistoryRepository @Inject constructor(
    private val localSource: HistoryLocalSource,
    private val mapper: HistoryMapper
) : HistoryRepository {

    /**
     * Reclassifying rows left RUNNING by a dead process must happen at most once per process, and
     * must finish before a new scan inserts its own RUNNING row - otherwise a scheduled scan that
     * cold-starts the process could have its brand-new row swept to INTERRUPTED. Both the startup
     * sweep and createScanId() await this same lazily-started job, so whoever gets there first
     * runs it and the other waits.
     */
    private val interruptedSweep: Deferred<Int> =
        CoroutineScope(Dispatchers.IO).async(start = CoroutineStart.LAZY) {
            localSource.markRunningAsInterrupted()
        }

    override suspend fun getAll(): List<HistoryModel> {
        val historyEntities = localSource.getAll()
        return mapper.entityToModel(historyEntities)
    }

    override fun getLatestAsFlow(): Flow<HistoryModel?> {
        return localSource.getLatestAsFlow().map {
            it?.let {
                mapper.entityToModel(it)
            }
        }
    }

    override suspend fun getLatest(): HistoryModel? {
        return localSource.getLatest()?.let {
            mapper.entityToModel(it)
        }
    }

    override suspend fun insert(model: HistoryModel): Int {
        val entity = mapper.modelToEntity(model)
        return localSource.insert(entity)
    }

    override suspend fun update(model: HistoryModel) {
        val entity = mapper.modelToEntity(model)
        return localSource.update(entity)
    }

    override suspend fun getLastEntryId(): Int? {
        return localSource.getLastEntryId()
    }

    override suspend fun delete(model: HistoryModel) {
        val entity = mapper.modelToEntity(model)
        localSource.delete(entity)
    }

    override suspend fun deleteAll() {
        localSource.deleteAll()
    }

    override suspend fun getHistoryById(id: Int): HistoryModel? {
        return localSource.getHistoryById(id)?.let {
            mapper.entityToModel(it)
        }
    }

    override suspend fun updateStatus(id: Int, status: ScanStatus) {
        localSource.updateStatus(id, status)
    }

    override suspend fun updateProgress(model: HistoryModel) {
        localSource.updateProgress(mapper.modelToEntity(model))
    }

    override suspend fun markRunningAsInterrupted(): Int = interruptedSweep.await()

    override suspend fun awaitInterruptedSweep() {
        runCatching { interruptedSweep.await() }
    }
}
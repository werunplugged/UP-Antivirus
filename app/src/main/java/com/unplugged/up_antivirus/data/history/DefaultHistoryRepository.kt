package com.unplugged.up_antivirus.data.history


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.unplugged.up_antivirus.data.history.model.HistoryMapper
import com.unplugged.up_antivirus.data.history.model.HistoryModel
import javax.inject.Inject

class DefaultHistoryRepository @Inject constructor(
    private val localSource: HistoryLocalSource,
    private val mapper: HistoryMapper
) : HistoryRepository {

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
}
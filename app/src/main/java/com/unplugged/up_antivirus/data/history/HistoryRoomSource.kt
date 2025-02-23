package com.unplugged.up_antivirus.data.history

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.unplugged.up_antivirus.data.history.model.HistoryEntity
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class HistoryRoomSource @Inject constructor(
    private val dao: HistoryDao
) : HistoryLocalSource {

    override suspend fun getAll(): List<HistoryEntity> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }

    override fun getLatestAsFlow(): Flow<HistoryEntity?> {
        return dao.getLatestAsFlow()
    }

    override suspend fun getLatest(): HistoryEntity? {
        return withContext(Dispatchers.IO) {
            dao.getLatest()
        }
    }

    override suspend fun insert(entity: HistoryEntity): Int {
        return withContext(Dispatchers.IO) {
            dao.insert(entity).toInt()
        }
    }

    override suspend fun update(entity: HistoryEntity) {
        return withContext(Dispatchers.IO) {
            dao.update(entity)
        }
    }

    override suspend fun getLastEntryId(): Int? {
        return withContext(Dispatchers.IO) {
            dao.getLastEntryId()
        }
    }

    override suspend fun delete(entity: HistoryEntity) {
        withContext(Dispatchers.IO) {
            dao.delete(entity)
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }

    override suspend fun getHistoryById(id: Int): HistoryEntity? {
        return withContext(Dispatchers.IO) {
            dao.getHistoryById(id)
        }
    }
}
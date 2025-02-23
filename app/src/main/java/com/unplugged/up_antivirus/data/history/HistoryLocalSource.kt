package com.unplugged.up_antivirus.data.history

import kotlinx.coroutines.flow.Flow
import com.unplugged.up_antivirus.data.history.model.HistoryEntity

interface HistoryLocalSource {
    suspend fun getAll(): List<HistoryEntity>
    fun getLatestAsFlow(): Flow<HistoryEntity?>
    suspend fun getLatest(): HistoryEntity?
    suspend fun insert(entity: HistoryEntity): Int
    suspend fun update(entity: HistoryEntity)
    suspend fun getLastEntryId(): Int?
    suspend fun delete(entity: HistoryEntity)
    suspend fun deleteAll()
    suspend fun getHistoryById(id: Int): HistoryEntity?
}
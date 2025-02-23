package com.unplugged.up_antivirus.data.history

import com.unplugged.up_antivirus.data.history.model.HistoryModel
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun getAll(): List<HistoryModel>
    fun getLatestAsFlow(): Flow<HistoryModel?>
    suspend fun getLatest(): HistoryModel?
    suspend fun insert(model: HistoryModel): Int
    suspend fun update(model: HistoryModel)
    suspend fun getLastEntryId(): Int?
    suspend fun delete(model: HistoryModel)
    suspend fun deleteAll()
    suspend fun getHistoryById(id: Int): HistoryModel?
}
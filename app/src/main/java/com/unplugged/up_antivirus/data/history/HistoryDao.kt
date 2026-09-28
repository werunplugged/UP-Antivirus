package com.unplugged.up_antivirus.data.history

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.unplugged.up_antivirus.data.history.model.HistoryEntity

@Dao
interface HistoryDao {
    @Query("SELECT * FROM historyentity")
    suspend fun getAll(): List<HistoryEntity>

    @Query("SELECT * FROM historyentity WHERE status = :completed ORDER BY date DESC LIMIT 1")
    fun getLatestCompletedAsFlow(completed: Int): Flow<HistoryEntity?>

    @Query("SELECT * FROM historyentity WHERE status = :completed ORDER BY date DESC LIMIT 1")
    suspend fun getLatestCompleted(completed: Int): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoryEntity): Long

    @Update
    suspend fun update(entity: HistoryEntity)

    @Query("SELECT id FROM historyentity ORDER BY date DESC LIMIT 1")
    suspend fun getLastEntryId(): Int?

    @Delete
    suspend fun delete(entity: HistoryEntity)

    @Query("DELETE FROM historyentity")
    suspend fun deleteAll()

    @Query("SELECT * FROM historyentity WHERE id = :id")
    suspend fun getHistoryById(id: Int): HistoryEntity?

    @Query("UPDATE historyentity SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: Int)

    /**
     * Nothing can still be RUNNING in a freshly started process, so any such row belongs to a scan
     * whose process died (UNP-8704). Returns the number of rows swept.
     */
    @Query("UPDATE historyentity SET status = :interrupted WHERE status = :running")
    suspend fun markRunningAsInterrupted(running: Int, interrupted: Int): Int
}
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

    /**
     * Newest scan that is running or finished. Cancelled and interrupted rows are excluded: they
     * used to be deleted, so this could not land on one, but they are kept now (UNP-8704).
     */
    @Query("SELECT id FROM historyentity WHERE status IN (:statuses) ORDER BY date DESC LIMIT 1")
    suspend fun getLastEntryId(statuses: List<Int>): Int?

    @Delete
    suspend fun delete(entity: HistoryEntity)

    @Query("DELETE FROM historyentity")
    suspend fun deleteAll()

    @Query("SELECT * FROM historyentity WHERE id = :id")
    suspend fun getHistoryById(id: Int): HistoryEntity?

    @Query("UPDATE historyentity SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: Int)

    /**
     * Counts-only update. Deliberately leaves `status` alone so an in-flight progress write can
     * never clobber a CANCELLED or COMPLETED transition - status is owned solely by updateStatus().
     */
    @Query("""
        UPDATE historyentity
        SET name = :name, date = :date, malwareFound = :malwareFound,
            trackersFound = :trackersFound, filesScanned = :filesScanned,
            appsScanned = :appsScanned, megabytesHashed = :megabytesHashed
        WHERE id = :id
    """)
    suspend fun updateProgress(
        id: Int,
        name: String,
        date: String,
        malwareFound: Int,
        trackersFound: Int,
        filesScanned: Int,
        appsScanned: Int,
        megabytesHashed: Long
    )

    /**
     * Nothing can still be RUNNING in a freshly started process, so any such row belongs to a scan
     * whose process died (UNP-8704). Returns the number of rows swept.
     */
    @Query("UPDATE historyentity SET status = :interrupted WHERE status = :running")
    suspend fun markRunningAsInterrupted(running: Int, interrupted: Int): Int
}
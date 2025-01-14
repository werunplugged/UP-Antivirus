package com.unplugged.up_antivirus.data.history

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.unplugged.up_antivirus.data.history.model.HistoryEntity

@Dao
interface HistoryDao {
    @Query("SELECT * FROM historyentity")
    suspend fun getAll(): List<HistoryEntity>

    @Query("SELECT * FROM historyentity ORDER BY date DESC LIMIT 1")
    fun getLatestAsFlow(): Flow<HistoryEntity?>

    @Query("SELECT * FROM historyentity ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): HistoryEntity?

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
}
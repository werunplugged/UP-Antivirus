package com.unplugged.up_antivirus.data.tracker

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.unplugged.up_antivirus.data.tracker.model.TrackerEntity


@Dao
interface TrackerDao {
    @Query("SELECT * FROM TrackerEntity WHERE scanId = :scanId")
    fun observeTrackers(scanId: Int): LiveData<List<TrackerEntity>>

    @Query("SELECT * FROM TrackerEntity WHERE scanId = :scanId")
    fun getByScanId(scanId: Int): Flow<List<TrackerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tracker: TrackerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trackers: List<TrackerEntity>)

    @Update
    suspend fun update(tracker: TrackerEntity)

    @Delete
    suspend fun delete(tracker: TrackerEntity)

    @Query("DELETE FROM TrackerEntity WHERE scanId = :scanId")
    suspend fun deleteAllForScan(scanId: Int)

    @Query("DELETE FROM TrackerEntity")
    suspend fun deleteAll()
}
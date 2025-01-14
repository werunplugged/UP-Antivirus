package com.unplugged.signature_scanner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.unplugged.signature_scanner.model.BlacklistPackageEntity

@Dao
interface BlacklistedPackageDao {
    @Query("SELECT * FROM BlacklistPackageEntity")
    suspend fun getAll(): List<BlacklistPackageEntity>
    @Query("SELECT * FROM BlacklistPackageEntity where packageName =:packageName limit 1")
    suspend fun find(packageName: String): BlacklistPackageEntity?
    @Query("SELECT * FROM BlacklistPackageEntity where sha256 =:sha256 and size =:size and packageName =:packageName limit 1")
    suspend fun findBy(sha256: String, size: Long, packageName: String): BlacklistPackageEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BlacklistPackageEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(packages: List<BlacklistPackageEntity>)
    @Delete
    suspend fun delete(entity: BlacklistPackageEntity)
    @Query("DELETE from BlacklistPackageEntity")
    suspend fun deleteAll()
}

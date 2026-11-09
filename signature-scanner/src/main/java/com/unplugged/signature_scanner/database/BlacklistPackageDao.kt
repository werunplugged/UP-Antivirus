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
    // UNP-9907: match on the hash alone. size is derived from the same file as sha256, so it
    // adds no selectivity, and packageName was silently breaking ~12% of lookups - the blacklist
    // loader lowercases it (AppRepositoryImpl.readCsvFile) while the scan path passes the real
    // name from PackageManager, and this column has no COLLATE NOCASE.
    @Query("SELECT * FROM BlacklistPackageEntity where sha256 =:sha256 limit 1")
    suspend fun findBy(sha256: String): BlacklistPackageEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BlacklistPackageEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(packages: List<BlacklistPackageEntity>)
    @Delete
    suspend fun delete(entity: BlacklistPackageEntity)
    @Query("DELETE from BlacklistPackageEntity")
    suspend fun deleteAll()
}

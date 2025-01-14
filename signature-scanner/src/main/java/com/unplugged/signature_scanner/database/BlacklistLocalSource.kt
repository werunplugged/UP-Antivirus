package com.unplugged.signature_scanner.database

import com.unplugged.signature_scanner.model.BlacklistPackageEntity

interface BlacklistLocalSource {
    suspend fun getAll(): List<BlacklistPackageEntity>
    suspend fun insert(entity: BlacklistPackageEntity): Int
    suspend fun insertAll(entity: List<BlacklistPackageEntity>)
    suspend fun delete(entity: BlacklistPackageEntity)
    suspend fun find(packageName: String): BlacklistPackageEntity?
    suspend fun findBy(sha256: String, length: Long, packageName: String): BlacklistPackageEntity?
    suspend fun deleteAll()
}
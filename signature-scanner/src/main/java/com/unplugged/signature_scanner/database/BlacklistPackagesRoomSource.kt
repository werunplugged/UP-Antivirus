package com.unplugged.signature_scanner.database

import kotlinx.coroutines.withContext
import com.unplugged.signature_scanner.model.BlacklistPackageEntity
import kotlinx.coroutines.Dispatchers

class BlacklistPackagesRoomSource(
    private val dao: BlacklistedPackageDao,
) : BlacklistLocalSource {
    override suspend fun getAll(): List<BlacklistPackageEntity> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }
    override suspend fun insert(entity: BlacklistPackageEntity): Int {
        return withContext(Dispatchers.IO) {
            dao.insert(entity).toInt()
        }
    }
    override suspend fun insertAll(entity: List<BlacklistPackageEntity>) {
        return withContext(Dispatchers.IO) {
            dao.insertAll(entity)
        }
    }
    override suspend fun delete(entity: BlacklistPackageEntity) {
        withContext(Dispatchers.IO) {
            dao.delete(entity)
        }
    }
    override suspend fun find(packageName: String): BlacklistPackageEntity? {
        return withContext(Dispatchers.IO) {
            dao.find(packageName)
        }
    }
    override suspend fun findBy(
        sha256: String,
        length: Long,
        packageName: String
    ): BlacklistPackageEntity? {
        return withContext(Dispatchers.IO) {
            dao.findBy(sha256, length, packageName)
        }
    }
    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}
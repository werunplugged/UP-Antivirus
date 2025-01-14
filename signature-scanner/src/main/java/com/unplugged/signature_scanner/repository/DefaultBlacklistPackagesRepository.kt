package com.unplugged.signature_scanner.repository


import com.unplugged.signature_scanner.database.BlacklistLocalSource
import com.unplugged.upantiviruscommon.model.ApkInfo
import com.unplugged.signature_scanner.model.BlacklistMapper

class DefaultBlacklistPackagesRepository(
    private val localSource: BlacklistLocalSource,
    private val mapper: BlacklistMapper
) : BlacklistPackageRepository {

    override suspend fun getAll(): List<ApkInfo> {
        val blacklistEntities = localSource.getAll()
        return mapper.entityToModel(blacklistEntities)
    }
    override suspend fun insert(apkInfo: ApkInfo): Int {
        val entity = mapper.modelToEntity(apkInfo)
        return localSource.insert(entity)
    }
    override suspend fun insertAll(apkInfo: List<ApkInfo>) {
        val entities = mapper.modelToEntity(apkInfo)
        return localSource.insertAll(entities)
    }
    override suspend fun delete(apkInfo: ApkInfo) {
        val entity = mapper.modelToEntity(apkInfo)
        return localSource.delete(entity)
    }
    override suspend fun find(packageName: String): ApkInfo? {
        val packageInfo = localSource.find(packageName)
        return packageInfo?.let {
            mapper.entityToModel(packageInfo)
        }
    }
    override suspend fun findBy(sha256: String, length: Long, packageName: String): ApkInfo? {
        val blacklistApp = localSource.findBy(sha256, length, packageName)
        return blacklistApp?.let {
            mapper.entityToModel(blacklistApp)
        }
    }
    override suspend fun deleteAll() {
        localSource.deleteAll()
    }
}
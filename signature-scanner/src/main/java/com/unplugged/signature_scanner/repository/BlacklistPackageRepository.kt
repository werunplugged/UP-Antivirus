package com.unplugged.signature_scanner.repository

import com.unplugged.upantiviruscommon.model.ApkInfo

interface BlacklistPackageRepository {
    suspend fun getAll(): List<ApkInfo>
    suspend fun insert(apkInfo: ApkInfo): Int
    suspend fun insertAll(apkInfo: List<ApkInfo>)
    suspend fun delete(apkInfo: ApkInfo)
    suspend fun find(packageName: String): ApkInfo?
    suspend fun findBy(sha256: String, length: Long, packageName: String): ApkInfo?
    suspend fun deleteAll()
}
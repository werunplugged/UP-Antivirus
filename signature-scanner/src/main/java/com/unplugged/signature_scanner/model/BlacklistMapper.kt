package com.unplugged.signature_scanner.model

import com.unplugged.upantiviruscommon.model.ApkInfo


interface BlacklistMapper {
    fun entityToModel(entity: BlacklistPackageEntity): ApkInfo
    fun entityToModel(entities: List<BlacklistPackageEntity>): List<ApkInfo>
    fun modelToEntity(model: ApkInfo): BlacklistPackageEntity
    fun modelToEntity(models: List<ApkInfo>): List<BlacklistPackageEntity>
}
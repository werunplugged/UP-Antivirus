package com.unplugged.signature_scanner.model

import com.unplugged.upantiviruscommon.model.ApkInfo


class DefaultBlacklistPackageMapper : BlacklistMapper {
    override fun entityToModel(entity: BlacklistPackageEntity): ApkInfo {
        return with(entity) {
            ApkInfo(sha256, size, packageName)
        }
    }
    override fun entityToModel(entities: List<BlacklistPackageEntity>): List<ApkInfo> {
        return entities.map { entityToModel(it) }
    }
    override fun modelToEntity(model: ApkInfo): BlacklistPackageEntity {
        return with(model) {
            BlacklistPackageEntity(sha256 = sha256, size = size, packageName = packageName)
        }
    }
    override fun modelToEntity(models: List<ApkInfo>): List<BlacklistPackageEntity> {
        return models.map { modelToEntity(it) }
    }
}
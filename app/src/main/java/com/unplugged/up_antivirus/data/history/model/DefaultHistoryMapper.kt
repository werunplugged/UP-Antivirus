package com.unplugged.up_antivirus.data.history.model

class DefaultHistoryMapper : HistoryMapper {
    override fun entityToModel(entity: HistoryEntity): HistoryModel {
        return with(entity) {
            HistoryModel(id, name, date, malwareFound, trackersFound, filesScanned, megabytesHashed)
        }
    }

    override fun entityToModel(entities: List<HistoryEntity>): List<HistoryModel> {
        return entities.map { entityToModel(it) }
    }

    override fun modelToEntity(model: HistoryModel): HistoryEntity {
        return with(model) {
            HistoryEntity(id, name, date, malwareFound, trackersFound, filesScanned, megabytesHashed)
        }
    }

    override fun modelToEntity(models: List<HistoryModel>): List<HistoryEntity> {
        return models.map { modelToEntity(it) }
    }
}
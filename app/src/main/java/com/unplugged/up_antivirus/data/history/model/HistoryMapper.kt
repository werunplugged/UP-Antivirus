package com.unplugged.up_antivirus.data.history.model

interface HistoryMapper {
    fun entityToModel(entity: HistoryEntity): HistoryModel
    fun entityToModel(entities: List<HistoryEntity>): List<HistoryModel>
    fun modelToEntity(model: HistoryModel): HistoryEntity
    fun modelToEntity(models: List<HistoryModel>): List<HistoryEntity>
}
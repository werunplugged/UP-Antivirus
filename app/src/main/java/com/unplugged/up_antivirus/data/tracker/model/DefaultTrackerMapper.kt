package com.unplugged.up_antivirus.data.tracker.model

import com.example.trackerextension.TrackerModel


class DefaultTrackerMapper : TrackerMapper {
    override fun entityToModel(entity: TrackerEntity): TrackerModel {
        return with(entity) {
            TrackerModel(id, scanId, appName, packageId, trackers)
        }
    }

    override fun entityToModel(entities: List<TrackerEntity>): List<TrackerModel> {
        return entities.map { entityToModel(it) }
    }

    override fun modelToEntity(model: TrackerModel): TrackerEntity {
        return with(model) {
            TrackerEntity(id, scanId, appName, packageId, trackers)
        }
    }

    override fun modelToEntity(models: List<TrackerModel>): List<TrackerEntity> {
        return models.map { modelToEntity(it) }
    }
}
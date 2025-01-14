package com.unplugged.up_antivirus.data.tracker.model

import com.example.trackerextension.TrackerModel


interface TrackerMapper {
    fun entityToModel(entity: TrackerEntity): TrackerModel
    fun entityToModel(entities: List<TrackerEntity>): List<TrackerModel>
    fun modelToEntity(model: TrackerModel): TrackerEntity
    fun modelToEntity(models: List<TrackerModel>): List<TrackerEntity>
}
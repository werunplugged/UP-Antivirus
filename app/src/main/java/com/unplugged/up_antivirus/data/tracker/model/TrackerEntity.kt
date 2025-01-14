package com.unplugged.up_antivirus.data.tracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrackerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val scanId: Int,
    val appName: String,
    val packageId: String,
    val trackers: String
)
package com.unplugged.up_antivirus.data.history.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val date: String,
    val malwareFound: Int,
    val trackersFound: Int,
    val filesScanned: Int,
    val megabytesHashed: Long
)
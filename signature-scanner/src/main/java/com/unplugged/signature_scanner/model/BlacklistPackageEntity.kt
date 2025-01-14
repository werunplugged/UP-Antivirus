package com.unplugged.signature_scanner.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BlacklistPackageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val sha256: String,
    val size: Long,
    val packageName: String
)
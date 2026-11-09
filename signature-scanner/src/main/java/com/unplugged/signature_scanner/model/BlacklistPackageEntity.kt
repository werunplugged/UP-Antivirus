package com.unplugged.signature_scanner.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// UNP-9907: lookups go through sha256 alone, over ~833k rows, once per installed app - without
// this index every one of those is a full table scan.
@Entity(indices = [Index(value = ["sha256"])])
data class BlacklistPackageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val sha256: String,
    val size: Long,
    val packageName: String
)
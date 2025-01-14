package com.unplugged.upantiviruscommon.model

data class DatabaseInfo(
    val version: Int,
    val toAdd: List<ApkInfo>,
    val toRemove: List<ApkInfo>)

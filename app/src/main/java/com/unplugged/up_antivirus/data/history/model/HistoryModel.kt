package com.unplugged.up_antivirus.data.history.model

data class HistoryModel(
    val id: Int,
    val name: String,
    val date: String,
    val malwareFound: Int,
    val trackersFound: Int,
    val filesScanned: Int,
    val megabytesHashed: Long
)
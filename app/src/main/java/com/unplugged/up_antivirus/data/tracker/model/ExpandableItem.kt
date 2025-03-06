package com.unplugged.up_antivirus.data.tracker.model

data class ExpandableItem(
    val tracker: TrackerDetails,
    var isOpen: Boolean = false
)

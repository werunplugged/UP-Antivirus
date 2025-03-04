package com.unplugged.up_antivirus.data.tracker.model

data class ExpandableItem(
    val tracker: TrackerDescription,
    var isOpen: Boolean = false
)

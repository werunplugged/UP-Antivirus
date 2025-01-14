package com.unplugged.up_antivirus.data.tracker.model

import com.example.trackerextension.Tracker
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TrackerListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromTrackerList(value: List<Tracker>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toTrackerList(value: String): List<Tracker> {
        val listType = object : TypeToken<List<Tracker>>() {}.type
        return gson.fromJson(value, listType)
    }
}
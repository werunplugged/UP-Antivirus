package com.unplugged.up_antivirus.data.tracker.model

import android.content.Context
import com.example.trackerextension.Tracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import kotlin.text.split
import kotlin.text.trim

object TrackerDetailsJsonHelper {
    private var cachedTrackerDescriptions: Map<String, TrackerDetails>? = null
    private var cachedFileName: String? = null

    suspend fun getTrackerDetails(context: Context, fileName: String): Map<String, TrackerDetails> {
        if (cachedTrackerDescriptions == null || cachedFileName != fileName) {
            cachedTrackerDescriptions = loadTrackerDetails(context, fileName)
            cachedFileName = fileName
        }
        return cachedTrackerDescriptions!!
    }

    suspend fun getTrackerDetailsByNames(
        context: Context,
        trackerNames: List<Tracker>
    ): List<TrackerDetails> {
        val allTrackers = getTrackerDetails(context, cachedFileName!!)
        return trackerNames.mapNotNull { trackerName ->
            allTrackers[trackerName.name]
        }
    }

    private suspend fun loadTrackerDetails(context: Context, fileName: String): Map<String, TrackerDetails> = withContext(Dispatchers.IO) {
        val trackerDescriptions = mutableMapOf<String, TrackerDetails>()
        try {
            val jsonString = getJsonFromAssets(context, fileName)
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val trackerName = if(jsonObject.getString("name").isNotEmpty()) jsonObject.getString("name") else fileName
                val description = if(jsonObject.getString("description").isNotEmpty()) jsonObject.getString("description") else "No description available"
                val website = jsonObject.getString("website")
                val category = jsonObject.getString("category")

                val categories = category.split(",").map { it.trim() }

                val trackerDetails = TrackerDetails(trackerName, categories, description, website)
                trackerDescriptions[trackerName] = trackerDetails
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        trackerDescriptions
    }

    private fun getJsonFromAssets(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }
}
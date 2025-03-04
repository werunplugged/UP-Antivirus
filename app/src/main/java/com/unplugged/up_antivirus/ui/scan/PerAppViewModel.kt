package com.unplugged.up_antivirus.ui.scan

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.trackerextension.TrackerModel
import com.unplugged.up_antivirus.data.tracker.model.ExpandableItem
import com.unplugged.up_antivirus.data.tracker.model.TrackerDescription
import com.unplugged.up_antivirus.data.tracker.model.TrackerListConverter
import com.unplugged.up_antivirus.domain.use_case.GetApplicationIconUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PerAppViewModel @Inject constructor(
    private val getApplicationIconUseCase: GetApplicationIconUseCase,
    private val trackerListConverter: TrackerListConverter
) :
    ViewModel() {

    private val _trackerList = MutableLiveData<List<ExpandableItem>>()
    val trackerList: LiveData<List<ExpandableItem>> = _trackerList

    fun getListOfTrackers(trackerModel: TrackerModel) {
        val listOfTrackers = getTrackersInfo(trackerModel)
        val expandableList: MutableList<ExpandableItem> = mutableListOf()
        for (tracker in listOfTrackers) {
            val item = ExpandableItem(tracker, false)
            expandableList.add(item)
        }
        _trackerList.postValue(expandableList)
    }

    fun getIcon(packageName: String?): Drawable? {
        return getApplicationIconUseCase(packageName)
    }

    private fun getTrackersInfo(trackerModel: TrackerModel): List<TrackerDescription> {
        val getTrackers = trackerListConverter.toTrackerList( trackerModel.trackers)
        val trackerList = mutableListOf<TrackerDescription>()
        val tags = listOf("tag1", "tag2", "tag3")
        //TODO: get trackers info from database
        for (tracker in getTrackers) {
            trackerList.add(
                TrackerDescription(
                    tracker.name,
                    tags,
                    "description",
                    "https://www.placeholder.com \n"
                            + "https://www.google.com \n"
                            + "www.one.co.il"
                )
            )
        }
        return trackerList
    }
}
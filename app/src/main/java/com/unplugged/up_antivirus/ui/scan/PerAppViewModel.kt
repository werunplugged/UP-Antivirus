package com.unplugged.up_antivirus.ui.scan

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.trackerextension.TrackerModel
import com.unplugged.up_antivirus.data.tracker.model.ExpandableItem
import com.unplugged.up_antivirus.data.tracker.model.TrackerDetailsRepository
import com.unplugged.up_antivirus.data.tracker.model.TrackerDetails
import com.unplugged.up_antivirus.data.tracker.model.TrackerListConverter
import com.unplugged.up_antivirus.domain.use_case.GetApplicationIconUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PerAppViewModel @Inject constructor(
    private val getApplicationIconUseCase: GetApplicationIconUseCase,
    private val trackerListConverter: TrackerListConverter,
    private val trackerDetailsRepository: TrackerDetailsRepository
) :
    ViewModel() {

    private val _expandableTrackerList = MutableLiveData<List<ExpandableItem>>()
    val trackerList: LiveData<List<ExpandableItem>> = _expandableTrackerList

    suspend fun getListOfTrackers(context: Context, trackerModel: TrackerModel) {
        val listOfTrackers = getTrackersInfo(context, trackerModel)
        val expandableList: MutableList<ExpandableItem> = mutableListOf()
        for (tracker in listOfTrackers) {
            val item = ExpandableItem(tracker, false)
            expandableList.add(item)
        }
        _expandableTrackerList.postValue(expandableList)
    }

    fun getIcon(packageName: String?): Drawable? {
        return getApplicationIconUseCase(packageName)
    }

    private suspend fun getTrackersInfo(context: Context, trackerModel: TrackerModel): List<TrackerDetails> {
        val trackerNames = trackerListConverter.toTrackerList(trackerModel.trackers)

        var trackerList: MutableList<TrackerDetails>
        withContext(Dispatchers.Main) {
            trackerList = trackerDetailsRepository.getTrackerDetailsByNames(context, trackerNames).toMutableList()
        }
        return trackerList
    }
}
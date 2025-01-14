package com.unplugged.up_antivirus.ui.scan

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.unplugged.antivirus.R
import com.example.trackerextension.TrackerModel
import com.unplugged.up_antivirus.data.tracker.model.TrackerListConverter
import com.unplugged.up_antivirus.domain.use_case.GetApplicationIconUseCase
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class TrackerAdapter @AssistedInject constructor(
    @Assisted private val clickListener: (tracker: TrackerModel) -> Unit,
    @Assisted private val trackers: MutableList<TrackerModel> = mutableListOf(),
    private val getApplicationIconUseCase: GetApplicationIconUseCase
) : RecyclerView.Adapter<TrackerAdapter.TrackerHolder>() {
    // Store a copy of the original list for filtering
    private val fullTrackerList: MutableList<TrackerModel> = mutableListOf()

    @AssistedInject.Factory
    interface Factory {
        fun create(
            clickListener: (tracker: TrackerModel) -> Unit,
            trackers: MutableList<TrackerModel> = mutableListOf()
        ): TrackerAdapter
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackerHolder {
        return TrackerHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.tracker_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TrackerHolder, position: Int) {
        val tracker = trackers[position]
        holder.bind(tracker, clickListener, getApplicationIconUseCase)
    }

    override fun getItemCount(): Int {
        return trackers.count()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTrackers(trackers: List<TrackerModel>) {
        this.trackers.clear()
        this.trackers.addAll(trackers)

        // Also keep a copy of the full list for filtering
        fullTrackerList.clear()
        fullTrackerList.addAll(trackers)

        notifyDataSetChanged()
    }

    // Filtering logic based on the app name
    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String?) {
        if (query.isNullOrEmpty()) {
            trackers.clear()
            trackers.addAll(fullTrackerList)  // Reset to full list if query is empty
        } else {
            val filteredList = fullTrackerList.filter {
                it.appName.contains(query, ignoreCase = true)
            }
            trackers.clear()
            trackers.addAll(filteredList)
        }
        notifyDataSetChanged()
    }

    class TrackerHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val appNameTv: TextView = view.findViewById(R.id.app_name)
        private val trackerCountTv: TextView = view.findViewById(R.id.tracker_count_tv)
        private val appIcon: ImageView = view.findViewById(R.id.app_icon)
        private val appNotInstalledInfoIcon: ImageView = view.findViewById(R.id.app_not_installed_info_icon)

        fun bind(
            tracker: TrackerModel,
            clickListener: (tracker: TrackerModel) -> Unit,
            getApplicationIconUseCase: GetApplicationIconUseCase
        ) {
            appNameTv.text = tracker.appName
            val trackersList = TrackerListConverter().toTrackerList(tracker.trackers)
            val trackersFound = itemView.context.getString(R.string.up_av_trackers_found)
            trackerCountTv.text = "$trackersFound ${trackersList.count()}"

            // Use the getApplicationIconUseCase to get the app icon and set it
            appIcon.setImageDrawable(getApplicationIconUseCase(tracker.packageId))

            // Check if the app is installed
            val packageManager = view.context.packageManager
            appNotInstalledInfoIcon.isVisible =
                !isPackageInstalled(tracker.packageId, packageManager)

            view.setOnClickListener {
                clickListener(tracker)
            }
        }

        private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
            return try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
}

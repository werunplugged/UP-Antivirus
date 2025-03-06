package com.unplugged.up_antivirus.ui.scan

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.trackerextension.TrackerModel
import com.unplugged.antivirus.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PerAppFragment: Fragment(R.layout.fragment_per_app) {

    private lateinit var closeButton: AppCompatButton
    private lateinit var appIcon: ImageView
    private lateinit var appTitle: TextView
    private lateinit var trackersIdentified: TextView
    private lateinit var expandableTrackerRecyclerView: RecyclerView
    private lateinit var infoButton: ImageView

    private lateinit var expandableTrackerAdapter : ExpandableTrackerAdapter

    private val viewModel: PerAppViewModel by viewModels()

    companion object {
        private const val ARG_TRACKER_MODEL = "trackerModel"
        fun newInstance(trackerModel: TrackerModel): PerAppFragment {
            val fragment = PerAppFragment()
            val args = Bundle()
            args.putParcelable(ARG_TRACKER_MODEL, trackerModel)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        val trackerModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_TRACKER_MODEL, TrackerModel::class.java)
        } else {
            arguments?.getParcelable(ARG_TRACKER_MODEL)
        }
        if (trackerModel != null) {
            lifecycleScope.launch {
                viewModel.getListOfTrackers(this@PerAppFragment.requireContext(), trackerModel)
            }
        }

        appIcon.setImageDrawable(viewModel.getIcon(trackerModel?.packageId))
        appTitle.text = trackerModel?.appName

        closeButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        infoButton.setOnClickListener {
            (requireActivity() as ScanResultsActivity).openInfoFragment()
        }

        observe()
    }

    private fun initViews(view: View) {
        closeButton = view.findViewById(R.id.close_button)
        infoButton = view.findViewById(R.id.info_button)
        appIcon = view.findViewById(R.id.app_icon)
        appTitle = view.findViewById(R.id.app_title)
        trackersIdentified = view.findViewById(R.id.trackers_identified_number)
        expandableTrackerRecyclerView = view.findViewById(R.id.expandable_tracker_recyclerview)
        expandableTrackerAdapter = ExpandableTrackerAdapter()
        expandableTrackerRecyclerView.adapter = expandableTrackerAdapter
    }

    private fun observe(){
        viewModel.trackerList.observe(viewLifecycleOwner){
            expandableTrackerAdapter.submitList(it)
            trackersIdentified.text = it.size.toString()
        }
    }
}
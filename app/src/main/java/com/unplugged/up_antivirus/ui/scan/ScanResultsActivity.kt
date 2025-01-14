package com.unplugged.up_antivirus.ui.scan

import android.animation.LayoutTransition
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trackerextension.TrackerModel
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.base.BaseActivity
import com.unplugged.up_antivirus.data.tracker.model.TrackerListConverter
import com.unplugged.up_antivirus.domain.use_case.GetApplicationIconUseCase
import com.unplugged.up_antivirus.ui.CellMarginDecoration
import com.unplugged.upantiviruscommon.malware.MalwareModel
import com.unplugged.upantiviruscommon.malware.ThreatStatus
import com.unplugged.upantiviruscommon.utils.Constants.SCAN_ID
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder
import javax.inject.Inject

@AndroidEntryPoint
class ScanResultsActivity : BaseActivity() {
    private lateinit var resultsRv: RecyclerView
    private val concatAdapter = ConcatAdapter()
    private lateinit var closeButton: AppCompatButton
    private lateinit var loadingIndicator: View
    private lateinit var infoButton: ImageView
    private lateinit var scanResultsTitleTv: TextView
    private val viewModel: ScanViewModel by viewModels()
    private var malwareToBeDeleted: MalwareModel? = null

    @Inject
    lateinit var trackerAdapterFactory: TrackerAdapter.Factory

    @Inject
    lateinit var getApplicationIconUseCase: GetApplicationIconUseCase

    private lateinit var trackerAdapter: TrackerAdapter

    private val malwareAdapter by lazy {
        MalwareAdapter(malwareClickListener, malwareActionClickListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_scan_results)
        initViews()

        trackerAdapter = trackerAdapterFactory.create(trackerClickListener)

        setupObservers()

        val scanId = intent.extras?.getInt(SCAN_ID)

        scanId?.let {
            //If activity started with scanId, means need to load data from db
            viewModel.scanId = it
        }
            ?: null //TODO: Needs to be replaced with text that says it has an issue pulling results

        //Init rvs and adapters
        resultsRv.layoutManager = LinearLayoutManager(this)
        resultsRv.adapter = concatAdapter
        resultsRv.addItemDecoration(CellMarginDecoration(32, 0))
        resultsRv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        closeButton.setOnClickListener {
            finish()
        }

        infoButton.setOnClickListener { openInfoDialog() }
    }

    private fun initViews() {
        resultsRv = findViewById(R.id.scan_results_rv)
        closeButton = findViewById(R.id.close_button)
        loadingIndicator = findViewById(R.id.loading_indicator)
        infoButton = findViewById(R.id.info_button)
        scanResultsTitleTv = findViewById(R.id.scan_results_activity_title_tv)

        // SearchBar stuff
        val searchView = findViewById<SearchView>(R.id.search_view)
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.white_70))
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.white_70))
        searchEditText.setPadding(70, 0, 0, 0)
        searchEditText.textSize = 16f
        val closeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setColorFilter(ContextCompat.getColor(this, R.color.white_70), PorterDuff.Mode.SRC_IN)
        closeButton.setPadding(0, 0, 50, 0)
        val searchBar = searchView.findViewById<LinearLayout>(com.google.android.material.R.id.search_bar)
        searchBar.layoutTransition = LayoutTransition()

        searchView.clearFocus()

        // Listener to handle SearchView expand and collapse to show or hide the page title
        searchView.setOnSearchClickListener {
            // Hide the title and info button when SearchView is expanded
            scanResultsTitleTv.visibility = View.INVISIBLE
            infoButton.visibility = View.INVISIBLE
            val searchBarParams = searchView.layoutParams
            searchBarParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            searchBarParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            searchView.layoutParams = searchBarParams
        }

        searchView.setOnCloseListener {
            // Show the title and info button when SearchView is closed
            scanResultsTitleTv.visibility = View.VISIBLE
            infoButton.visibility = View.VISIBLE
            val searchBarParams = searchView.layoutParams
            searchBarParams.width = ViewGroup.LayoutParams.WRAP_CONTENT // Original width
            searchBarParams.height = ViewGroup.LayoutParams.WRAP_CONTENT // Original height
            searchView.layoutParams = searchBarParams
            false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(text: String?) {
        trackerAdapter.filter(text)
        malwareAdapter.filter(text)
    }

    private fun setupObservers() {
        viewModel.malwareData.observe(this) {
            concatAdapter.addAdapter(0, malwareAdapter)

            if (it.isEmpty()) {
                malwareAdapter.setMalwares(emptyList())
            } else {
                malwareAdapter.setMalwares(it)
            }
        }

        viewModel.trackerData.observe(this) {
            concatAdapter.addAdapter(trackerAdapter)
            trackerAdapter.setTrackers(it)
        }
    }

    private val malwareClickListener: (malware: MalwareModel) -> Unit = { malware ->
        showMessage(malware.name)
    }

    private val malwareActionClickListener: (malware: MalwareModel) -> Unit = { malware ->
        when (malware.status) {
            ThreatStatus.EXIST -> {
                //Start to remove
                removeThreat(malware)
            }

            ThreatStatus.PENDING -> {
                //In remove process
                showMessage(getString(R.string.up_av_already_removing))
            }

            ThreatStatus.REMOVED -> {
                //Already removed, the adapter will be updated
            }

            ThreatStatus.FAILED -> {
                showMessage(getString(R.string.up_av_failed))
            }
        }
    }

    private val trackerClickListener: (tracker: TrackerModel) -> Unit = { tracker ->
        showCustomTrackerDialog(tracker.appName, tracker.packageId, tracker.trackers)
    }

    private fun openInfoDialog() {
        val deviceModel = getDeviceModel()
        if (deviceModel == "UP01") {
            showDialog(
                getString(R.string.up_av_malware_trackers_info_title),
                getString(R.string.up_av_malware_trackers_info_message_up_phone)
            ) {}
        } else {
            showDialogWithIntent(
                getString(R.string.up_av_malware_trackers_info_title),
                getString(R.string.up_av_malware_trackers_info_message),
                getString(R.string.up_av_unplugged_website_clickable_text),
                getString(R.string.up_av_unplugged_website)
            ) {}
        }
    }

    private fun getDeviceModel(): String? {
        val deviceModel = Build.MODEL ?: return null
        return URLEncoder.encode(deviceModel, "UTF-8")
    }

    private val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _: ActivityResult ->
            malwareToBeDeleted?.let {
                if (!isPackageInstalled(it.name, packageManager)) {
                    val malware = it.copy(status = ThreatStatus.REMOVED)
                    viewModel.updateMalwareStatus(malware)
                    malwareAdapter.updateMalware(malware)
                }
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

    private fun removeThreat(malware: MalwareModel) {
        when (malware.filePath) {
            MalwareModel.BLACK_LIST_PACKAGE -> {
                val intent = Intent(Intent.ACTION_DELETE)
                intent.data = Uri.parse("package:${malware.name}")
                malwareToBeDeleted = malware
                registerForActivityResult.launch(intent)
            }

            else -> {
                val result = viewModel.removeThreat(malware)
                result.observe(this, object : Observer<MalwareModel> {
                    override fun onChanged(value: MalwareModel) {
                        malwareAdapter.updateMalware(value)
                        result.removeObserver(this)
                    }
                })
            }
        }
    }

    private fun showCustomTrackerDialog(appName: String, packageId: String, trackers: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.tracker_custom_dialog, null)

        val appIcon = dialogLayout.findViewById<ImageView>(R.id.dialog_app_icon)
        val appNameTitle = dialogLayout.findViewById<TextView>(R.id.dialog_app_name_title)
        val trackerList = dialogLayout.findViewById<TextView>(R.id.dialog_tracker_list)
        val appNotInstalledLayout =
            dialogLayout.findViewById<LinearLayout>(R.id.dialog_app_not_installed_layout)

        val appIconDrawable = getApplicationIconUseCase(packageId)
        appIcon.setImageDrawable(appIconDrawable)

        appNameTitle.text = appName

        // Join the list items into a single string with line breaks
        val trackersConvertedList = TrackerListConverter().toTrackerList(trackers)
        val trackerNames = trackersConvertedList.map { it.name }
        trackerList.text = trackerNames.joinToString(separator = "\n")

        // Check if the app is installed
        if (isPackageInstalled(packageId, packageManager)) {
            appNotInstalledLayout.visibility = View.GONE
        } else {
            appNotInstalledLayout.visibility = View.VISIBLE
        }

        builder.setView(dialogLayout)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.up_av_ok) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
        dialog.window?.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                this, R.drawable.background_dialog
            )
        )
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(this.getColor(R.color.main_text_color))
    }
}
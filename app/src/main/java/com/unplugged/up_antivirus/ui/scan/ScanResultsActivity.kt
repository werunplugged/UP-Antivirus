package com.unplugged.up_antivirus.ui.scan

import android.animation.LayoutTransition
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.trackerextension.TrackerModel
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.base.BaseActivity
import com.unplugged.up_antivirus.data.history.model.HistoryModel
import com.unplugged.up_antivirus.ui.CellMarginDecoration
import com.unplugged.up_antivirus.ui.status.StatusActivity
import com.unplugged.upantiviruscommon.malware.MalwareModel
import com.unplugged.upantiviruscommon.malware.ThreatStatus
import com.unplugged.upantiviruscommon.model.AppInfo
import com.unplugged.upantiviruscommon.utils.Constants.SCAN_ID
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder
import javax.inject.Inject

@AndroidEntryPoint
class ScanResultsActivity : BaseActivity() {
    private lateinit var malwareResultsRv: RecyclerView
    private lateinit var trackersResultsRv: RecyclerView
    private lateinit var shieldLogo: ImageView
    private lateinit var tvTitleFromScan: TextView
    private lateinit var tvSubtitleFromScan: TextView
    private lateinit var tvTitleFromHistory: TextView
    private lateinit var tvSubtitleFromHistory: TextView
    private lateinit var filesScanned: TextView
    private lateinit var malwareFound: TextView
    private lateinit var appsScanned: TextView
    private lateinit var trackersResultsTitle: TextView
    private lateinit var trackersIdentified: TextView
    private lateinit var closeButton: AppCompatButton
    private lateinit var learnMoreButton: AppCompatButton
    private lateinit var searchView: SearchView
    private lateinit var loadingIndicator: View
    private lateinit var infoButton: ImageView
    private lateinit var scanResultsTitleTv: TextView
    private lateinit var container: FragmentContainerView

    private var malwareToBeDeleted: MalwareModel? = null

    private var deviceModel: String? = getDeviceModel()

    private val viewModel: ScanViewModel by viewModels()

    @Inject
    lateinit var trackerAdapterFactory: TrackerAdapter.Factory
    private lateinit var trackerAdapter: TrackerAdapter

    private val malwareAdapter by lazy {
        val apps = this.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appInfoList : MutableList<AppInfo?> = mutableListOf()
        for (app in apps) {
            appInfoList.add(viewModel.getAppInfo(app.packageName))
        }

        MalwareAdapter(this, malwareClickListener, malwareActionClickListener, installedApplications = appInfoList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_scan_results)
        initViews()
        viewModel.loadTrackersDetails(this)
        trackerAdapter = trackerAdapterFactory.create(trackerClickListener)

        setupObservers()

        val scanId = intent.extras?.getInt(SCAN_ID)
        scanId?.let {
            //If activity started with scanId, means need to load data from db
            viewModel.scanId = it
        } ?: null //TODO: Needs to be replaced with text that says it has an issue pulling results

        //Init rvs and adapters
        trackersResultsRv.addItemDecoration(CellMarginDecoration(32, 0))
        trackersResultsRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        malwareResultsRv.addItemDecoration(CellMarginDecoration(32, 0))
        malwareResultsRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        closeButton.setOnClickListener {
            handleBackPressed()
        }

        learnMoreButton.setOnClickListener {
            infoButton.callOnClick()
        }

        onBackPressedDispatcher.addCallback(this) {
            if (!handleFragmentBackPressed()) {
                isEnabled = false
                handleBackPressed()
            }
        }

        malwareResultsRv.adapter = malwareAdapter
        trackersResultsRv.adapter = trackerAdapter
        infoButton.setOnClickListener {
            container.isVisible = true
           openInfoFragment()
        }
    }

    private fun setValues(historyModel: HistoryModel?) {
        filesScanned.text = historyModel?.filesScanned.toString()
        appsScanned.text = historyModel?.appsScanned.toString()
        trackersIdentified.text = historyModel?.trackersFound.toString()
    }

    private fun initViews() {
        trackersResultsRv = findViewById(R.id.trackers_results_rv)
        malwareResultsRv = findViewById(R.id.malware_recyclerview)

        shieldLogo = findViewById(R.id.shield_logo)
        tvTitleFromScan = findViewById(R.id.tv_title_from_scan)
        tvSubtitleFromScan = findViewById(R.id.tv_subtitle_from_scan)
        tvTitleFromHistory = findViewById(R.id.tv_title_from_history)
        tvSubtitleFromHistory = findViewById(R.id.tv_subtitle_from_history)
        closeButton = findViewById(R.id.close_button)
        learnMoreButton = findViewById(R.id.learn_more_button)
        loadingIndicator = findViewById(R.id.loading_indicator)
        infoButton = findViewById(R.id.info_button)
        scanResultsTitleTv = findViewById(R.id.scan_results_activity_title_tv)
        filesScanned = findViewById(R.id.files_scanned_number)
        malwareFound = findViewById(R.id.malware_found_number)
        appsScanned = findViewById(R.id.apps_scanned_number)
        trackersResultsTitle = findViewById(R.id.tv_trackers_scan_results)
        trackersIdentified = findViewById(R.id.trackers_identified_number)
        container = findViewById(R.id.container)

        // SearchBar stuff
        searchView = findViewById(R.id.search_view)
        val searchEditText =
            searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.white_70))
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.white_70))
        searchEditText.setPadding(70, 0, 0, 0)
        searchEditText.textSize = 16f
        val closeButton =
            searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setColorFilter(
            ContextCompat.getColor(this, R.color.white_70),
            PorterDuff.Mode.SRC_IN
        )
        closeButton.setPadding(0, 0, 50, 0)
        val searchBar =
            searchView.findViewById<LinearLayout>(com.google.android.material.R.id.search_bar)
        searchBar.layoutTransition = LayoutTransition()

        searchView.clearFocus()

        // Listener to handle SearchView expand and collapse to show or hide the page title
        searchView.setOnSearchClickListener {
            // Hide the title and info button when SearchView is expanded
            trackersResultsTitle.visibility = View.INVISIBLE
            val searchBarParams = searchView.layoutParams
            searchBarParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            searchBarParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            searchView.layoutParams = searchBarParams
        }

        searchView.setOnCloseListener {
            // Show the title and info button when SearchView is closed
            trackersResultsTitle.visibility = View.VISIBLE
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
            if (it.isEmpty()) {
                malwareAdapter.setMalwares(emptyList())
                val mainColor = ContextCompat.getColor(this, R.color.results_numbers_color)
                malwareFound.setTextColor(mainColor)
            } else {
                malwareAdapter.setMalwares(it)
                malwareFound.text = it.size.toString()
                malwareFound.setTextColor(Color.RED)
            }

            viewModel.setTitleFromScan(this, !viewModel.isActiveThreatsExist)
            malwareResultsRv.isEnabled = viewModel.isActiveThreatsExist
            viewModel.setShieldLogo(viewModel.isActiveThreatsExist)
        }

        viewModel.trackerData.observe(this) {
            trackerAdapter.setTrackers(it)
            if (it.isEmpty()) {
                searchView.isVisible = false
                trackersResultsTitle.isVisible = false
            }
        }

        viewModel.historyModel.observe(this) {
            setValues(it)

            if (intent.getBooleanExtra("fromHistory", false)) {
                tvTitleFromScan.isVisible = false
                tvSubtitleFromScan.isVisible = false
                tvTitleFromHistory.isVisible = true
                tvSubtitleFromHistory.isVisible = true
                tvTitleFromHistory.text = it?.name
                tvSubtitleFromHistory.text = it?.date
                scanResultsTitleTv.text = getString(R.string.history_scan_results)
            } else {
                tvTitleFromScan.isVisible = true
                tvSubtitleFromScan.isVisible = true
                tvTitleFromHistory.isVisible = false
                tvSubtitleFromHistory.isVisible = false

                tvSubtitleFromScan.text = getString(
                    R.string.scan_result_subtitle_format,
                    it?.name, it?.date
                )
                scanResultsTitleTv.text = getString(R.string.up_av_scan_results)
            }
        }

        viewModel.shieldLogoLiveData.observe(this) {
            shieldLogo.setImageResource(
                when (it) {
                    false -> {
                        R.drawable.ic_small_shield_logo_normal
                        //if(deviceModel == "UP01")R.drawable.ic_small_shield_logo_attention
                    }

                    true -> {
                        R.drawable.ic_small_shield_logo_broken
                    }

                    else -> {
                        R.drawable.ic_small_shield_logo_normal
                    }
                }
            )
        }

        viewModel.setTitle.observe(this) {
            tvTitleFromScan.text = it
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
                viewModel.setShieldLogo(viewModel.isActiveThreatsExist)
                malwareResultsRv.isEnabled = viewModel.isActiveThreatsExist
                viewModel.setTitleFromScan(this, viewModel.isActiveThreatsExist)
            }

            ThreatStatus.FAILED -> {
                showMessage(getString(R.string.up_av_failed))
            }
        }
    }

    private val trackerClickListener: (tracker: TrackerModel) -> Unit = { tracker ->
        //showCustomTrackerDialog(tracker.appName, tracker.packageId, tracker.trackers)
        container.isVisible = true
        val perAppFragment = PerAppFragment.newInstance(tracker)
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        setTransitionAnimations(transaction)
        transaction.add(container.id, perAppFragment, "PerAppFragment")
        transaction.addToBackStack(null)
        transaction.commit()
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
                    viewModel.setShieldLogo(viewModel.isActiveThreatsExist)
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


    private fun setTransitionAnimations(transaction: FragmentTransaction) {
        transaction.setCustomAnimations(
            R.anim.fragment_in_right,
            R.anim.fragment_out_left,
            R.anim.fragment_in_left,
            R.anim.fragment_out_right
        )
    }

    private fun handleFragmentBackPressed(): Boolean {
        return if (supportFragmentManager.fragments.isNotEmpty()) {
            supportFragmentManager.popBackStack()
            if(supportFragmentManager.fragments.isEmpty()){
                container.isVisible = false
            }
            true
        } else {
            false
        }
    }

    fun openInfoFragment(){
        val infoFragment = InfoFragment()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        setTransitionAnimations(transaction)
        transaction.add(container.id, infoFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun returnToStatusActivity(){
        val intent = Intent(this, StatusActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun handleBackPressed(){
        if(intent.getBooleanExtra("fromHistory", false)){
            onBackPressedDispatcher.onBackPressed()
        } else {
            returnToStatusActivity()
        }
    }
}
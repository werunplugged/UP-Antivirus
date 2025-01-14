package com.unplugged.up_antivirus.ui.scan

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.trackerextension.TrackerModel
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.base.BaseActivity
import com.unplugged.up_antivirus.base.Utils
import com.unplugged.up_antivirus.data.tracker.model.TrackerListConverter
import com.unplugged.up_antivirus.domain.use_case.CancelScanningUseCase
import com.unplugged.up_antivirus.domain.use_case.GetApplicationIconUseCase
import com.unplugged.up_antivirus.ui.CellMarginDecoration
import com.unplugged.up_antivirus.ui.splash.SplashActivity
import com.unplugged.up_antivirus.ui.status.StatusActivity
import com.unplugged.upantiviruscommon.malware.MalwareModel
import com.unplugged.upantiviruscommon.malware.ThreatStatus
import com.unplugged.upantiviruscommon.model.ScannerType
import com.unplugged.upantiviruscommon.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScanActivity : BaseActivity() {
    private lateinit var titleTv: TextView
    private lateinit var scanningHypatiaTitleTv: TextView
    private lateinit var scanningBlacklistTitleTv: TextView
    private lateinit var scanningTrackersTitleTv: TextView
    private lateinit var scanningHypatiaProgressTv: TextView
    private lateinit var scanningBlacklistProgressTv: TextView
    private lateinit var scanningTrackersProgressTv: TextView
    private lateinit var scanningHypatiaProgressBar: ProgressBar
    private lateinit var scanningBlacklistProgressBar: ProgressBar
    private lateinit var scanningTrackersProgressBar: ProgressBar
    private lateinit var removeAllTv: TextView
    private lateinit var resultsRv: RecyclerView
    private lateinit var scanningHypatiaProgressWrapper: View
    private lateinit var scanningBlacklistProgressWrapper: View
    private lateinit var scanningTrackersProgressWrapper: View
    private lateinit var cancelButton: AppCompatButton
    private lateinit var goToFullResultsButton: AppCompatButton
    private lateinit var loadingIndicator: View
    private lateinit var scanningHypatiaMessageTv: TextView
    private lateinit var scanningBlacklistMessageTv: TextView
    private lateinit var scanningTrackersMessageTv: TextView
    private lateinit var scanResultsTitleTv: TextView

    private var malwareToBeDeleted: MalwareModel? = null

    private val viewModel: ScanViewModel by viewModels()

    private val concatAdapter = ConcatAdapter()
    private val malwareAdapter by lazy {
        MalwareAdapter(malwareClickListener, malwareActionClickListener)
    }

    @Inject
    lateinit var trackerAdapterFactory: TrackerAdapter.Factory

    @Inject
    lateinit var getApplicationIconUseCase: GetApplicationIconUseCase

    private lateinit var trackerAdapter: TrackerAdapter

    private val malwareClickListener: (malware: MalwareModel) -> Unit = { malware ->
        showMessage(malware.name)
    }

    private val trackerClickListener: (tracker: TrackerModel) -> Unit = { tracker ->
        showCustomTrackerDialog(tracker.appName, tracker.packageId, tracker.trackers)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        initViews()

        trackerAdapter = trackerAdapterFactory.create(trackerClickListener)

        setupObservers()

        if (viewModel.isScanning()) {
            Log.d("isScanning", "scan activity onCreate: here")
            onReturnToScan()
        } else {
            startScan()
        }

        cancelButton.setOnClickListener {
            if (viewModel.isScanning()) {
                showDialog(
                    getString(R.string.up_av_cancel_scan),
                    getString(R.string.up_av_cancel_scan_message),
                    { viewModel.cancelScan() },
                    {})
            } else {
                returnToStatusActivity()
            }
        }
        goToFullResultsButton.setOnClickListener {
            if (!viewModel.isScanning()) {
                val scanId = viewModel.scanId
                Intent(this, ScanResultsActivity::class.java).apply {
                    putExtra(Constants.SCAN_ID, scanId)
                    startActivity(this)
                }
            }
        }

        removeAllTv.setOnClickListener {
            if (viewModel.isMalwareScanDone()) {
                if (viewModel.isActiveThreatsExist) {
                    removeAllThreats()
                } else {
                    showMessage(getString(R.string.up_av_you_are_already_protected))
                }
            } else {
                showMessage(getString(R.string.up_av_wait_for_the_previous_scan_to_finish))
            }
        }

        // Add adapters to ConcatAdapter
        concatAdapter.addAdapter(trackerAdapter)
        //Init rvs and adapters
        resultsRv.adapter = concatAdapter
        resultsRv.addItemDecoration(CellMarginDecoration(32, 0))
        resultsRv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.isScanning()) {
                    moveTaskToBack(true)
                } else if (viewModel.isMalwareScanDone() && viewModel.isScannerDone(ScannerType.TRACKERS)) {
                    returnToStatusActivity()
                } else {
                    moveTaskToBack(true)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.scanId != null) {
            onReturnToScan()
        }
    }

    private fun initViews() {
        titleTv = findViewById(R.id.title_tv)
        scanningHypatiaTitleTv = findViewById(R.id.scanning_hypatia_title_tv)
        scanningBlacklistTitleTv = findViewById(R.id.scanning_blacklist_title_tv)
        scanningTrackersTitleTv = findViewById(R.id.scanning_trackers_title_tv)
        scanningHypatiaProgressTv = findViewById(R.id.scanning_hypatia_progress_tv)
        scanningBlacklistProgressTv = findViewById(R.id.scanning_blacklist_progress_tv)
        scanningTrackersProgressTv = findViewById(R.id.scanning_trackers_progress_tv)
        scanningHypatiaProgressBar = findViewById(R.id.scanning_hypatia_progress_bar)
        scanningBlacklistProgressBar = findViewById(R.id.scanning_blacklist_progress_bar)
        scanningTrackersProgressBar = findViewById(R.id.scanning_trackers_progress_bar)
        removeAllTv = findViewById(R.id.remove_all_threats_tv)
        resultsRv = findViewById(R.id.scan_results_rv)
        scanningHypatiaProgressWrapper = findViewById(R.id.scanning_hypatia_progress_wrapper)
        scanningBlacklistProgressWrapper = findViewById(R.id.scanning_blacklist_progress_wrapper)
        scanningTrackersProgressWrapper = findViewById(R.id.scanning_trackers_progress_wrapper)
        cancelButton = findViewById(R.id.cancel_button)
        goToFullResultsButton = findViewById(R.id.go_to_full_results_button)
        loadingIndicator = findViewById(R.id.loading_indicator)
        scanningHypatiaMessageTv = findViewById(R.id.scan_hypatia_message_tv)
        scanningBlacklistMessageTv = findViewById(R.id.scan_blacklist_message_tv)
        scanningTrackersMessageTv = findViewById(R.id.scan_trackers_message_tv)
        scanResultsTitleTv = findViewById(R.id.scan_results_title_tv)
    }

    private fun startScan() {
        //If there is no scan active, start new scan
        if (!viewModel.isScanning()) {
            loadingIndicator.visibility = View.VISIBLE
            viewModel.createScanId()
            viewModel.startScan()
            viewModel.subscribeToScanStats()
        }
    }

    private fun setupObservers() {
        viewModel.scanIdLiveData.observe(this) {
            viewModel.subscribeTrackers(it)
            viewModel.trackersLiveData.observe(this) { model ->
                trackerAdapter.setTrackers(model)
                scanResultsTitleTv.visibility = View.VISIBLE
            }
        }

        viewModel.navigateBack.observe(this){
            if(it){
                val dialogTitle = getString(R.string.up_av_scan_fail_db_missing_title)
                val dialogMessage = getString(R.string.up_av_database_unavailable_an_error_occurred_while_downloading_the_required_files)
                val action = { returnToStatusActivity() }
                showDialog(dialogTitle, dialogMessage, action)
            }
            viewModel.navigateBackToPreviousActivity(false)
        }

        viewModel.scanningState.observe(this) { scanningState ->
            if (scanningState?.error != null) {
                var dialogTitle: String
                var dialogMessage: String
                var action = { finish() }
                when (scanningState.error) {
                    "NO_SUBSCRIPTION" -> {
                        dialogTitle = getString(R.string.up_av_subscription_expired)
                        dialogMessage = getString(R.string.up_av_subscription_expired_message)
                    }

                    "SCAN_LIMIT_REACHED" -> {
                        dialogTitle = getString(R.string.up_av_scan_limit_reached)
                        dialogMessage = getString(R.string.up_av_scan_limit_reached_message)
                    }

                    "NO_DATABASE_AVAILABLE" -> {
                        dialogTitle = getString(R.string.up_av_scan_fail_db_missing_title)
                        dialogMessage = getString(R.string.up_av_scan_fail_db_missing_message)
                        action = {
                            returnToStatusActivity()
                        }
                    }

                    CancelScanningUseCase.CancelReason.USER.name -> {
                        dialogTitle = getString(R.string.up_av_scan_canceled_title)
                        dialogMessage = getString(R.string.up_av_scan_canceled_message)
                        action = {
                            returnToStatusActivity()
                        }
                    }

                    CancelScanningUseCase.CancelReason.SESSION_REVOKE.name -> {
                        dialogTitle = getString(R.string.up_av_session_revoke_title)
                        dialogMessage = getString(R.string.up_av_session_revoke_message)
                        action = {
                            val intent = Intent(this, SplashActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }

                    else -> {
                        dialogTitle = getString(R.string.up_av_unknown_error)
                        dialogMessage = getString(R.string.up_av_cancel_unknown_reason)
                    }
                }

                showDialog(dialogTitle, dialogMessage, action)
                scanningHypatiaMessageTv.text = ""
                scanningBlacklistMessageTv.text = ""
                scanningTrackersMessageTv.text = ""
                viewModel.stopScanService()
            }

            if (scanningState?.malware != null) {
                malwareAdapter.addMalware(scanningState.malware)
                scanResultsTitleTv.visibility = View.VISIBLE
            }

            if (scanningState?.scanStats != null) {
                //Scan finished
                if (!viewModel.isScanning()) {
                    cancelButton.text = getString(R.string.up_av_close)
                    goToFullResultsButton.visibility = View.VISIBLE
                    scanResultsTitleTv.visibility = View.VISIBLE
                    viewModel.stopScanService()
                }

                when (scanningState.scanStats.type) {
                    ScannerType.HYPATIA -> {
                        hypatiaDone()
                    }

                    ScannerType.BLACKLIST -> {
                        blacklistDone()
                    }

                    ScannerType.TRACKERS -> {
                        trackersDone()
                    }

                    else -> {}
                }
            }

            if (scanningState?.message?.message != null) {
                when (scanningState.message.type) {
                    ScannerType.HYPATIA -> {
                        scanningHypatiaMessageTv.text = scanningState.message.message
                    }

                    ScannerType.BLACKLIST -> {
                        scanningBlacklistMessageTv.text = scanningState.message.message
                    }

                    ScannerType.TRACKERS -> {
                        scanningTrackersMessageTv.text = scanningState.message.message
                    }

                    else -> {}
                }
            }
        }

        viewModel.scanningProgress.observe(this) { scanProgress ->
            when (scanProgress.type) {
                ScannerType.HYPATIA -> {
                    if (scanProgress.progress < 100) {
                        scanningHypatiaTitleTv.text = getString(R.string.up_av_scanning_title)
                    } else {
                        scanningHypatiaTitleTv.text = getString(R.string.up_av_finished_title)
                        scanningHypatiaMessageTv.text = ""
                    }
                }

                ScannerType.BLACKLIST -> {
                    if (scanProgress.progress < 100) {
                        scanningBlacklistTitleTv.text = getString(R.string.up_av_scanning_title)
                    } else {
                        scanningBlacklistTitleTv.text = getString(R.string.up_av_finished_title)
                        scanningBlacklistMessageTv.text = ""
                    }
                }

                ScannerType.TRACKERS -> {
                    if (scanProgress.progress < 100) {
                        scanningTrackersTitleTv.text = getString(R.string.up_av_scanning_title)
                    } else {
                        scanningTrackersTitleTv.text = getString(R.string.up_av_finished_title)
                        scanningTrackersMessageTv.text = ""
                    }
                }

                else -> {}
            }
            updateProgress(scanProgress.progress, scanProgress.type)
        }

        viewModel.malwareData.observe(this) {
            if (viewModel.isScannerDone(ScannerType.HYPATIA) && viewModel.isScannerDone(ScannerType.BLACKLIST) && it.isEmpty()) {
                concatAdapter.addAdapter(0, malwareAdapter)
                resultsRv.scrollToPosition(0)
            }

            malwareAdapter.setMalwares(it)
            if (it.isNotEmpty()) {
                removeAllTv.visibility =
                    if ((it.any { malwareModel -> malwareModel.filePath != MalwareModel.BLACK_LIST_PACKAGE }) && !areAllThreatsRemoved()) {
                        View.VISIBLE
                    } else View.GONE
                if (concatAdapter.adapters[0] !is MalwareAdapter)
                    concatAdapter.addAdapter(0, malwareAdapter)
                resultsRv.scrollToPosition(0)
            }
        }

        viewModel.trackerData.observe(this) {
            trackerAdapter.setTrackers(it)
        }
    }

    private fun updateProgress(progress: Double, scannerType: ScannerType) {
        loadingIndicator.visibility = View.GONE
        Utils.printLog(ScanActivity::class.java, "progress: $progress")
        when (scannerType) {
            ScannerType.HYPATIA -> {
                scanningHypatiaProgressBar.progress = progress.toInt()
                scanningHypatiaProgressTv.text = "${progress.toInt()}%"
            }

            ScannerType.BLACKLIST -> {
                scanningBlacklistProgressBar.progress = progress.toInt()
                scanningBlacklistProgressTv.text = "${progress.toInt()}%"
            }

            ScannerType.TRACKERS -> {
                scanningTrackersProgressBar.progress = progress.toInt()
                scanningTrackersProgressTv.text = "${progress.toInt()}%"
            }

            else -> {}
        }
    }

    private val malwareActionClickListener: (malware: MalwareModel) -> Unit = { malware ->
        if (viewModel.isMalwareScanDone()) {
            when (malware.status) {
                ThreatStatus.EXIST -> {
                    // Start to remove
                    removeThreat(malware)
                }

                ThreatStatus.PENDING -> {
                    // In remove process
                    showMessage(getString(R.string.up_av_already_removing))
                }

                ThreatStatus.REMOVED -> {
                    // Already removed
                }

                ThreatStatus.FAILED -> {
                    showMessage(getString(R.string.up_av_failed))
                }
            }
        } else {
            showMessage(getString(R.string.up_av_wait_for_scan_to_finish))
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
        builder.setPositiveButton(R.string.up_av_close) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
        dialog.window?.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                this,
                R.drawable.background_dialog
            )
        )
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(this.getColor(R.color.main_text_color))
    }

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun removeAllThreats() {
        val malware = viewModel.malwareData.value ?: listOf()
        for (threat in malware.reversed()) {
            if (threat.status != ThreatStatus.REMOVED) {
                removeThreat(threat)
            }
        }
    }

    private fun areAllThreatsRemoved(): Boolean {
        val malware = viewModel.malwareData.value ?: listOf()
        return if (malware.isNotEmpty()) {
            malware.all { it.status == ThreatStatus.REMOVED }
        } else true
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

        // Check if all threats are removed
        if (areAllThreatsRemoved()) {
            // Hide the "Remove All" button
            removeAllTv.visibility = View.GONE
        }
    }

    private fun returnToStatusActivity() {
        viewModel.stopScanService()
        val intent = Intent(this@ScanActivity, StatusActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun hypatiaDone() {
        updateProgress(100.0, ScannerType.HYPATIA)
        scanningHypatiaMessageTv.text = ""
        scanningHypatiaTitleTv.text = getString(R.string.up_av_finished_title)
    }

    private fun blacklistDone() {
        updateProgress(100.0, ScannerType.BLACKLIST)
        scanningBlacklistMessageTv.text = ""
        scanningBlacklistTitleTv.text = getString(R.string.up_av_finished_title)
    }

    private fun trackersDone() {
        updateProgress(100.0, ScannerType.TRACKERS)
        scanningTrackersMessageTv.text = ""
        scanningTrackersTitleTv.text = getString(R.string.up_av_finished_title)
    }

    private fun onReturnToScan() {
        viewModel.subscribeToScanStats()
        cancelButton.text = getString(R.string.up_av_cancel)

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.updateUiWhenResuming()
        }

        scanningHypatiaProgressBar.progress = viewModel.getScanProgress(ScannerType.HYPATIA).toInt()
        scanningHypatiaProgressTv.text = viewModel.getScanProgress(ScannerType.HYPATIA).toInt().toString() + "%"
        scanningBlacklistProgressBar.progress = viewModel.getScanProgress(ScannerType.BLACKLIST).toInt()
        scanningBlacklistProgressTv.text = viewModel.getScanProgress(ScannerType.BLACKLIST).toInt().toString() + "%"
        scanningTrackersProgressBar.progress = viewModel.getScanProgress(ScannerType.TRACKERS).toInt()
        scanningTrackersProgressTv.text = viewModel.getScanProgress(ScannerType.TRACKERS).toInt().toString() + "%"


        if (viewModel.isScannerDone(ScannerType.HYPATIA)) {
            hypatiaDone()
        } else {
            scanningHypatiaTitleTv.text = getString(R.string.up_av_scanning_title)
        }

        if (viewModel.isScannerDone(ScannerType.BLACKLIST)) {
            blacklistDone()
        } else {
            scanningBlacklistTitleTv.text = getString(R.string.up_av_scanning_title)
        }

        if (viewModel.isScannerDone(ScannerType.TRACKERS)) {
            trackersDone()
        } else {
            scanningTrackersTitleTv.text = getString(R.string.up_av_scanning_title)
        }

        if (viewModel.isScanning().not()) {
            cancelButton.text = getString(R.string.up_av_close)
            goToFullResultsButton.visibility = View.VISIBLE
        }
    }
}
package com.unplugged.up_antivirus.ui.scan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.base.BaseActivity
import com.unplugged.up_antivirus.base.Utils
import com.unplugged.up_antivirus.domain.use_case.CancelScanningUseCase
import com.unplugged.up_antivirus.ui.splash.SplashActivity
import com.unplugged.up_antivirus.ui.status.StatusActivity
import com.unplugged.upantiviruscommon.model.ScannerType
import com.unplugged.upantiviruscommon.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var circularProgressBarPercentage: TextView
    private lateinit var scanType: TextView
    private lateinit var cancelButton: AppCompatButton
    private lateinit var closeButton: AppCompatButton
    private lateinit var goToFullResultsButton: AppCompatButton
    private lateinit var loadingIndicator: View
    private lateinit var scanningHypatiaMessageTv: TextView
    private lateinit var scanningBlacklistMessageTv: TextView
    private lateinit var scanningTrackersMessageTv: TextView

    private val viewModel: ScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        initViews()
        scanType.text = viewModel.getScanType(this, intent.getBooleanExtra("scanType", true))

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
            }
        }
        goToFullResultsButton.isActivated = true
        goToFullResultsButton.setOnClickListener {
            if (!viewModel.isScanning()) {
                val scanId = viewModel.scanId
                Intent(this, ScanResultsActivity::class.java).apply {
                    putExtra(Constants.SCAN_ID, scanId)
                    putExtra("fromHistory", false)
                    startActivity(this)
                }
                finish()
            }
        }

        closeButton.setOnClickListener {
            returnToStatusActivity()
        }

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
        circularProgressBar = findViewById(R.id.circular_progress_bar)
        circularProgressBarPercentage = findViewById(R.id.tv_progress_percentage)
        scanType = findViewById(R.id.tv_scan_type)
        cancelButton = findViewById(R.id.cancel_button)
        closeButton = findViewById(R.id.close_button)
        goToFullResultsButton = findViewById(R.id.go_to_full_results_button)
        loadingIndicator = findViewById(R.id.loading_indicator)
        scanningHypatiaMessageTv = findViewById(R.id.scan_hypatia_message_tv)
        scanningBlacklistMessageTv = findViewById(R.id.scan_blacklist_message_tv)
        scanningTrackersMessageTv = findViewById(R.id.scan_trackers_message_tv)
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
        }

        viewModel.navigateBack.observe(this) {
            if (it) {
                val dialogTitle = getString(R.string.up_av_scan_fail_db_missing_title)
                val dialogMessage =
                    getString(R.string.up_av_database_unavailable_an_error_occurred_while_downloading_the_required_files)
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
                scanningHypatiaMessageTv.text = " "
                scanningBlacklistMessageTv.text = " "
                scanningTrackersMessageTv.text = " "
                viewModel.stopScanService()
            }

            if (scanningState?.scanStats != null) {
                //Scan finished
                if (!viewModel.isScanning()) {
                    cancelButton.isInvisible = true
                    closeButton.isVisible = true
                    goToFullResultsButton.visibility = View.VISIBLE
                    titleTv.text = getString(R.string.scan_completed)
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
                        scanningHypatiaMessageTv.text = " "
                    }
                }

                ScannerType.BLACKLIST -> {
                    if (scanProgress.progress < 100) {
                        scanningBlacklistTitleTv.text = getString(R.string.up_av_scanning_title)
                    } else {
                        scanningBlacklistTitleTv.text = getString(R.string.up_av_finished_title)
                        scanningBlacklistMessageTv.text = " "
                    }
                }

                ScannerType.TRACKERS -> {
                    if (scanProgress.progress < 100) {
                        scanningTrackersTitleTv.text = getString(R.string.up_av_scanning_title)
                    } else {
                        scanningTrackersTitleTv.text = getString(R.string.up_av_finished_title)
                        scanningTrackersMessageTv.text = " "
                    }
                }

                else -> {}
            }
            updateProgress(scanProgress.progress, scanProgress.type)
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
        val totalPercentage = ((scanningHypatiaProgressBar.progress + scanningBlacklistProgressBar.progress + scanningTrackersProgressBar.progress)/3).toFloat()
        circularProgressBar.progress = totalPercentage
        circularProgressBarPercentage.text = "$totalPercentage"
    }

    private fun returnToStatusActivity() {
        viewModel.stopScanService()
        val intent = Intent(this@ScanActivity, StatusActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun hypatiaDone() {
        updateProgress(100.0, ScannerType.HYPATIA)
        scanningHypatiaMessageTv.text = " "
        scanningHypatiaTitleTv.text = getString(R.string.up_av_finished_title)
    }

    private fun blacklistDone() {
        updateProgress(100.0, ScannerType.BLACKLIST)
        scanningBlacklistMessageTv.text = " "
        scanningBlacklistTitleTv.text = getString(R.string.up_av_finished_title)
    }

    private fun trackersDone() {
        updateProgress(100.0, ScannerType.TRACKERS)
        scanningTrackersMessageTv.text = " "
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
            cancelButton.isInvisible = true
            closeButton.isVisible = true
            goToFullResultsButton.visibility = View.VISIBLE
        }
    }
}
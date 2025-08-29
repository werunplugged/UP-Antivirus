package com.unplugged.up_antivirus.ui.status

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import com.google.android.material.tabs.TabLayout
import com.tapadoo.alerter.Alerter
import com.unplugged.account.UpAccount
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.base.BaseActivity
import com.unplugged.up_antivirus.base.Utils
import com.unplugged.up_antivirus.data.history.model.HistoryModel
import com.unplugged.up_antivirus.data.receiver.PackageMonitorService
import com.unplugged.up_antivirus.ui.history.ScanHistoryActivity
import com.unplugged.up_antivirus.ui.scan.ScanActivity
import com.unplugged.up_antivirus.ui.scan.ScanViewModel
import com.unplugged.up_antivirus.ui.settings.main.SettingsFragment
import com.unplugged.up_antivirus.ui.splash.SplashActivity
import com.unplugged.upantiviruscommon.model.Connectivity
import com.unplugged.upantiviruscommon.model.ScanParams
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatusActivity : BaseActivity() {

    private lateinit var startScanBtn: Button
    private lateinit var lastScanTime: TextView
    private lateinit var scanPreferencesGroup: RadioGroup
    private lateinit var loadingIndicator: View
    private lateinit var settingsButton: Button
    private lateinit var container: FragmentContainerView
    private lateinit var scanInfoButton: ImageButton
    private lateinit var tabLayout: TabLayout
    private lateinit var shieldAnimation: ImageView

    val viewModel: StatusViewModel by viewModels()
    private val scanViewModel: ScanViewModel by viewModels()

    override fun onStart() {
        viewModel.fetchAccountSubscription()
        viewModel.getSession()
        viewModel.loadBlacklistApps()
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status_new)
        initViews()

        startScanBtn.isActivated = true
        startScanBtn.setOnClickListener {
            MainScope().launch {
                startScan()
            }
        }

        scanInfoButton.setOnClickListener {
            showDialog(R.string.up_av_scan_info_title, R.string.up_av_scan_tooltip_info) {}
        }

        settingsButton.text = UpAccount.getSession()?.username?.first()?.uppercase().toString()
        settingsButton.setOnClickListener {
            container.isVisible = true
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.fragment_slide_in_from_top,
                R.anim.fragment_slide_out_to_bottom,
                R.anim.fragment_slide_in_from_bottom,
                R.anim.fragment_slide_out_to_top
            )
            transaction.add(container.id, SettingsFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        onBackPressedDispatcher.addCallback(this) {
            if (!container.isVisible) {
                // If container is not visible, directly call onBackPressedDispatcher
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                return@addCallback
            }

            // Proceed with the existing logic if the container is visible
            val fragment = supportFragmentManager.findFragmentById(R.id.container)
            if (fragment is SettingsFragment && !fragment.handleBackPressed()) {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                container.isVisible = false
            }
        }

        scanPreferencesGroup.setOnCheckedChangeListener { group, checkedId ->
            if (group.findViewById<RadioButton>(checkedId).isPressed) {
                val scanParams = when (checkedId) {
                    R.id.quick_scan_rb -> ScanParams(true)
                    else -> ScanParams(false)
                }
                viewModel.updateScanPreferences(scanParams)
            }
        }

        val scanParams = viewModel.getScanPreferences()
        val checkId = when {
            scanParams.isQuickScan -> R.id.quick_scan_rb
            else -> R.id.scan_all_rb
        }

        scanPreferencesGroup.check(checkId)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                onTabSelected(tab?.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                onTabSelected(tab?.position)
            }
        })

        shieldAnimation.setBackgroundResource(R.drawable.shield_animation)
        val animationDrawable = shieldAnimation.background as AnimationDrawable
        shieldAnimation.scaleType = ImageView.ScaleType.CENTER_CROP
        animationDrawable.start()

        setupObservers()
    }

    private fun initViews() {
        startScanBtn = findViewById(R.id.start_scan_btn)
        lastScanTime = findViewById(R.id.subtitle)
        loadingIndicator = findViewById(R.id.loading_indicator)
        scanPreferencesGroup = findViewById(R.id.scan_preferences_group)
        settingsButton = findViewById(R.id.settings_avatar)
        container = findViewById(R.id.container)
        scanInfoButton = findViewById(R.id.info_button)
        tabLayout = findViewById(R.id.tab_layout)
        shieldAnimation = findViewById(R.id.shield_logo)
    }

    private fun setupObservers() {
        viewModel.blackListedAppLiveData.observe(this) { isLoading ->
            isLoading?.let {
                if (isLoading) {
                    loadingIndicator.visibility = View.VISIBLE
                } else {
                    loadingIndicator.visibility = View.GONE
                    checkFileManagerPermission()
                    if (!PackageMonitorService.isRunning) {
                        monitorInstallations()
                    }
                }
            }
        }

        viewModel.latestScan.observe(this) {
            setupLatestScan(it)
        }

        viewModel.sessionLiveData.observe(this) {
            if (it == null) {
                onSessionNotFound()
            }
        }

        viewModel.subscriptionStateLiveData.observe(this) { subscriptionState ->
            if (subscriptionState.error != null) {
                if (subscriptionState.error == "NO_DATA_FOUND") {
                    //onSubscriptionNotFound() TODO
                }
                return@observe
            }

            subscriptionState.accountSubscription?.let {
                if (it.expirationDays in 0..2) {
                    //less than 3 days left
                    val message = if (it.expirationDays == 0) {
                        //expires today
                        getString(R.string.up_av_premium_subscription_expires_today_warning)
                    } else {
                        //1-3 days left
                        getString(
                            R.string.up_av_premium_subscription_expiration_warning,
                            it.expirationDays.toString()
                        )
                    }

                    if (viewModel.shouldShowExpirationMessage) {
                        viewModel.shouldShowExpirationMessage = false
                        Alerter.create(this@StatusActivity)
                            .setTitle("")
                            .setText(message)
                            .setIcon(R.drawable.ic_info)
                            .setBackgroundColorRes(R.color.warning_yellow)
                            .enableInfiniteDuration(true)
                            .setDismissable(true)
                            .addButton(
                                getString(R.string.dismiss),
                                R.style.Widget_Vector_Button_Text_Alerter
                            ) {
                                Alerter.hide()
                            }
                            .show()
                    }
                }
            } ?: run {
                //onSubscriptionNotFound()
            }
        }
    }

    private fun monitorInstallations() {
        if (viewModel.startRealTimeProtection()) {
            val intent = Intent(this, PackageMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(intent)
            } else {
                this.startService(intent)
            }
        }
    }

    private fun onSessionNotFound() {
        showDialog(R.string.up_av_session_not_found, R.string.up_av_session_not_found_message) {
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }
    }

    private fun setupLatestScan(historyItem: HistoryModel?) {
        historyItem?.let {
            lastScanTime.text = getString(R.string.up_av_last_scan_was_at, it.date)
        }
    }

    private fun checkFileManagerPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val isStorageManager = Environment.isExternalStorageManager()
            if (!isStorageManager) {
                showDialog(
                    getString(R.string.up_av_file_manager_permission_title),
                    getString(R.string.up_av_file_manager_permission_body)
                ) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.fromParts("package", packageName, null)
                    startActivity(intent)
                }
            } else {
                checkReadPermission()
            }
        } else {
            checkReadPermission()
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionMap ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val readAudio = permissionMap[Manifest.permission.READ_MEDIA_AUDIO] ?: false
                val readImages = permissionMap[Manifest.permission.READ_MEDIA_IMAGES] ?: false
                val readVideo = permissionMap[Manifest.permission.READ_MEDIA_VIDEO] ?: false
                if (readAudio && readImages && readVideo) {
                    getNotificationPermission()
                }
            } else {
                val read = permissionMap[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
                val write = permissionMap[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
                if (read && write) {
                    //updateDatabase()
                }
            }

        }
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionMap ->
            Utils.printLog(StatusActivity::class.java, permissionMap.toString())
        }

    private fun isPermissionGranted(permission: String): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            permission
        ) != PackageManager.PERMISSION_DENIED)
    }

    private fun checkReadPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val readGranted = (isPermissionGranted(Manifest.permission.READ_MEDIA_AUDIO) &&
                    isPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES) &&
                    isPermissionGranted(Manifest.permission.READ_MEDIA_VIDEO))
            if (!readGranted) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                    )
                )
            } else {
                getNotificationPermission()
            }
        } else {
            val readGranted =
                (isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                        isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            if (!readGranted) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Utils.printLog(StatusActivity::class.java, "getNotificationPermission: ")
            val shouldShowRequestPermissionRationale =
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
            Utils.printLog(
                StatusActivity::class.java,
                "getNotificationPermission: shouldShowRequestPermissionRationale:$shouldShowRequestPermissionRationale",
            )
            notificationPermissionLauncher.launch(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            )
        }
    }

    private fun showScanActivity() {
        if (scanViewModel.isScanning()) {
            Toast.makeText(
                this,
                getString(R.string.up_av_please_wait_for_previous_scan_to_be_done),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val intent = Intent(this, ScanActivity::class.java)
            intent.putExtra("scanType", viewModel.getScanPreferences().isQuickScan)
            startActivity(intent)
            finish()
        }
    }

    fun onTabSelected(position: Int?) {
        when (position) {
            0 -> {
                Intent(this@StatusActivity, ScanHistoryActivity::class.java).apply {
                    startActivity(this)
                }
            }

            1 -> {
                showDialog(R.string.up_av_what_is_this_title, R.string.up_av_app_description) {}
            }
        }
    }

    private fun startScan() {
        when (viewModel.internetConnectivity()) {
            Connectivity.WIFI -> {
                showScanActivity()
            }

            Connectivity.CELLULAR -> {
                MainScope().launch {
                    if (viewModel.updateRequired()) {
                        cellularDialog()
                    } else if (!viewModel.newAppDatabase()) {
                        cellularDialog()
                    } else {
                        showScanActivity()
                    }
                }
            }

            Connectivity.NONE -> {
                MainScope().launch {
                    if (!viewModel.newAppDatabase()) {
                        noInternetDialog()
                    } else {
                        showScanActivity()
                    }
                }
            }
        }
    }

    private fun cellularDialog() {
        val title = "Update Required"
        val subtitle = getString(R.string.up_av_cellular_network_message)
        showDialog(title, subtitle, { allowDownloadOverCellular(); showScanActivity() }, {})
    }

    private fun noInternetDialog() {
        val title = "No Internet Connection"
        val subtitle =
            getString(R.string.up_av_no_network_for_database_message)
        showTryAgainDialog(title, subtitle, { startScan() }, {})
    }

    private fun allowDownloadOverCellular() {
        viewModel.setAllowDownloadOverCellular()
    }

    fun Activity.isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = activityManager.getRunningServices(Int.MAX_VALUE)

        for (service in runningServices) {
            if (service.service.className == serviceClass.name) {
                return true
            }
        }
        return false
    }
}
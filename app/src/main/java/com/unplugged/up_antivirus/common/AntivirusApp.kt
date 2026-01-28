package com.unplugged.up_antivirus.common

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.unplugged.accounthelper.AccountHelper
import com.unplugged.accounthelper.AccountListener

import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.base.Utils
import com.unplugged.up_antivirus.data.AntivirusRoomDatabase
import com.unplugged.up_antivirus.data.receiver.PackageMonitorService
import com.unplugged.up_antivirus.data.receiver.PackageStateReceiver
import com.unplugged.up_antivirus.domain.use_case.GetScanPreferencesUseCase
import com.unplugged.up_antivirus.domain.use_case.LogoutUseCase
import com.unplugged.up_antivirus.domain.use_case.UpdateDatabaseUseCase
import com.unplugged.up_antivirus.ui.splash.SplashActivity
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import us.spotco.malwarescanner.BuildConfig
import javax.inject.Inject

@HiltAndroidApp
class AntivirusApp : Application() {
    @Inject
    lateinit var packageStateReceiver: PackageStateReceiver

    @Inject
    lateinit var database: AntivirusRoomDatabase

    @Inject
    lateinit var updateDatabaseUseCase: UpdateDatabaseUseCase

    @Inject
    lateinit var getScanPreferencesUseCase: GetScanPreferencesUseCase

    @Inject
    lateinit var logoutUseCase: LogoutUseCase

    var isReceiverRegistered = false

    @Inject
    lateinit var accountHelper: AccountHelper

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        us.spotco.malwarescanner.Utils.setDatabaseUrl(com.unplugged.upantiviruscommon.BuildConfig.DEV_BASE_URL)

        Utils.printLog(AntivirusApp::class.java, "Application onCreate()")

        setHypatiaUtilsContext()

        getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE).edit()
            .putString("DATABASE_SERVER", com.unplugged.upantiviruscommon.BuildConfig.DEV_BASE_URL)
            .apply()

        accountHelper.setAccountListener(object : AccountListener {
            override fun onLoggedIn(username: String?) {
                Intent(applicationContext, SplashActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }

            override fun onLoggedOut() {
                Intent(applicationContext, SplashActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }
        })

        registerNotificationChannels()
    }

    private fun registerNotificationChannels() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val detectionChannel = NotificationChannel(
                "DETECTION",
                getString(R.string.up_av_lblNotificationMalwareDetectionTitle),
                NotificationManager.IMPORTANCE_HIGH
            )
            detectionChannel.description =
                getString(R.string.up_av_lblNotificationMalwareDetectionDescription)
            notificationManager.createNotificationChannel(detectionChannel)

            val realtimeScannerChannel = NotificationChannel(
                "com.unplugged.antivirus.REALTIME_CHANNEL",
                "Real Time channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(realtimeScannerChannel)
        }
    }

    fun listenPackagesState() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }

        registerReceiver(packageStateReceiver, filter)
        isReceiverRegistered = true
    }

    fun unregisterReceiver() {
        unregisterReceiver(packageStateReceiver)
    }

    private fun setHypatiaUtilsContext() {
        us.spotco.malwarescanner.Utils.setContext(applicationContext)
    }

    private fun stopPackageMonitorService() {
        val intent = Intent(this, PackageMonitorService::class.java)
        this.stopService(intent)
    }
}


package com.unplugged.up_antivirus.common

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.unplugged.accounthelper.AccountHelper
import com.unplugged.accounthelper.AccountListener
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.base.Utils
import com.unplugged.up_antivirus.data.AntivirusRoomDatabase
import com.unplugged.up_antivirus.domain.use_case.GetScanPreferencesUseCase
import com.unplugged.up_antivirus.domain.use_case.LogoutUseCase
import com.unplugged.up_antivirus.domain.use_case.SoftLogoutUseCase
import com.unplugged.up_antivirus.domain.use_case.UpdateDatabaseUseCase
import dagger.hilt.android.HiltAndroidApp
import us.spotco.malwarescanner.BuildConfig
import javax.inject.Inject
import androidx.core.content.edit

@HiltAndroidApp
class AntivirusApp : Application() {
    @Inject
    lateinit var database: AntivirusRoomDatabase
    @Inject
    lateinit var updateDatabaseUseCase: UpdateDatabaseUseCase
    @Inject
    lateinit var getScanPreferencesUseCase: GetScanPreferencesUseCase
    @Inject
    lateinit var logoutUseCase: LogoutUseCase
    @Inject
    lateinit var softLogoutUseCase: SoftLogoutUseCase

    @Inject
    lateinit var accountHelper: AccountHelper

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        us.spotco.malwarescanner.Utils.setDatabaseUrl(getString(com.unplugged.accounthelper.R.string.base_url) + "hypatia/")
        us.spotco.malwarescanner.Utils.setContext(applicationContext)

        Utils.printLog(AntivirusApp::class.java, "Application onCreate()")

        getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE).edit {
            putString("DATABASE_SERVER", getString(com.unplugged.accounthelper.R.string.base_url) + "hypatia/")
        }

        accountHelper.setAccountListener(object : AccountListener {
            override fun onLoggedIn(username: String?) {
                applicationContext.restartApplication()
            }

            override fun onSoftLogout() {
                softLogoutUseCase()
                applicationContext.restartApplication()
            }

            override fun onLoggedOut() {
                logoutUseCase()
                applicationContext.restartApplication()
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
}


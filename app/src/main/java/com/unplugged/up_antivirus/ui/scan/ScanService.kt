package com.unplugged.up_antivirus.ui.scan

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.domain.preferences.PreferencesRepository
import com.unplugged.up_antivirus.domain.use_case.CreateScanIdUseCase
import com.unplugged.up_antivirus.scanner.repository.ScannerRepository
import com.unplugged.upantiviruscommon.model.ScanParams
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScanService: Service() {

    @Inject lateinit var scannerRepository: ScannerRepository
    @Inject lateinit var  preferencesRepository: PreferencesRepository
    private val CHANNEL_ID = "ScanServiceChannel"

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        val notification: Notification = createNotification()
        startForeground(100, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("onStartCommand", "$this")
        CoroutineScope(Dispatchers.IO).launch{
            CreateScanIdUseCase(scannerRepository).invoke()
            scannerRepository.startScan(preferencesRepository.getScanParams())
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.up_av_app_scan_service),
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.up_av_app_scanning_service))
            .setContentText(getString(R.string.up_av_scan_in_progress))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_av_notification)

        return builder.build()
    }
}

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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScanService: Service() {

    companion object {
        private const val TAG = "ScanService"

        /**
         * Every legitimate start carries this action. AMS re-delivers a *null* intent when it
         * restarts a crashed service, so the action is what tells a real start apart from a
         * post-crash relaunch (UNP-8704).
         */
        const val ACTION_START_SCAN = "com.unplugged.up_antivirus.action.START_SCAN"
    }

    @Inject lateinit var scannerRepository: ScannerRepository
    @Inject lateinit var  preferencesRepository: PreferencesRepository
    private val CHANNEL_ID = "ScanServiceChannel"

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var scanJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        val notification: Notification = createNotification()
        startForeground(100, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action} flags=$flags startId=$startId")

        // A null intent means AMS is re-delivering after the process died. Never turn that into
        // a fresh full scan - that is what produced the endless crash/rescan loop (UNP-8704).
        if (intent?.action != ACTION_START_SCAN) {
            Log.w(TAG, "start without ACTION_START_SCAN (intent=$intent); stopping")
            stopSelf(startId)
            return START_NOT_STICKY
        }

        // In-process guard only: after a process death the Hilt graph is rebuilt and this returns
        // false, so it protects against a double start (manual scan racing the scheduled worker),
        // not against the crash loop. START_NOT_STICKY above is what breaks the loop.
        if (scannerRepository.isScanning() || scanJob?.isActive == true) {
            Log.d(TAG, "scan already running; ignoring duplicate start")
            return START_NOT_STICKY
        }

        scanJob = serviceScope.launch {
            CreateScanIdUseCase(scannerRepository).invoke()
            scannerRepository.startScan(preferencesRepository.getScanParams())
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
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

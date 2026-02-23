package com.unplugged.up_antivirus.data.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.unplugged.antivirus.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PackageMonitorService: Service() {
    private val CHANNEL_ID = "PackageMonitorServiceChannel"

    @Inject
    lateinit var packageStateReceiver: PackageStateReceiver

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification: Notification = createNotification()
        startForeground(1, notification)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }
        registerReceiver(packageStateReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("onStartCommand", "$this")
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(packageStateReceiver)
        } catch (e: IllegalArgumentException) {
            Log.e("unregisterReceiver", "Receiver was not registered", e)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.up_av_app_installation_service),
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.up_av_app_installation_service))
            .setContentText(getString(R.string.up_av_monitoring_applications_installation))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_av_notification)

        return builder.build()
    }
}

package com.unplugged.up_antivirus.common.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.unplugged.up_antivirus.base.Utils
import com.unplugged.up_antivirus.common.AntivirusApp
import com.unplugged.up_antivirus.ui.scan.ScanActivity
import com.unplugged.upantiviruscommon.utils.Constants
import com.unplugged.up_antivirus.ui.scan.ScanResultsActivity
import com.unplugged.up_antivirus.workers.CHANNEL_ID
import com.unplugged.up_antivirus.workers.NOTIFICATION_ID
import com.unplugged.up_antivirus.workers.NOTIFICATION_TITLE
import com.unplugged.up_antivirus.workers.NOTIFICATION_TITLE_SCAN_FINISHED
import com.unplugged.up_antivirus.workers.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
import com.unplugged.up_antivirus.workers.VERBOSE_NOTIFICATION_CHANNEL_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val threatGroup = "com.unplugged.antivirus.THREAT_GROUP"
    private val pendingIntentFlag: Int = PendingIntent.FLAG_IMMUTABLE

    fun showNotification(
        notificationId: Int?,
        historyItemId: Int,
        title: String,
        text: String
    ) {
        checkNotificationPermission()

        val pendingIntent = if (historyItemId != 0) {
            val intent = Intent(context, ScanResultsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(Constants.SCAN_ID, historyItemId)
            }
            PendingIntent.getActivity(context, historyItemId, intent, pendingIntentFlag)
        } else null

        val builder =
            NotificationCompat.Builder(context, "com.unplugged.antivirus.REALTIME_CHANNEL")
                .setSmallIcon(us.spotco.malwarescanner.R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(longArrayOf(0))
                .setGroup(threatGroup)
                .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(notificationId ?: 123321, builder.build())

        // Summary Notification
        val summaryNotification = NotificationCompat.Builder(context, "com.unplugged.antivirus.REALTIME_CHANNEL")
            .setSmallIcon(us.spotco.malwarescanner.R.drawable.ic_notification)
            .setContentTitle("Applications scanned")
            .setContentText("You have multiple application install scans")
            .setStyle(NotificationCompat.InboxStyle()
                .setSummaryText("App scanning Summary"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setGroup(threatGroup)
            .setGroupSummary(true)

        NotificationManagerCompat.from(context).notify(123456, summaryNotification.build())
    }

    fun makeStatusNotification(message: String, progress: Int? = null, historyItemId: Int = 0, scanFinished: Boolean = false) {
        checkNotificationPermission()

        val pendingIntent = if (historyItemId != 0) {
            if (!scanFinished) {
                val intent = Intent(context, ScanActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(Constants.SCAN_ID, historyItemId)
                }
                PendingIntent.getActivity(context, historyItemId, intent, pendingIntentFlag)
            } else {
                val intent = Intent(context, ScanResultsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(Constants.SCAN_ID, historyItemId)
                }
                PendingIntent.getActivity(context, historyItemId, intent, pendingIntentFlag)
            }
        } else null

        Utils.printLog(
            AntivirusApp::class.java,
            "makeStatusNotification: $message progress: $progress"
        )

        // Make a channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is new and not in the support library
            val channel = NotificationChannel(CHANNEL_ID, VERBOSE_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION

            // Add the channel
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)
        }

        val notificationTitle = if (!scanFinished) NOTIFICATION_TITLE else NOTIFICATION_TITLE_SCAN_FINISHED

        // Create the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(us.spotco.malwarescanner.R.drawable.ic_notification)
            .setContentTitle(notificationTitle)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
        progress?.let {
            builder.setProgress(100, it, false)
        }

        if (progress == 100) {
            builder.setProgress(0, 0, false)
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        }

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }

    fun errorNotification(title: String, description: String){
        checkNotificationPermission()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is new and not in the support library
            val description = description
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, title, importance)
            channel.description = description

            // Add the channel
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(us.spotco.malwarescanner.R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(LongArray(0))
                .setOnlyAlertOnce(true)

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun checkNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
    }

    fun dismissNotification() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}
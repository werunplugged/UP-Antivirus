package com.unplugged.up_antivirus.workers

@JvmField
val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
    "Verbose WorkManager Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Shows notifications whenever work starts"

@JvmField
val NOTIFICATION_TITLE: CharSequence = "Scanning"
val NOTIFICATION_TITLE_SCAN_FINISHED: CharSequence = "Scan Finished"
const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
const val NOTIFICATION_ID = 12321
const val UNIQUE_SCHEDULED_SCAN_WORKER_REQUEST = "uniqueScheduledScanWorkerRequest"
const val UNIQUE_SCAN_WORKER_REQUEST = "uniqueScanWorkerRequest"

const val WORKER_RESULT = "workerResult"

package com.unplugged.up_antivirus.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import javax.inject.Inject

class PackageStateReceiver @Inject constructor(private val checkAppTask: CheckAppTask): BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val packageName = intent.data?.schemeSpecificPart

        Log.d("CheckAppTask", "Intent action: -> $action")

        try {
            //React only for ADDED action because in case of CHANGED it calls ADDED also
            when (action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    if (packageName != null) {
                        checkAppTask.checkApp(context, packageName, true)
                    }
                }

                Intent.ACTION_PACKAGE_REMOVED -> {
                    Log.d("CheckAppTask", "app removed: -> $packageName")
                }
            }
        } catch (e: Exception) {
            Log.d("CheckAppTask", "Exception: -> $e")
        }
    }
}
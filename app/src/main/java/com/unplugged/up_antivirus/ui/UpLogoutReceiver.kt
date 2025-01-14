package com.unplugged.up_antivirus.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.unplugged.up_antivirus.base.Utils
import com.unplugged.up_antivirus.domain.use_case.LogoutUseCase
import javax.inject.Inject

class UpLogoutReceiver@Inject constructor(private val logoutUseCase: LogoutUseCase) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Utils.printLog(UpLogoutReceiver::class.java, "received event: ${intent?.action}")

        if (intent?.action == "com.unplugged.logged_out") {
            logoutUseCase()
        }
    }
}
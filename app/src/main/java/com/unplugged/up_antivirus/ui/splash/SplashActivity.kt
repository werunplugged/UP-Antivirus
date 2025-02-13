package com.unplugged.up_antivirus.ui.splash

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.unplugged.account.UpAccount
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.base.BaseActivity
import com.unplugged.up_antivirus.common.AntivirusApp
import com.unplugged.up_antivirus.data.receiver.PackageMonitorService
import com.unplugged.up_antivirus.ui.onBoarding.OnBoardingActivity
import com.unplugged.up_antivirus.ui.scan.ScanActivity
import com.unplugged.up_antivirus.ui.status.StatusActivity
import com.unplugged.upantiviruscommon.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (savedInstanceState == null) {
            val session = viewModel.getSession()
            if (session == null) {
                registerResult.launch(UpAccount.getAuthActivityIntent(this))
            } else {
                onSession()
            }
        }
    }

    private val registerResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onSession()
        } else {
            finish()
        }
    }

    private fun onSession() {
        if (viewModel.isScanning()){
            val intent = Intent(this, ScanActivity::class.java)
            intent.putExtra("scanType", viewModel.getScanType())
            startActivity(intent)
            finish()
        } else {
            if (viewModel.shouldShowOnBoardingFirstTime()) {
                startOnBoardingActivity(OnBoardingActivity.Companion.Reason.FIRST_START)
            } else {
                val intent = Intent(this, StatusActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun startOnBoardingActivity(reason: OnBoardingActivity.Companion.Reason) {
        val intent = Intent(this, OnBoardingActivity::class.java)
        intent.putExtra(Constants.REASON_KEY, reason)
        startActivity(intent)
        finish()
    }
}
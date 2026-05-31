package com.unplugged.up_antivirus.ui.splash

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import com.unplugged.accounthelper.getAuthActivityIntent
import com.unplugged.antivirus.R
import com.unplugged.antivirus.databinding.ActivitySplashBinding
import com.unplugged.up_antivirus.base.BaseActivity
import com.unplugged.up_antivirus.ui.onBoarding.OnBoardingActivity
import com.unplugged.up_antivirus.ui.scan.ScanActivity
import com.unplugged.up_antivirus.ui.status.StatusActivity
import com.unplugged.upantiviruscommon.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private val viewModel: SplashViewModel by viewModels()
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            when {
                viewModel.hasAttestation() -> onSession()
                viewModel.hasSession() -> onSession()
                else -> launchAuthActivityOrShowError()
            }
        }
    }

    private fun launchAuthActivityOrShowError() {
        try {
            startActivity(getAuthActivityIntent(this))
            finish()
        } catch (e: ActivityNotFoundException) {
            Log.w("SplashActivity", "up_account AuthActivity not available", e)
            binding.errorTv.apply {
                text = getString(R.string.up_av_up_phone_only)
                visibility = View.VISIBLE
            }
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
package com.unplugged.up_antivirus.ui.splash

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.unplugged.accounthelper.getAuthActivityIntent
import com.unplugged.antivirus.databinding.ActivitySplashBinding
import com.unplugged.up_antivirus.base.BaseActivity
import com.unplugged.up_antivirus.domain.AuthMode
import com.unplugged.up_antivirus.ui.onBoarding.OnBoardingActivity
import com.unplugged.up_antivirus.ui.scan.ScanActivity
import com.unplugged.up_antivirus.ui.status.StatusActivity
import com.unplugged.upantiviruscommon.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private val TAG = SplashActivity::class.simpleName
    private val viewModel: SplashViewModel by viewModels()
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            when (viewModel.authMode) {
                is AuthMode.Attestation -> {
                    viewModel.tokenState.observe(this) { state ->
                        when {
                            state.tokenExist -> onSession()
                            state.error != null -> {
                                binding.errorTv.apply {
                                    text = state.error
                                    visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                    viewModel.getOrRefreshToken()
                }
                is AuthMode.Account -> {
                    val session = viewModel.getSession()
                    if (session == null) {
                        try {
                            registerResult.launch(getAuthActivityIntent(this))
                        } catch (e: ActivityNotFoundException) {
                            Log.d(TAG, "${e.message}")
                            binding.errorTv.apply {
                                text = getString(com.unplugged.resources.resources.R.string.up_account_app_not_installed)
                                visibility = View.VISIBLE
                            }
                        } catch (e: SecurityException) {
                            Log.d(TAG, "${e.message}")
                            binding.errorTv.apply {
                                text = getString(com.unplugged.resources.resources.R.string.no_permission_account_activity)
                                visibility = View.VISIBLE
                            }
                        }
                    } else {
                        onSession()
                    }
                }
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
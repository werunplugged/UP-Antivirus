package com.unplugged.up_antivirus.ui.onBoarding

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.unplugged.antivirus.BuildConfig
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.base.BaseActivity
import com.unplugged.upantiviruscommon.utils.Constants
import com.unplugged.up_antivirus.ui.status.StatusActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity : BaseActivity() {

    private lateinit var appVersionTv: TextView
    private lateinit var messageTv: TextView
    private lateinit var actionButton: TextView

    companion object {
        enum class Reason {
            FIRST_START, UNKNOWN
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)
        appVersionTv = findViewById(R.id.app_version_tv)
        messageTv = findViewById(R.id.message_tv)
        actionButton = findViewById(R.id.action_button)

        val reason =
            intent?.extras?.getSerializable(Constants.REASON_KEY) as Reason? ?: Reason.UNKNOWN
        renderUI(reason)
    }

    private fun renderUI(reason: Reason) {
        appVersionTv.text = getString(R.string.up_av_app_version, BuildConfig.VERSION_NAME)

        when (reason) {
            Reason.FIRST_START -> {
                messageTv.text = getString(R.string.up_av_on_boarding_message_first_start)
                actionButton.setOnClickListener {
                    Intent(this, StatusActivity::class.java).also {
                        startActivity(it)
                        finish()
                    }
                }
            }

            else -> {
                messageTv.text = getString(R.string.up_av_on_boarding_message_unknown_reason)
                messageTv.setBackgroundResource(R.drawable.bg_red_border)
                actionButton.text = getString(R.string.up_av_on_boarding_button_close)
                actionButton.setOnClickListener {
                    Runtime.getRuntime().exit(0)
                }
            }
        }
    }
}
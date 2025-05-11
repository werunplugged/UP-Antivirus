package com.unplugged.up_antivirus.ui.history

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.base.BaseActivity
import com.unplugged.upantiviruscommon.utils.Constants
import com.unplugged.up_antivirus.data.history.model.HistoryModel
import com.unplugged.up_antivirus.ui.CellMarginDecoration
import com.unplugged.up_antivirus.ui.scan.ScanResultsActivity
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder

@AndroidEntryPoint
class ScanHistoryActivity : BaseActivity() {

    private lateinit var scanHistoryRv: RecyclerView
    private lateinit var noHistoryTv: TextView
    private lateinit var closeButton: AppCompatButton
    private lateinit var infoButton: ImageView
    private lateinit var infoText: TextView

    private val historyAdapter by lazy {
        ScanHistoryAdapter(historyClickListener)
    }

    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_history)

        scanHistoryRv = findViewById(R.id.scan_history_rv)
        noHistoryTv = findViewById(R.id.no_history_tv)
        closeButton = findViewById(R.id.close_button)
        infoButton = findViewById(R.id.info_button)
        infoText = findViewById(R.id.info_text)

        scanHistoryRv.adapter = historyAdapter
        scanHistoryRv.addItemDecoration(CellMarginDecoration(24, 0))
        scanHistoryRv.layoutManager = LinearLayoutManager(this)
        scanHistoryRv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        closeButton.isActivated = true
        closeButton.setOnClickListener {
            finish()
        }

        infoButton.setOnClickListener { openInfoDialog() }
        infoText.setOnClickListener { openInfoDialog() }
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.historyState.observe(this) {
            historyAdapter.setHistory(it.history.reversed())
            if (it.history.isEmpty()) {
                noHistoryTv.visibility = View.VISIBLE
            } else {
                noHistoryTv.visibility = View.INVISIBLE
            }
        }
    }

    private val historyClickListener: (item: HistoryModel) -> Unit = { historyItem ->
        val scanId = historyItem.id
        Intent(this, ScanResultsActivity::class.java).apply {
            putExtra(Constants.SCAN_ID, scanId)
            putExtra("fromHistory", true)
            intent.putExtra("history_item", historyItem)
            startActivity(this)
        }
    }

    private fun openInfoDialog() {
        val deviceModel = getDeviceModel()
        if (deviceModel == "UP01") {
            showDialog(
                getString(R.string.up_av_malware_trackers_info_title),
                getString(R.string.up_av_malware_trackers_info_message_up_phone)
            ) {}
        } else {
            showDialogWithIntent(
                getString(R.string.up_av_malware_trackers_info_title),
                getString(R.string.up_av_malware_trackers_info_message),
                getString(R.string.up_av_unplugged_website_clickable_text),
                getString(R.string.up_av_unplugged_website)
            ) {}
        }
    }

    private fun getDeviceModel(): String? {
        val deviceModel = Build.MODEL ?: return null
        return URLEncoder.encode(deviceModel, "UTF-8")
    }
}
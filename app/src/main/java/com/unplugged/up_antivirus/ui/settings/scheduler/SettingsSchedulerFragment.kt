package com.unplugged.up_antivirus.ui.settings.scheduler

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.base.BaseActivity
import com.unplugged.up_antivirus.ui.settings.main.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsSchedulerFragment : Fragment(R.layout.fragment_settings_scheduler) {

    private lateinit var scanSchedulerLayout: ConstraintLayout
    private lateinit var databaseUpdateSchedulerLayout: ConstraintLayout
    private lateinit var scanSchedulerPreview: TextView
    private lateinit var databaseUpdateSchedulerPreview: TextView

    private val viewModel: SettingsViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)

        scanSchedulerLayout.setOnClickListener { checkIfBackgroundExecutionAllowed("scan") }
        databaseUpdateSchedulerLayout.setOnClickListener { checkIfBackgroundExecutionAllowed("database") }
        val interval = viewModel.getScheduledScanningInterval()
        scanSchedulerPreview.text = viewModel.getScheduledScanningIntervalString(interval)
    }

    private fun initViews(view: View) {
        scanSchedulerLayout = view.findViewById(R.id.scan_scheduler_layout)
        databaseUpdateSchedulerLayout = view.findViewById(R.id.database_update_scheduler_layout)
        scanSchedulerPreview = view.findViewById(R.id.scan_scheduler_preview)
        databaseUpdateSchedulerPreview = view.findViewById(R.id.database_update_scheduler_preview)
    }

    private fun checkIfBackgroundExecutionAllowed(scanType: String) {
        val packageName = activity?.packageName
        val pm =
            activity?.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            (activity as BaseActivity).showDialog(
                getString(R.string.up_av_allow_background_activity),
                getString(R.string.up_av_inorder_to_run_the_scheduled_scan_we_need_the_background_activity_permission)
            ) { openSettingsForAllowingBackgroundActivity() }
        } else {
            when (scanType) {
                "scan" -> {
                    openScheduleDialog("scan")
                }

                "database" -> {
                    openScheduleDialog("database")
                }
            }
        }
    }

    @SuppressLint("BatteryLife")
    private fun openSettingsForAllowingBackgroundActivity() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:${activity?.packageName}")
        startActivity(intent)
    }

    private fun openScheduleDialog(type: String) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
        builder.setTitle(getString(R.string.up_av_select_scheduled_scan_interval))
        val listItems = arrayOf(
            Html.fromHtml("<font color='#FFFFFF'>${getString(R.string.up_av_daily)}</font>"),
            Html.fromHtml("<font color='#FFFFFF'>${getString(R.string.up_av__3_times_a_week)}</font>"),
            Html.fromHtml("<font color='#FFFFFF'>${getString(R.string.up_av_weekly)}</font>"),
            Html.fromHtml("<font color='#FFFFFF'>${getString(R.string.up_av_disable)}</font>")
        )
        val selection = if (viewModel.getScheduledScanningInterval() != -1) {
            viewModel.getScheduledScanningInterval()
        } else {
            3
        }

        builder.setSingleChoiceItems(
            listItems,
            selection
        ) { dialog, which ->
            viewModel.scheduleScanning(which)
            scanSchedulerPreview.text = viewModel.getScheduledScanningIntervalString(which)
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.up_av_cancel)) { _, _ -> }
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
        dialog.window?.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.background_dialog
            )
        )
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.main_text_color
                )
            )
    }
}
package com.unplugged.up_antivirus.ui.settings.main

import android.content.Intent
import android.graphics.Outline
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.unplugged.account.UpAccount
import com.unplugged.antivirus.BuildConfig
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.data.receiver.PackageMonitorService
import com.unplugged.up_antivirus.ui.settings.SettingsOnClickListener
import com.unplugged.up_antivirus.ui.settings.legals.SettingsLegalsFragment
import com.unplugged.up_antivirus.ui.settings.scheduler.SettingsSchedulerFragment
import com.unplugged.up_antivirus.ui.status.StatusActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings_home), SettingsOnClickListener {

    private lateinit var appBarLayout: AppBarLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var container: FragmentContainerView

    private lateinit var settingsRecyclerView: RecyclerView
    private lateinit var settingsAdapter: SettingsAdapter
    private lateinit var version: TextView
    private lateinit var realTimeProtectionSwitch: SwitchCompat
    private lateinit var downloadOverCellularSwitch: SwitchCompat

    private val viewModel: SettingsViewModel by viewModels()


//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            activity?.window?.apply {
//                // Change status bar color
//                statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
//                // Set status bar icons color
//                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//            }
//        }
//        return super.onCreateView(inflater, container, savedInstanceState)
//    }//TODO:don't delete, for future light mode

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)

        settingsAdapter = SettingsAdapter(this)
        settingsAdapter.submitList(viewModel.getSettingsList())
        settingsRecyclerView.adapter = settingsAdapter

        version.text = getString(R.string.up_av_app_version, BuildConfig.VERSION_NAME)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (!handleBackPressed()) {
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        toolbar.title = getString(R.string.up_av_settings)
        toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        appBarLayout.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRect(0, 0, view.width, view.height)
            }
        }

        realTimeProtectionSwitch.isChecked = viewModel.getRealTimeProtection()
        realTimeProtectionSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startPackageMonitorService()
            } else {
                stopPackageMonitorService()
            }
        }

        downloadOverCellularSwitch.isChecked = viewModel.getDownloadOverCellular() == true
        downloadOverCellularSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setDownloadOverCellular(true)
            } else {
                viewModel.setDownloadOverCellular(false)
            }
        }
    }

    private fun initViews(view: View) {
        appBarLayout = view.findViewById(R.id.app_bar_layout)
        toolbar = view.findViewById(R.id.settings_toolbar)
        container = view.findViewById(R.id.container)
        settingsRecyclerView = view.findViewById(R.id.recycler_view)
        version = view.findViewById(R.id.version_tv)
        realTimeProtectionSwitch = view.findViewById(R.id.real_time_protection_toggle)
        downloadOverCellularSwitch = view.findViewById(R.id.updates_over_cellular_toggle)
    }

    override fun onMainItemClick(item: String) {
        when (item) {
            "Account" -> {
                UpAccount.getMyAccountActivityIntent(requireContext())?.let {
                    startActivity(it)
                } ?: run {
                    Toast.makeText(requireContext(), "Not Implemented", Toast.LENGTH_SHORT).show()
                }
            }

            "Scheduler" -> {
                toolbar.title = getString(R.string.up_av_scheduler)
                val settingsSchedulerFragment = SettingsSchedulerFragment()
                val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
                setTransitionAnimations(transaction)
                transaction.add(container.id, settingsSchedulerFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }

            "Support" -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    UpAccount.launchSupport(requireActivity())
                }
            }

            "Legals" -> {
                toolbar.title = getString(R.string.up_av_legals)
                val settingsLegalsFragment = SettingsLegalsFragment()
                val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
                setTransitionAnimations(transaction)
                transaction.add(container.id, settingsLegalsFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
    }

//    override fun onDestroyView() {//TODO:don't delete, for future light mode
//        super.onDestroyView()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            activity?.window?.apply {
//                // Reset status bar color to default
//                statusBarColor = ContextCompat.getColor(requireContext(),R.color.colorPrimary)
//                // Reset status bar icons color to default
//                decorView.systemUiVisibility = 0
//            }
//        }
//    }

    fun handleBackPressed(): Boolean {
        val fragment = childFragmentManager.findFragmentById(R.id.container)
        return if (childFragmentManager.fragments.isNotEmpty()) {
            if (fragment is SettingsLegalsFragment) {
                if (!fragment.handleOnBackPressed()) {
                    toolbar.title = getString(R.string.up_av_settings)
                }
            } else {
                toolbar.title = getString(R.string.up_av_settings)
                childFragmentManager.popBackStack()
            }
            true
        } else {
            false
        }
    }

    private fun setTransitionAnimations(transaction: FragmentTransaction) {
        transaction.setCustomAnimations(
            R.anim.fragment_in_right,
            R.anim.fragment_out_left,
            R.anim.fragment_in_left,
            R.anim.fragment_out_right
        )
    }

    private fun startPackageMonitorService() {
        viewModel.setRealTimeProtection(true)
        val intent = Intent(requireContext(), PackageMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
    }

    private fun stopPackageMonitorService() {
        viewModel.setRealTimeProtection(false)
        val intent = Intent(requireContext(), PackageMonitorService::class.java)
        requireContext().stopService(intent)
    }
}
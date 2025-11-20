package com.unplugged.up_antivirus.ui.settings.legals

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.ui.settings.main.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsLegalsFragment : Fragment(R.layout.fragment_settings_legals), LegalsOnClickListener {

    private lateinit var adapter: LegalsSettingsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var webView: WebView

    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)

        adapter = LegalsSettingsAdapter(this)
        adapter.submitList(viewModel.getLegalsSettingsList())
        recyclerView.adapter = adapter
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        webView = view.findViewById(R.id.web_view)
    }

    override fun onLegalsItemClick(legalItem: LegalItem) {
        if (!legalItem.url.isNullOrEmpty()) {
            if (legalItem.url.startsWith("file:///android_asset/")) {
                webView.isVisible = true
                webView.loadUrl(legalItem.url)
            } else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(legalItem.url))
                startActivity(intent)
            }
        }
    }

    fun handleOnBackPressed(): Boolean {
        if (webView.isVisible) {
            webView.isVisible = false
            return true
        } else {
            parentFragmentManager.popBackStack()
            return false
        }
    }
}
package com.unplugged.up_antivirus.ui.settings.legals

import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
    lateinit var webView: WebView

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
        webView.isVisible = true
        if (!legalItem.url.isNullOrEmpty()) {
            val webSettings: WebSettings = webView.settings
            webSettings.javaScriptEnabled = true
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    view?.loadUrl(legalItem.url)
                    return true
                }

                // For older versions of Android
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    view?.loadUrl(legalItem.url)
                    return true
                }
            }
            webView.settings.setSupportZoom(true)
            webView.settings.builtInZoomControls = true
            webView.settings.displayZoomControls = false
            webView.loadUrl(legalItem.url) // Initial load
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
package com.unplugged.up_antivirus.ui.scan

import android.os.Bundle
import android.text.Spanned
import android.text.SpannedString
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.unplugged.antivirus.R
import com.unplugged.up_antivirus.data.tracker.model.ExpandableItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InfoFragment : Fragment(R.layout.fragment_trackers_malware_info) {

    private lateinit var closeBtn : AppCompatButton
    private lateinit var trackersBtn : AppCompatButton
    private lateinit var malwareBtn : AppCompatButton
    private lateinit var firstTitle: TextView
    private lateinit var firstDescription: TextView
    private lateinit var secondTitle: TextView
    private lateinit var secondDescription: TextView
    private lateinit var thirdTitle: TextView
    private lateinit var thirdDescription: TextView
    private lateinit var lastTitle: TextView
    private lateinit var lastDescription: TextView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)

        trackersBtn.isActivated = true
        malwareBtn.isActivated = false
        setTexts(ViewType.TRACKERS)

        trackersBtn.setOnClickListener{
            trackersBtn.isActivated = true
            malwareBtn.isActivated = false
            setTexts(ViewType.TRACKERS)
        }

        malwareBtn.setOnClickListener{
            trackersBtn.isActivated = false
            malwareBtn.isActivated = true
            setTexts(ViewType.MALWARE)
        }

        closeBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initViews(view: View){
        closeBtn = view.findViewById(R.id.close_button)
        trackersBtn = view.findViewById(R.id.trackers_btn)
        malwareBtn = view.findViewById(R.id.malware_btn)
        firstTitle = view.findViewById(R.id.first_title_tv)
        firstDescription = view.findViewById(R.id.first_description_tv)
        secondTitle = view.findViewById(R.id.second_title_tv)
        secondDescription = view.findViewById(R.id.second_description_tv)
        thirdTitle = view.findViewById(R.id.third_title_tv)
        thirdDescription = view.findViewById(R.id.third_description_tv)
        lastTitle = view.findViewById(R.id.last_title_tv)
        lastDescription = view.findViewById(R.id.last_description_tv)
    }

    private fun setTexts(viewType: ViewType){
        when(viewType){
            ViewType.TRACKERS -> {
                convertToHtmlFormat(firstTitle, getString(R.string.what_are_trackers))
                convertToHtmlFormat(firstDescription, getString(R.string.what_are_trackers_description))
                convertToHtmlFormat(secondTitle, getString(R.string.why_it_matters))
                convertToHtmlFormat(secondDescription, getString(R.string.why_it_matters_description))
                convertToHtmlFormat(thirdTitle, getString(R.string.common_types_of_trackers))
                convertToHtmlFormat(thirdDescription, getString(R.string.common_types_of_trackers_description))
                convertToHtmlFormat(lastTitle, getString(R.string.how_up_phone_protects_you))
                convertToHtmlFormat(lastDescription, getString(R.string.how_up_phone_protects_you_description))
            }
            ViewType.MALWARE -> {
                convertToHtmlFormat(firstTitle, getString(R.string.what_are_malware))
                convertToHtmlFormat(firstDescription, getString(R.string.what_is_malware_description))
                convertToHtmlFormat(secondTitle, getString(R.string.how_malware_spreads))
                convertToHtmlFormat(secondDescription, getString(R.string.how_malware_spreads_description))
                convertToHtmlFormat(thirdTitle, getString(R.string.types_of_malware))
                convertToHtmlFormat(thirdDescription, getString(R.string.types_of_malware_description))
                convertToHtmlFormat(lastTitle, getString(R.string.how_up_phone_protects_you))
                convertToHtmlFormat(lastDescription, getString(R.string.how_up_phone_protects_you_description_malware))
            }
        }
    }

    private fun convertToHtmlFormat(view: TextView, text: String){
        view.setHtmlText(text)
    }

    enum class ViewType{
        TRACKERS,
        MALWARE
    }

    private fun trimTrailingWhitespace(source: Spanned): Spanned {
        var i = source.length

        do {
            i--
        } while (i >= 0 && Character.isWhitespace(source[i]))

        return if (i == source.length - 1) {
            source
        } else {
            SpannedString(source.subSequence(0, i + 1))
        }
    }

    private fun TextView.setHtmlText(html: String) {
        val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
        text = trimTrailingWhitespace(spanned)
    }
}
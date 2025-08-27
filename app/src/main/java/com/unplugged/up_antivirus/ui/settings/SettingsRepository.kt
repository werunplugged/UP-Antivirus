package com.unplugged.up_antivirus.ui.settings

import com.unplugged.up_antivirus.ui.settings.legals.LegalItem
import javax.inject.Inject

class SettingsRepository@Inject constructor() {

    var settingsList: List<String> = listOf("Account", "Scheduler", "Support", "Legals")


    var legalsList: List<LegalItem> =
        listOf(
            LegalItem("title","Antivirus policy"),
            LegalItem("item","Terms & Conditions","https://unplugged.com/pages/end-user-license-agreement"),
            LegalItem("item","Privacy Policy","https://unplugged.com/policies/privacy-policy"),
            LegalItem("title","Third party libraries"),
            LegalItem("item","Third party licenses", "file:///android_asset/open_source_licenses_av.html")
        )

}
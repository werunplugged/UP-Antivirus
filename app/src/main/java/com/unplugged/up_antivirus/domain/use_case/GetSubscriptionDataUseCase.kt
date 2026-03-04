package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.accounthelper.AccountHelper
import com.unplugged.accounthelper.AccountSubscription
import com.unplugged.accounthelper.SubscriptionType
import com.unplugged.up_antivirus.domain.AuthMode

class GetSubscriptionDataUseCase(
    private val accountHelper: AccountHelper,
    private val authMode: AuthMode
) {
    operator fun invoke(): AccountSubscription? {
        if (authMode is AuthMode.Attestation) {
            return AccountSubscription("", SubscriptionType.BASIC, 0L, 0L, 0L, 0.0, "", "", "")
        }
        return accountHelper.getAccountSubscription()
    }
}
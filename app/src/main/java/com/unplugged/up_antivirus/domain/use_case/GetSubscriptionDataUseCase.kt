package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.accounthelper.AccountHelper
import com.unplugged.accounthelper.AccountSubscription


class GetSubscriptionDataUseCase(private val accountHelper: AccountHelper) {

    operator fun invoke(): AccountSubscription? {
        return accountHelper.getAccountSubscription()
    }
}
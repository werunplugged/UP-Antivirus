package com.unplugged.up_antivirus.ui.status

import com.unplugged.accounthelper.AccountSubscription


data class SubscriptionState(
    val accountSubscription: AccountSubscription? = null,
    val error: String? = null
)
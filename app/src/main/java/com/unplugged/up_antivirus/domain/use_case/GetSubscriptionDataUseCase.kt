package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.account.AccountSubscription
import com.unplugged.account.UpAccount

class GetSubscriptionDataUseCase {

    operator fun invoke(): AccountSubscription? {
        return UpAccount.getAccountSubscription()
    }
}
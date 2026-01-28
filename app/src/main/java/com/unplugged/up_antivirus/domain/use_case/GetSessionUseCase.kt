package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.accounthelper.AccountHelper
import com.unplugged.accounthelper.SessionData

class GetSessionUseCase(private val accountHelper: AccountHelper) {

    operator fun invoke(): SessionData? {
        return accountHelper.getSession()
    }
}
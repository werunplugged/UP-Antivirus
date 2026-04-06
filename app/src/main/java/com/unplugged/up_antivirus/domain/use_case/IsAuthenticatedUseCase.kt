package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.accounthelper.AccountHelper

class IsAuthenticatedUseCase(private val accountHelper: AccountHelper) {

    operator fun invoke(): Boolean {
        return accountHelper.getAttToken() != null
    }
}

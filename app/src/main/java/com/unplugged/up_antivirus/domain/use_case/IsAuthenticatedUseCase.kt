package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.accounthelper.AccountHelper

class IsAuthenticatedUseCase(private val accountHelper: AccountHelper) {

    fun hasAttestation(): Boolean = accountHelper.getAttToken(refresh = false) != null

    fun hasSession(): Boolean = accountHelper.getSession() != null

    operator fun invoke(): Boolean {
        return hasAttestation() || hasSession()
    }
}

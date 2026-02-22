package com.unplugged.up_antivirus

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder

class StubAuthenticatorService : Service() {

    private lateinit var authenticator: StubAuthenticator

    override fun onCreate() {
        super.onCreate()
        authenticator = StubAuthenticator(this)
    }

    override fun onBind(intent: Intent?): IBinder = authenticator.iBinder

    private class StubAuthenticator(context: Context) : AbstractAccountAuthenticator(context) {

        override fun editProperties(response: AccountAuthenticatorResponse?, accountType: String?): Bundle =
            throw UnsupportedOperationException()

        override fun addAccount(
            response: AccountAuthenticatorResponse?,
            accountType: String?,
            authTokenType: String?,
            requiredFeatures: Array<out String>?,
            options: Bundle?
        ): Bundle? = null

        override fun confirmCredentials(
            response: AccountAuthenticatorResponse?,
            account: Account?,
            options: Bundle?
        ): Bundle? = null

        override fun getAuthToken(
            response: AccountAuthenticatorResponse?,
            account: Account?,
            authTokenType: String?,
            options: Bundle?
        ): Bundle = throw UnsupportedOperationException()

        override fun getAuthTokenLabel(authTokenType: String?): String =
            throw UnsupportedOperationException()

        override fun updateCredentials(
            response: AccountAuthenticatorResponse?,
            account: Account?,
            authTokenType: String?,
            options: Bundle?
        ): Bundle = throw UnsupportedOperationException()

        override fun hasFeatures(
            response: AccountAuthenticatorResponse?,
            account: Account?,
            features: Array<out String>?
        ): Bundle = throw UnsupportedOperationException()
    }
}

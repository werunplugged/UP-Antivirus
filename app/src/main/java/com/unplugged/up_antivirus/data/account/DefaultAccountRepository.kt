package com.unplugged.up_antivirus.data.account

import com.unplugged.up_antivirus.data.preferences.SharedPreferencesSource
import com.unplugged.upantiviruscommon.model.Resource
import com.unplugged.up_antivirus.domain.account.AccountRepository
import javax.inject.Inject

class DefaultAccountRepository @Inject constructor(
    private val accountRemoteSource: AccountRemoteSource,
    private val sharedPreferencesSource: SharedPreferencesSource
) : AccountRepository {

    private var remainingScans = 0

    override suspend fun updateScanDone() {
        accountRemoteSource.updateScanDone().also {
            if (it is Resource.Data) {
                remainingScans = it.data
            }
        }
    }

    override suspend fun syncRemainingScans() {
        accountRemoteSource.getRemainingScans().also {
            if (it is Resource.Data) {
                remainingScans = it.data
            }
        }
    }

    override fun getRemainingScans(): Int {
        return remainingScans
    }

    override fun reset() {
        remainingScans = 0
    }
}
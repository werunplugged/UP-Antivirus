package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.up_antivirus.domain.account.AccountRepository
import javax.inject.Inject

class SyncRemainingScansUseCase @Inject constructor(private val accountRepository: AccountRepository) {

    suspend operator fun invoke() {
        accountRepository.syncRemainingScans()
    }
}
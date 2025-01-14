package com.unplugged.up_antivirus.domain.use_case

import com.unplugged.up_antivirus.domain.account.AccountRepository
import javax.inject.Inject

class GetRemainingScansUseCase @Inject constructor(private val accountRepository: AccountRepository) {

    operator fun invoke(): Int {
        return accountRepository.getRemainingScans()
    }
}
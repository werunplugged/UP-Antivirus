package com.unplugged.up_antivirus.domain.account

data class AccountState(
    val isLoading: Boolean = false,
    val account: AccountModel? = null,
    val error: String? = null
)
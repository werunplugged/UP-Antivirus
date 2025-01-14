package com.unplugged.up_antivirus.domain.account

data class AccountModel(
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phoneNumber: String?
)
